package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Invoice;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.InvoiceDialog;
import com.hospital.ui.dialogs.PaymentDialog;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;
import com.hospital.util.PdfGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for patient invoices, payments, revenue tracking, and PDF receipts.
 */
public class BillingPanel extends JPanel {

    private final UserSession session;
    private final PlaceholderTextField txtSearch;
    private final JComboBox<String> cmbStatusFilter;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<Invoice> currentInvoices = new ArrayList<>();

    public BillingPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        txtSearch = new PlaceholderTextField("Search Patient Name or ID...", 20);
        txtSearch.setPreferredSize(new Dimension(220, 36));

        cmbStatusFilter = new JComboBox<>(new String[]{"ALL INVOICES", "UNPAID", "PARTIAL", "PAID"});
        cmbStatusFilter.setPreferredSize(new Dimension(140, 36));

        ModernButton btnFilter = new ModernButton("Filter", ModernButton.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadInvoices());

        ModernButton btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            loadInvoices();
        });

        filterPanel.add(txtSearch);
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("➕ Create Invoice", ModernButton.ButtonType.SUCCESS);
        btnNew.setEnabled(session.hasPermission(RolePermission.Permission.CREATE_INVOICE));
        btnNew.addActionListener(e -> onNewInvoice());

        ModernButton btnPay = new ModernButton("💳 Record Payment", ModernButton.ButtonType.PRIMARY);
        btnPay.setEnabled(session.hasPermission(RolePermission.Permission.COLLECT_PAYMENT));
        btnPay.addActionListener(e -> onRecordPayment());

        ModernButton btnPrint = new ModernButton("📄 Print / PDF Receipt", ModernButton.ButtonType.SECONDARY);
        btnPrint.addActionListener(e -> onPrintReceipt());

        actionPanel.add(btnNew);
        actionPanel.add(btnPay);
        actionPanel.add(btnPrint);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Invoice #", "Issue Date", "Patient Name", "Total Bill", "Paid", "Balance Due", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadInvoices();
    }

    public void loadInvoices() {
        String query = txtSearch.getText().trim();
        String sel = (String) cmbStatusFilter.getSelectedItem();
        String status = "ALL INVOICES".equals(sel) ? null : sel;

        AppController.executeAsync(
                () -> AppController.getInstance().getBillingService().listInvoices(status, query.isEmpty() ? null : query),
                list -> {
                    currentInvoices.clear();
                    currentInvoices.addAll(list);
                    tableModel.setRowCount(0);
                    for (Invoice inv : list) {
                        tableModel.addRow(new Object[]{
                                inv.getInvoiceNumber(),
                                DateUtil.formatDate(inv.getIssueDate()),
                                inv.getPatientName(),
                                CurrencyUtil.format(inv.getTotalAmount()),
                                CurrencyUtil.format(inv.getPaidAmount()),
                                CurrencyUtil.format(inv.getBalanceAmount()),
                                inv.getPaymentStatus()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load invoices: " + error.getMessage())
        );
    }

    private Invoice getSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= currentInvoices.size()) {
            NotificationToast.showWarning(this, "Please select an invoice from the table first.");
            return null;
        }
        return currentInvoices.get(r);
    }

    private void onNewInvoice() {
        InvoiceDialog d = new InvoiceDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            Invoice inv = d.getInvoice();
            AppController.executeAsync(
                    () -> AppController.getInstance().getBillingService().createInvoice(inv),
                    res -> {
                        NotificationToast.showSuccess(this, "Invoice created successfully! ID: " + res.getInvoiceNumber());
                        loadInvoices();
                    },
                    error -> NotificationToast.showError(this, "Failed to create invoice: " + error.getMessage())
            );
        }
    }

    private void onRecordPayment() {
        Invoice sel = getSelected();
        if (sel == null) return;

        if ("PAID".equalsIgnoreCase(sel.getPaymentStatus())) {
            NotificationToast.showInfo(this, "This invoice is already fully paid.");
            return;
        }

        PaymentDialog d = new PaymentDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isSaved()) {
            AppController.executeAsync(
                    () -> AppController.getInstance().getBillingService().recordPayment(
                            sel.getInvoiceId(),
                            d.getPaidAmount(),
                            d.getPaymentMethod(),
                            d.getTxnRef(),
                            d.getNotes()
                    ),
                    payment -> {
                        NotificationToast.showSuccess(this, "Payment of " + CurrencyUtil.format(payment.getAmount()) + " recorded! Receipt #" + payment.getReceiptNumber());
                        loadInvoices();
                    },
                    error -> NotificationToast.showError(this, "Payment recording failed: " + error.getMessage())
            );
        }
    }

    private void onPrintReceipt() {
        Invoice sel = getSelected();
        if (sel == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Invoice_" + sel.getInvoiceNumber() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            AppController.executeAsync(
                    () -> {
                        // Fetch fresh invoice with items
                        Invoice fresh = AppController.getInstance().getBillingService().getInvoiceById(sel.getInvoiceId()).orElse(sel);
                        PdfGenerator.generateInvoicePdf(fresh, target);
                        return target;
                    },
                    file -> {
                        NotificationToast.showSuccess(this, "Invoice PDF saved at: " + file.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        } catch (Exception ignored) {}
                    },
                    error -> NotificationToast.showError(this, "PDF generation failed: " + error.getMessage())
            );
        }
    }
}
