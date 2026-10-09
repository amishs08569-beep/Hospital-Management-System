package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Admission;
import com.hospital.model.Bed;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.AdmissionDialog;
import com.hospital.ui.dialogs.DischargeDialog;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for inpatient admissions, bed allocation, and discharges.
 */
public class AdmissionPanel extends JPanel {

    private final UserSession session;
    private final JTabbedPane tabbedPane;

    // Inpatient Admissions
    private final JComboBox<String> cmbStatusFilter;
    private final JTable admissionTable;
    private final DefaultTableModel admissionModel;
    private final List<Admission> currentAdmissions = new ArrayList<>();

    // Beds & Wards
    private final JTable bedTable;
    private final DefaultTableModel bedModel;
    private final JLabel lblBedStats;

    public AdmissionPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FONT_HEADER);

        // --- TAB 1: ADMISSIONS ---
        JPanel pnlAdm = new JPanel(new BorderLayout(0, 12));
        pnlAdm.setBackground(UIHelper.BACKGROUND);
        pnlAdm.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel admTop = new JPanel(new BorderLayout());
        admTop.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL ADMISSIONS", "ADMITTED", "DISCHARGED"});
        cmbStatusFilter.setPreferredSize(new Dimension(160, 36));
        ModernButton btnFilter = new ModernButton("Filter", ModernButton.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadAdmissions());
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(btnFilter);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnAdmit = new ModernButton("🏥 Admit Patient", ModernButton.ButtonType.SUCCESS);
        btnAdmit.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_ADMISSIONS));
        btnAdmit.addActionListener(e -> onAdmitPatient());

        ModernButton btnDischarge = new ModernButton("🚪 Discharge Patient", ModernButton.ButtonType.DANGER);
        btnDischarge.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_ADMISSIONS));
        btnDischarge.addActionListener(e -> onDischargePatient());

        actionPanel.add(btnAdmit);
        actionPanel.add(btnDischarge);

        admTop.add(filterPanel, BorderLayout.WEST);
        admTop.add(actionPanel, BorderLayout.EAST);
        pnlAdm.add(admTop, BorderLayout.NORTH);

        String[] admCols = {"Admission #", "Patient Name", "Doctor", "Ward", "Bed #", "Admitted Date", "Discharged Date", "Status"};
        admissionModel = new DefaultTableModel(admCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        admissionTable = new JTable(admissionModel);
        UIHelper.styleTable(admissionTable);
        pnlAdm.add(UIHelper.createModernScrollPane(admissionTable), BorderLayout.CENTER);

        // --- TAB 2: WARDS & BEDS ---
        JPanel pnlBeds = new JPanel(new BorderLayout(0, 12));
        pnlBeds.setBackground(UIHelper.BACKGROUND);
        pnlBeds.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel bedTop = new JPanel(new BorderLayout());
        bedTop.setOpaque(false);
        lblBedStats = new JLabel("Loading bed statistics...");
        lblBedStats.setFont(UIHelper.FONT_HEADER);
        lblBedStats.setForeground(UIHelper.TEXT_DARK);
        ModernButton btnRefreshBeds = new ModernButton("🔄 Refresh Beds", ModernButton.ButtonType.SECONDARY);
        btnRefreshBeds.addActionListener(e -> loadBeds());
        bedTop.add(lblBedStats, BorderLayout.WEST);
        bedTop.add(btnRefreshBeds, BorderLayout.EAST);
        pnlBeds.add(bedTop, BorderLayout.NORTH);

        String[] bedCols = {"Ward Name", "Bed Number", "Bed Type", "Daily Rate", "Status"};
        bedModel = new DefaultTableModel(bedCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bedTable = new JTable(bedModel);
        UIHelper.styleTable(bedTable);
        pnlBeds.add(UIHelper.createModernScrollPane(bedTable), BorderLayout.CENTER);

        tabbedPane.addTab("Inpatient Admissions", pnlAdm);
        tabbedPane.addTab("Wards & Bed Availability", pnlBeds);

        add(tabbedPane, BorderLayout.CENTER);

        loadAdmissions();
        loadBeds();
    }

    public void loadAdmissions() {
        String sel = (String) cmbStatusFilter.getSelectedItem();
        String filter = "ALL ADMISSIONS".equals(sel) ? null : sel;

        AppController.executeAsync(
                () -> AppController.getInstance().getAdmissionService().listAdmissions(filter, null),
                list -> {
                    currentAdmissions.clear();
                    currentAdmissions.addAll(list);
                    admissionModel.setRowCount(0);
                    for (Admission a : list) {
                        admissionModel.addRow(new Object[]{
                                a.getAdmissionNumber(),
                                a.getPatientName(),
                                a.getDoctorName(),
                                a.getWardName(),
                                a.getBedNumber() != null ? a.getBedNumber() : "-",
                                DateUtil.formatDateTime(a.getAdmissionDate()),
                                a.getDischargeDate() != null ? DateUtil.formatDateTime(a.getDischargeDate()) : "-",
                                a.getStatus()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load admissions: " + error.getMessage())
        );
    }

    public void loadBeds() {
        AppController.executeAsync(
                () -> {
                    // List beds from all wards (or ward 0 for all)
                    return AppController.getInstance().getAdmissionService().listBedsByWard(0);
                },
                beds -> {
                    bedModel.setRowCount(0);
                    int total = beds.size();
                    int avail = 0;
                    for (Bed b : beds) {
                        if ("AVAILABLE".equalsIgnoreCase(b.getStatus())) avail++;
                        bedModel.addRow(new Object[]{
                                b.getWardName(),
                                b.getBedNumber(),
                                b.getBedType(),
                                CurrencyUtil.format(b.getDailyRate()),
                                b.getStatus()
                        });
                    }
                    lblBedStats.setText("Hospital Bed Capacity: " + total + " Total Beds | " + avail + " Available | " + (total - avail) + " Occupied / Maintenance");
                },
                error -> NotificationToast.showError(this, "Failed to load beds: " + error.getMessage())
        );
    }

    private Admission getSelectedAdmission() {
        int r = admissionTable.getSelectedRow();
        if (r < 0 || r >= currentAdmissions.size()) {
            NotificationToast.showWarning(this, "Please select an admission record first.");
            return null;
        }
        return currentAdmissions.get(r);
    }

    private void onAdmitPatient() {
        AdmissionDialog d = new AdmissionDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            Admission adm = d.getAdmission();
            AppController.executeAsync(
                    () -> AppController.getInstance().getAdmissionService().admitPatient(adm),
                    res -> {
                        NotificationToast.showSuccess(this, "Patient admitted! Case: " + res.getAdmissionNumber());
                        loadAdmissions();
                        loadBeds();
                    },
                    error -> NotificationToast.showError(this, "Admission failed: " + error.getMessage())
            );
        }
    }

    private void onDischargePatient() {
        Admission sel = getSelectedAdmission();
        if (sel == null) return;

        if (!"ADMITTED".equalsIgnoreCase(sel.getStatus())) {
            NotificationToast.showWarning(this, "This patient is already discharged.");
            return;
        }

        DischargeDialog d = new DischargeDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isConfirmed()) {
            AppController.executeAsync(
                    () -> AppController.getInstance().getAdmissionService().dischargePatient(
                            sel.getAdmissionId(),
                            LocalDateTime.now(),
                            d.getDischargeSummary()
                    ),
                    success -> {
                        NotificationToast.showSuccess(this, "Patient discharged successfully. Bed released.");
                        loadAdmissions();
                        loadBeds();
                    },
                    error -> NotificationToast.showError(this, "Discharge failed: " + error.getMessage())
            );
        }
    }
}
