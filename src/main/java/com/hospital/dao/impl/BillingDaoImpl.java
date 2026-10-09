package com.hospital.dao.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.BillingDao;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillingDaoImpl implements BillingDao {

    private static final Logger logger = LoggerFactory.getLogger(BillingDaoImpl.class);

    @Override
    public Optional<Invoice> findById(int invoiceId) {
        String sql = "SELECT inv.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "adm.admission_number, u.username AS created_by_username " +
                "FROM invoices inv " +
                "JOIN patients p ON inv.patient_id = p.patient_id " +
                "LEFT JOIN admissions adm ON inv.admission_id = adm.admission_id " +
                "LEFT JOIN users u ON inv.created_by_user_id = u.user_id " +
                "WHERE inv.invoice_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    inv.setItems(findItemsByInvoiceId(invoiceId));
                    inv.setPayments(findPaymentsByInvoiceId(invoiceId));
                    return Optional.of(inv);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding invoice {}: {}", invoiceId, e.getMessage());
            throw new DatabaseException("Failed to find invoice: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Invoice> findByNumber(String invoiceNumber) {
        String sql = "SELECT inv.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "adm.admission_number, u.username AS created_by_username " +
                "FROM invoices inv " +
                "JOIN patients p ON inv.patient_id = p.patient_id " +
                "LEFT JOIN admissions adm ON inv.admission_id = adm.admission_id " +
                "LEFT JOIN users u ON inv.created_by_user_id = u.user_id " +
                "WHERE UPPER(inv.invoice_number) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    inv.setItems(findItemsByInvoiceId(inv.getInvoiceId()));
                    inv.setPayments(findPaymentsByInvoiceId(inv.getInvoiceId()));
                    return Optional.of(inv);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding invoice by number {}: {}", invoiceNumber, e.getMessage());
            throw new DatabaseException("Failed to find invoice: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Invoice> findAll(String status, String patientSearch) {
        List<Invoice> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT inv.*, p.first_name, p.last_name, p.patient_code, p.contact_number AS patient_phone, " +
                "adm.admission_number, u.username AS created_by_username " +
                "FROM invoices inv " +
                "JOIN patients p ON inv.patient_id = p.patient_id " +
                "LEFT JOIN admissions adm ON inv.admission_id = adm.admission_id " +
                "LEFT JOIN users u ON inv.created_by_user_id = u.user_id WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND inv.payment_status = ? ");
        }
        if (patientSearch != null && !patientSearch.trim().isEmpty()) {
            sql.append("AND (inv.invoice_number LIKE ? OR p.first_name LIKE ? OR p.last_name LIKE ? OR p.patient_code LIKE ?) ");
        }
        sql.append("ORDER BY inv.invoice_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                ps.setString(idx++, status.trim().toUpperCase());
            }
            if (patientSearch != null && !patientSearch.trim().isEmpty()) {
                String pattern = "%" + patientSearch.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving invoices: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve invoices: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insertInvoice(Invoice invoice, Connection conn) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().trim().isEmpty()) {
            invoice.setInvoiceNumber(generateNextInvoiceNumber());
        }
        String sql = "INSERT INTO invoices (invoice_number, patient_id, admission_id, issue_date, subtotal, " +
                "discount_percent, discount_amount, tax_percent, tax_amount, total_amount, paid_amount, balance_amount, " +
                "payment_status, notes, created_by_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, invoice.getInvoiceNumber());
                ps.setInt(2, invoice.getPatientId());
                if (invoice.getAdmissionId() != null) ps.setInt(3, invoice.getAdmissionId()); else ps.setNull(3, Types.INTEGER);
                ps.setDate(4, Date.valueOf(invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now()));
                ps.setBigDecimal(5, invoice.getSubtotal());
                ps.setBigDecimal(6, invoice.getDiscountPercent());
                ps.setBigDecimal(7, invoice.getDiscountAmount());
                ps.setBigDecimal(8, invoice.getTaxPercent());
                ps.setBigDecimal(9, invoice.getTaxAmount());
                ps.setBigDecimal(10, invoice.getTotalAmount());
                ps.setBigDecimal(11, invoice.getPaidAmount());
                ps.setBigDecimal(12, invoice.getBalanceAmount());
                ps.setString(13, invoice.getPaymentStatus());
                ps.setString(14, invoice.getNotes());
                if (invoice.getCreatedByUserId() != null) ps.setInt(15, invoice.getCreatedByUserId()); else ps.setNull(15, Types.INTEGER);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        invoice.setInvoiceId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating invoice: {}", e.getMessage());
            throw new DatabaseException("Failed to save invoice: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public int insertItem(InvoiceItem item, Connection conn) {
        String sql = "INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, total_price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, item.getInvoiceId());
                ps.setString(2, item.getItemType());
                ps.setString(3, item.getDescription());
                ps.setInt(4, item.getQuantity());
                ps.setBigDecimal(5, item.getUnitPrice());
                ps.setBigDecimal(6, item.getTotalPrice());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        item.setItemId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting invoice item: {}", e.getMessage());
            throw new DatabaseException("Failed to save invoice item: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public List<InvoiceItem> findItemsByInvoiceId(int invoiceId) {
        List<InvoiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY item_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setInvoiceId(rs.getInt("invoice_id"));
                    item.setItemType(rs.getString("item_type"));
                    item.setDescription(rs.getString("description"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding items for invoice {}: {}", invoiceId, e.getMessage());
            throw new DatabaseException("Failed to find invoice items: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int insertPayment(Payment payment, Connection conn) {
        if (payment.getPaymentNumber() == null || payment.getPaymentNumber().trim().isEmpty()) {
            payment.setPaymentNumber(generateNextPaymentNumber());
        }
        String sql = "INSERT INTO payments (invoice_id, payment_number, payment_date, amount, payment_method, transaction_ref, notes, received_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, payment.getInvoiceId());
                ps.setString(2, payment.getPaymentNumber());
                ps.setTimestamp(3, Timestamp.valueOf(payment.getPaymentDate() != null ? payment.getPaymentDate() : java.time.LocalDateTime.now()));
                ps.setBigDecimal(4, payment.getAmount());
                ps.setString(5, payment.getPaymentMethod());
                ps.setString(6, payment.getTransactionRef());
                ps.setString(7, payment.getNotes());
                if (payment.getReceivedByUserId() != null) ps.setInt(8, payment.getReceivedByUserId()); else ps.setNull(8, Types.INTEGER);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        payment.setPaymentId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating payment: {}", e.getMessage());
            throw new DatabaseException("Failed to record payment: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return 0;
    }

    @Override
    public List<Payment> findPaymentsByInvoiceId(int invoiceId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username AS received_by_username FROM payments p " +
                "LEFT JOIN users u ON p.received_by_user_id = u.user_id " +
                "WHERE p.invoice_id = ? ORDER BY p.payment_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment pay = new Payment();
                    pay.setPaymentId(rs.getInt("payment_id"));
                    pay.setInvoiceId(rs.getInt("invoice_id"));
                    pay.setPaymentNumber(rs.getString("payment_number"));
                    Timestamp pd = rs.getTimestamp("payment_date");
                    if (pd != null) pay.setPaymentDate(pd.toLocalDateTime());
                    pay.setAmount(rs.getBigDecimal("amount"));
                    pay.setPaymentMethod(rs.getString("payment_method"));
                    pay.setTransactionRef(rs.getString("transaction_ref"));
                    pay.setNotes(rs.getString("notes"));
                    int uid = rs.getInt("received_by_user_id");
                    if (!rs.wasNull()) pay.setReceivedByUserId(uid);
                    pay.setReceivedByUsername(rs.getString("received_by_username"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) pay.setCreatedAt(cat.toLocalDateTime());
                    list.add(pay);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payments for invoice {}: {}", invoiceId, e.getMessage());
            throw new DatabaseException("Failed to find payments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean updateInvoiceBalances(int invoiceId, BigDecimal paidAmount, BigDecimal balanceAmount, String status, Connection conn) {
        String sql = "UPDATE invoices SET paid_amount = ?, balance_amount = ?, payment_status = ? WHERE invoice_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DatabaseConfig.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, paidAmount);
                ps.setBigDecimal(2, balanceAmount);
                ps.setString(3, status);
                ps.setInt(4, invoiceId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating invoice {} balance: {}", invoiceId, e.getMessage());
            throw new DatabaseException("Failed to update invoice balances: " + e.getMessage(), e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public BigDecimal getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_date) = CURDATE()";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating today's revenue: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE MONTH(payment_date) = MONTH(CURDATE()) AND YEAR(payment_date) = YEAR(CURDATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating monthly revenue: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalOutstandingBalance() {
        String sql = "SELECT COALESCE(SUM(balance_amount), 0) FROM invoices WHERE payment_status != 'PAID'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating outstanding balances: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int countUnpaidInvoices() {
        String sql = "SELECT COUNT(*) FROM invoices WHERE payment_status IN ('UNPAID', 'PARTIAL')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting unpaid invoices: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateNextInvoiceNumber() {
        String sql = "SELECT COALESCE(MAX(invoice_id), 0) + 1 FROM invoices";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("INV-%d-%03d", LocalDate.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating invoice number: {}", e.getMessage());
        }
        return "INV-" + System.currentTimeMillis() % 10000;
    }

    @Override
    public String generateNextPaymentNumber() {
        String sql = "SELECT COALESCE(MAX(payment_id), 0) + 1 FROM payments";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("PAY-%d-%03d", LocalDate.now().getYear(), rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error generating payment number: {}", e.getMessage());
        }
        return "PAY-" + System.currentTimeMillis() % 10000;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setPatientId(rs.getInt("patient_id"));
        inv.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
        inv.setPatientCode(rs.getString("patient_code"));
        inv.setPatientPhone(rs.getString("patient_phone"));
        int admId = rs.getInt("admission_id");
        if (!rs.wasNull()) inv.setAdmissionId(admId);
        inv.setAdmissionNumber(rs.getString("admission_number"));
        Date idt = rs.getDate("issue_date");
        if (idt != null) inv.setIssueDate(idt.toLocalDate());
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setTaxPercent(rs.getBigDecimal("tax_percent"));
        inv.setTaxAmount(rs.getBigDecimal("tax_amount"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setBalanceAmount(rs.getBigDecimal("balance_amount"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setNotes(rs.getString("notes"));
        int uid = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) inv.setCreatedByUserId(uid);
        inv.setCreatedByUsername(rs.getString("created_by_username"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) inv.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) inv.setUpdatedAt(uat.toLocalDateTime());
        return inv;
    }
}
