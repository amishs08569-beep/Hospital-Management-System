package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.*;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 360-degree comprehensive medical profile for a patient.
 */
public class PatientProfileDialog extends JDialog {

    public PatientProfileDialog(Window owner, Patient p) {
        super(owner, "Comprehensive Patient Profile: " + p.getFullName() + " (" + p.getPatientCode() + ")", ModalityType.APPLICATION_MODAL);
        setSize(850, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        // Header info banner
        JPanel banner = new JPanel(new GridLayout(2, 3, 10, 8));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));

        banner.add(createInfoLabel("Patient Code", p.getPatientCode()));
        banner.add(createInfoLabel("Full Name", p.getFullName()));
        banner.add(createInfoLabel("Date of Birth", DateUtil.formatDate(p.getDateOfBirth())));
        banner.add(createInfoLabel("Gender / Blood", p.getGender() + " / " + p.getBloodGroup()));
        banner.add(createInfoLabel("Contact", p.getContactNumber()));
        banner.add(createInfoLabel("Status", p.isActive() ? "ACTIVE" : "INACTIVE"));

        add(banner, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_HEADER);

        // Tab 1: Clinical Records
        DefaultTableModel medModel = new DefaultTableModel(new String[]{"Record #", "Date", "Doctor", "Diagnosis", "Follow-up"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable medTable = new JTable(medModel);
        UIHelper.styleTable(medTable);
        tabs.addTab("Medical Records", UIHelper.createModernScrollPane(medTable));

        // Tab 2: Prescriptions
        DefaultTableModel rxModel = new DefaultTableModel(new String[]{"Prescription #", "Date", "Doctor", "Diagnosis", "Dispensed?"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable rxTable = new JTable(rxModel);
        UIHelper.styleTable(rxTable);
        tabs.addTab("Prescriptions", UIHelper.createModernScrollPane(rxTable));

        // Tab 3: Lab Tests
        DefaultTableModel labModel = new DefaultTableModel(new String[]{"Order #", "Date", "Doctor", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable labTable = new JTable(labModel);
        UIHelper.styleTable(labTable);
        tabs.addTab("Laboratory Orders", UIHelper.createModernScrollPane(labTable));

        // Tab 4: Billing History
        DefaultTableModel billModel = new DefaultTableModel(new String[]{"Invoice #", "Date", "Total", "Paid", "Balance", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable billTable = new JTable(billModel);
        UIHelper.styleTable(billTable);
        tabs.addTab("Invoices & Billing", UIHelper.createModernScrollPane(billTable));

        add(tabs, BorderLayout.CENTER);

        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        btnPanel.setBackground(Color.WHITE);
        ModernButton btnClose = new ModernButton("Close Profile", ModernButton.ButtonType.SECONDARY);
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        // Load data async
        AppController.executeAsync(
                () -> {
                    List<MedicalRecord> recs = AppController.getInstance().getMedicalRecordService().getPatientHistory(p.getPatientId());
                    List<Prescription> rxs = AppController.getInstance().getPrescriptionService().getPatientPrescriptions(p.getPatientId());
                    List<LabOrder> labs = AppController.getInstance().getLabService().listOrders(null, p.getPatientId());
                    List<Invoice> invs = AppController.getInstance().getBillingService().listInvoices(null, p.getPatientCode());
                    return new Object[]{recs, rxs, labs, invs};
                },
                result -> {
                    Object[] data = (Object[]) result;
                    @SuppressWarnings("unchecked") List<MedicalRecord> recs = (List<MedicalRecord>) data[0];
                    @SuppressWarnings("unchecked") List<Prescription> rxs = (List<Prescription>) data[1];
                    @SuppressWarnings("unchecked") List<LabOrder> labs = (List<LabOrder>) data[2];
                    @SuppressWarnings("unchecked") List<Invoice> invs = (List<Invoice>) data[3];

                    for (MedicalRecord r : recs) {
                        medModel.addRow(new Object[]{r.getRecordNumber(), DateUtil.formatDate(r.getRecordDate()), r.getDoctorName(), r.getDiagnosis(), DateUtil.formatDate(r.getFollowUpDate())});
                    }
                    for (Prescription rx : rxs) {
                        rxModel.addRow(new Object[]{rx.getPrescriptionNumber(), DateUtil.formatDate(rx.getPrescriptionDate()), rx.getDoctorName(), rx.getDiagnosis(), rx.isDispensed() ? "YES" : "NO"});
                    }
                    for (LabOrder l : labs) {
                        labModel.addRow(new Object[]{l.getOrderNumber(), DateUtil.formatDateTime(l.getOrderDate()), l.getDoctorName(), l.getStatus()});
                    }
                    for (Invoice inv : invs) {
                        billModel.addRow(new Object[]{inv.getInvoiceNumber(), DateUtil.formatDate(inv.getIssueDate()), CurrencyUtil.format(inv.getTotalAmount()), CurrencyUtil.format(inv.getPaidAmount()), CurrencyUtil.format(inv.getBalanceAmount()), inv.getPaymentStatus()});
                    }
                },
                null
        );
    }

    private JPanel createInfoLabel(String title, String val) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UIHelper.FONT_SMALL);
        t.setForeground(UIHelper.TEXT_MUTED);

        JLabel v = new JLabel(val != null ? val : "-");
        v.setFont(UIHelper.FONT_BOLD);
        v.setForeground(UIHelper.TEXT_DARK);

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
