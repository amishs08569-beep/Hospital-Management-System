package com.hospital.service;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Payment;
import com.hospital.service.impl.BillingServiceImpl;
import com.hospital.util.DbInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BillingServiceTest {

    private static BillingService billingService;

    @BeforeAll
    public static void setup() {
        DatabaseConfig.initializeH2();
        DbInitializer.initializeDatabase();
        com.hospital.model.User admin = new com.hospital.model.User();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setRoleName("ADMIN");
        com.hospital.security.SecurityContext.setCurrentSession(new com.hospital.security.UserSession(admin));
        billingService = new BillingServiceImpl();
    }

    @Test
    public void testInvoiceCreationAndFinancialCalculations() {
        Invoice inv = new Invoice();
        inv.setPatientId(1);
        inv.setIssueDate(LocalDate.now());

        List<InvoiceItem> items = new ArrayList<>();
        InvoiceItem item1 = new InvoiceItem();
        item1.setDescription("General Consultation");
        item1.setItemType("CONSULTATION");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("100.00"));
        item1.setTotalPrice(new BigDecimal("100.00"));
        items.add(item1);

        InvoiceItem item2 = new InvoiceItem();
        item2.setDescription("Blood Routine Test");
        item2.setItemType("LAB_TEST");
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setTotalPrice(new BigDecimal("100.00"));
        items.add(item2);

        inv.setItems(items);
        inv.setSubtotal(new BigDecimal("200.00"));
        inv.setDiscountPercent(new BigDecimal("10.00")); // 10% of 200 = 20.00
        inv.setDiscountAmount(new BigDecimal("20.00"));
        inv.setTaxPercent(new BigDecimal("5.00")); // 5% of 180 = 9.00
        inv.setTaxAmount(new BigDecimal("9.00"));
        inv.setTotalAmount(new BigDecimal("189.00"));
        inv.setPaidAmount(BigDecimal.ZERO);
        inv.setBalanceAmount(new BigDecimal("189.00"));
        inv.setPaymentStatus("UNPAID");

        Invoice created = billingService.createInvoice(inv);
        assertNotNull(created.getInvoiceId());
        assertNotNull(created.getInvoiceNumber());
        assertEquals(new BigDecimal("189.00"), created.getTotalAmount());
        assertEquals("UNPAID", created.getPaymentStatus());

        // Process partial payment
        Payment payment1 = billingService.recordPayment(created.getInvoiceId(), new BigDecimal("89.00"), "CASH", "REC-01", "Partial cash");
        assertNotNull(payment1);
        Invoice afterP1 = billingService.getInvoiceById(created.getInvoiceId()).orElseThrow();
        assertEquals("PARTIAL", afterP1.getPaymentStatus());
        assertEquals(new BigDecimal("89.00"), afterP1.getPaidAmount());
        assertEquals(new BigDecimal("100.00"), afterP1.getBalanceAmount());

        // Process remaining balance
        Payment payment2 = billingService.recordPayment(created.getInvoiceId(), new BigDecimal("100.00"), "CREDIT_CARD", "REC-02", "Clear balance");
        assertNotNull(payment2);
        Invoice afterP2 = billingService.getInvoiceById(created.getInvoiceId()).orElseThrow();
        assertEquals("PAID", afterP2.getPaymentStatus());
        assertEquals(new BigDecimal("189.00"), afterP2.getPaidAmount());
        assertEquals(new BigDecimal("0.00"), afterP2.getBalanceAmount());
    }
}
