package com.hospital.service;

import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Payment;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillingService {
    Invoice createInvoice(Invoice invoice);
    Payment recordPayment(int invoiceId, BigDecimal amount, String paymentMethod, String txnRef, String notes);
    Optional<Invoice> getInvoiceById(int invoiceId);
    Optional<Invoice> getInvoiceByNumber(String invoiceNumber);
    List<Invoice> listInvoices(String status, String patientSearch);
    File exportInvoicePdf(int invoiceId, File destinationDirectory) throws Exception;

    BigDecimal getTodayRevenue();
    BigDecimal getMonthlyRevenue();
    BigDecimal getTotalOutstandingBalance();
    int getUnpaidInvoicesCount();
}
