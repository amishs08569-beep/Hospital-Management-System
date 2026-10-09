package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.BillingDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.impl.BillingDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Payment;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.BillingService;
import com.hospital.util.PdfGenerator;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillingDao billingDao;
    private final PatientDao patientDao;
    private final AuditService auditService;

    public BillingServiceImpl() {
        this.billingDao = new BillingDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public BillingServiceImpl(BillingDao billingDao, PatientDao patientDao, AuditService auditService) {
        this.billingDao = billingDao;
        this.patientDao = patientDao;
        this.auditService = auditService;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        SecurityContext.requirePermission(RolePermission.Permission.CREATE_INVOICE);

        if (invoice.getPatientId() == null) throw new ValidationException("Patient is required.");
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new ValidationException("Invoice must contain at least one item line.");
        }

        patientDao.findById(invoice.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }

        // Calculate subtotal, discount, tax, total, balance using BigDecimal safe math
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getQuantity() <= 0) item.setQuantity(1);
            if (item.getUnitPrice() == null) item.setUnitPrice(BigDecimal.ZERO);
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setTotalPrice(itemTotal);
            subtotal = subtotal.add(itemTotal);
        }
        invoice.setSubtotal(subtotal);

        // Discount
        BigDecimal discountPercent = invoice.getDiscountPercent() != null ? invoice.getDiscountPercent() : BigDecimal.ZERO;
        BigDecimal discountAmount = subtotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setDiscountPercent(discountPercent);
        invoice.setDiscountAmount(discountAmount);

        BigDecimal taxableAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        // Tax
        BigDecimal taxPercent = invoice.getTaxPercent() != null ? invoice.getTaxPercent() : BigDecimal.ZERO;
        BigDecimal taxAmount = taxableAmount.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setTaxPercent(taxPercent);
        invoice.setTaxAmount(taxAmount);

        BigDecimal total = taxableAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(total);

        BigDecimal paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        invoice.setPaidAmount(paid);

        BigDecimal balance = total.subtract(paid).max(BigDecimal.ZERO);
        invoice.setBalanceAmount(balance);

        if (balance.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setPaymentStatus("PAID");
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setPaymentStatus("PARTIAL");
        } else {
            invoice.setPaymentStatus("UNPAID");
        }

        invoice.setCreatedByUserId(SecurityContext.getCurrentUserId());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int id = billingDao.insertInvoice(invoice, conn);
                invoice.setInvoiceId(id);

                for (InvoiceItem item : invoice.getItems()) {
                    item.setInvoiceId(id);
                    billingDao.insertItem(item, conn);
                }

                conn.commit();
                auditService.log("CREATE", "INVOICE", invoice.getInvoiceNumber(),
                        "Generated invoice total: $" + invoice.getTotalAmount() + " for Patient ID " + invoice.getPatientId());
                logger.info("Created invoice {} for amount {}", invoice.getInvoiceNumber(), invoice.getTotalAmount());
                return invoice;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating invoice: {}", e.getMessage());
            throw new DatabaseException("Failed to save invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment recordPayment(int invoiceId, BigDecimal amount, String paymentMethod, String txnRef, String notes) {
        SecurityContext.requirePermission(RolePermission.Permission.COLLECT_PAYMENT);

        ValidationUtil.requirePositive(amount, "Payment Amount");
        ValidationUtil.requireNonBlank(paymentMethod, "Payment Method");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Invoice inv = billingDao.findById(invoiceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

                if ("PAID".equalsIgnoreCase(inv.getPaymentStatus())) {
                    throw new ValidationException("Invoice " + inv.getInvoiceNumber() + " is already fully paid.");
                }

                if (amount.compareTo(inv.getBalanceAmount()) > 0) {
                    throw new ValidationException("Payment amount ($" + amount + ") exceeds outstanding balance ($" + inv.getBalanceAmount() + ").");
                }

                Payment payment = new Payment();
                payment.setInvoiceId(invoiceId);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setAmount(amount);
                payment.setPaymentMethod(paymentMethod);
                payment.setTransactionRef(txnRef);
                payment.setNotes(notes);
                payment.setReceivedByUserId(SecurityContext.getCurrentUserId());

                int paymentId = billingDao.insertPayment(payment, conn);
                payment.setPaymentId(paymentId);

                // Update invoice balances
                BigDecimal newPaid = inv.getPaidAmount().add(amount);
                BigDecimal newBalance = inv.getTotalAmount().subtract(newPaid).max(BigDecimal.ZERO);
                String newStatus = (newBalance.compareTo(BigDecimal.ZERO) == 0) ? "PAID" : "PARTIAL";

                billingDao.updateInvoiceBalances(invoiceId, newPaid, newBalance, newStatus, conn);
                conn.commit();

                auditService.log("PAYMENT", "PAYMENT", payment.getPaymentNumber(),
                        "Received payment of $" + amount + " for " + inv.getInvoiceNumber() + " via " + paymentMethod);
                logger.info("Recorded payment {} for invoice {}", payment.getPaymentNumber(), inv.getInvoiceNumber());
                return payment;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error processing payment: {}", e.getMessage());
            throw new DatabaseException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Invoice> getInvoiceById(int invoiceId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_INVOICES);
        return billingDao.findById(invoiceId);
    }

    @Override
    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_INVOICES);
        return billingDao.findByNumber(invoiceNumber);
    }

    @Override
    public List<Invoice> listInvoices(String status, String patientSearch) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_INVOICES);
        return billingDao.findAll(status, patientSearch);
    }

    @Override
    public File exportInvoicePdf(int invoiceId, File destinationDirectory) throws Exception {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_INVOICES);
        Invoice inv = billingDao.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }
        File pdfFile = new File(destinationDirectory, "Invoice_" + inv.getInvoiceNumber() + ".pdf");
        PdfGenerator.generateInvoicePdf(inv, pdfFile);
        return pdfFile;
    }

    @Override
    public BigDecimal getTodayRevenue() {
        return billingDao.getTodayRevenue();
    }

    @Override
    public BigDecimal getMonthlyRevenue() {
        return billingDao.getMonthlyRevenue();
    }

    @Override
    public BigDecimal getTotalOutstandingBalance() {
        return billingDao.getTotalOutstandingBalance();
    }

    @Override
    public int getUnpaidInvoicesCount() {
        return billingDao.countUnpaidInvoices();
    }
}
