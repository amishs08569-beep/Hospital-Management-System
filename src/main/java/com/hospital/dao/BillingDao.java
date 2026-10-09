package com.hospital.dao;

import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BillingDao {
    Optional<Invoice> findById(int invoiceId);
    Optional<Invoice> findByNumber(String invoiceNumber);
    List<Invoice> findAll(String status, String patientSearch);
    int insertInvoice(Invoice invoice, Connection conn);
    int insertItem(InvoiceItem item, Connection conn);
    List<InvoiceItem> findItemsByInvoiceId(int invoiceId);

    int insertPayment(Payment payment, Connection conn);
    List<Payment> findPaymentsByInvoiceId(int invoiceId);
    boolean updateInvoiceBalances(int invoiceId, BigDecimal paidAmount, BigDecimal balanceAmount, String status, Connection conn);

    BigDecimal getTodayRevenue();
    BigDecimal getMonthlyRevenue();
    BigDecimal getTotalOutstandingBalance();
    int countUnpaidInvoices();
    String generateNextInvoiceNumber();
    String generateNextPaymentNumber();
}
