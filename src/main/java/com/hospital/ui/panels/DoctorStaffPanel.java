package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Doctor;
import com.hospital.model.Staff;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.DoctorDialog;
import com.hospital.ui.dialogs.DoctorScheduleDialog;
import com.hospital.ui.dialogs.StaffDialog;
import com.hospital.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for Doctors, Staff, Schedules, and Departments.
 */
public class DoctorStaffPanel extends JPanel {

    private final UserSession session;
    private final JTabbedPane tabbedPane;

    // Doctor Tab
    private final PlaceholderTextField txtDoctorSearch;
    private final JTable doctorTable;
    private final DefaultTableModel doctorModel;
    private final List<Doctor> currentDoctors = new ArrayList<>();

    // Staff Tab
    private final PlaceholderTextField txtStaffSearch;
    private final JTable staffTable;
    private final DefaultTableModel staffModel;
    private final List<Staff> currentStaff = new ArrayList<>();

    public DoctorStaffPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FONT_HEADER);

        // --- DOCTORS TAB ---
        JPanel pnlDoctors = new JPanel(new BorderLayout(0, 12));
        pnlDoctors.setBackground(UIHelper.BACKGROUND);
        pnlDoctors.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel docTop = new JPanel(new BorderLayout());
        docTop.setOpaque(false);

        JPanel docSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        docSearch.setOpaque(false);
        txtDoctorSearch = new PlaceholderTextField("Search Doctor Name, Department, Specialization...", 24);
        txtDoctorSearch.setPreferredSize(new Dimension(280, 36));
        ModernButton btnDocSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnDocSearch.addActionListener(e -> loadDoctors());
        docSearch.add(txtDoctorSearch);
        docSearch.add(btnDocSearch);

        JPanel docActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        docActions.setOpaque(false);
        ModernButton btnAddDoc = new ModernButton("➕ Add Doctor", ModernButton.ButtonType.SUCCESS);
        btnAddDoc.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_DOCTORS));
        btnAddDoc.addActionListener(e -> onAddDoctor());

        ModernButton btnEditDoc = new ModernButton("✏️ Edit Doctor", ModernButton.ButtonType.SECONDARY);
        btnEditDoc.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_DOCTORS));
        btnEditDoc.addActionListener(e -> onEditDoctor());

        ModernButton btnSchedule = new ModernButton("📅 Manage Shifts", ModernButton.ButtonType.PRIMARY);
        btnSchedule.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_DOCTORS));
        btnSchedule.addActionListener(e -> onManageSchedule());

        ModernButton btnToggleDoc = new ModernButton("Deactivate / Activate", ModernButton.ButtonType.DANGER);
        btnToggleDoc.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_DOCTORS));
        btnToggleDoc.addActionListener(e -> onToggleDoctorStatus());

        docActions.add(btnAddDoc);
        docActions.add(btnEditDoc);
        docActions.add(btnSchedule);
        docActions.add(btnToggleDoc);

        docTop.add(docSearch, BorderLayout.WEST);
        docTop.add(docActions, BorderLayout.EAST);
        pnlDoctors.add(docTop, BorderLayout.NORTH);

        String[] docCols = {"Emp Code", "Full Name", "Department", "Specialization", "Qualification", "Contact", "Fee", "Status"};
        doctorModel = new DefaultTableModel(docCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        doctorTable = new JTable(doctorModel);
        UIHelper.styleTable(doctorTable);
        pnlDoctors.add(UIHelper.createModernScrollPane(doctorTable), BorderLayout.CENTER);

        // --- STAFF TAB ---
        JPanel pnlStaff = new JPanel(new BorderLayout(0, 12));
        pnlStaff.setBackground(UIHelper.BACKGROUND);
        pnlStaff.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel staffTop = new JPanel(new BorderLayout());
        staffTop.setOpaque(false);

        JPanel staffSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        staffSearch.setOpaque(false);
        txtStaffSearch = new PlaceholderTextField("Search Staff Name, Role, Department...", 24);
        txtStaffSearch.setPreferredSize(new Dimension(280, 36));
        ModernButton btnStaffSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnStaffSearch.addActionListener(e -> loadStaff());
        staffSearch.add(txtStaffSearch);
        staffSearch.add(btnStaffSearch);

        JPanel staffActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        staffActions.setOpaque(false);
        ModernButton btnAddStaff = new ModernButton("➕ Add Staff", ModernButton.ButtonType.SUCCESS);
        btnAddStaff.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_STAFF));
        btnAddStaff.addActionListener(e -> onAddStaff());

        ModernButton btnEditStaff = new ModernButton("✏️ Edit Staff", ModernButton.ButtonType.SECONDARY);
        btnEditStaff.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_STAFF));
        btnEditStaff.addActionListener(e -> onEditStaff());

        ModernButton btnToggleStaff = new ModernButton("Deactivate / Activate", ModernButton.ButtonType.DANGER);
        btnToggleStaff.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_STAFF));
        btnToggleStaff.addActionListener(e -> onToggleStaffStatus());

        staffActions.add(btnAddStaff);
        staffActions.add(btnEditStaff);
        staffActions.add(btnToggleStaff);

        staffTop.add(staffSearch, BorderLayout.WEST);
        staffTop.add(staffActions, BorderLayout.EAST);
        pnlStaff.add(staffTop, BorderLayout.NORTH);

        String[] staffCols = {"Emp Code", "Full Name", "Department", "Role Title", "Qualification", "Contact", "Status"};
        staffModel = new DefaultTableModel(staffCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = new JTable(staffModel);
        UIHelper.styleTable(staffTable);
        pnlStaff.add(UIHelper.createModernScrollPane(staffTable), BorderLayout.CENTER);

        tabbedPane.addTab("Medical Doctors", pnlDoctors);
        tabbedPane.addTab("Clinical & Hospital Staff", pnlStaff);

        add(tabbedPane, BorderLayout.CENTER);

        loadDoctors();
        loadStaff();
    }

    public void loadDoctors() {
        String query = txtDoctorSearch.getText().trim();
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().listDoctors(query.isEmpty() ? null : query, null, false),
                list -> {
                    currentDoctors.clear();
                    currentDoctors.addAll(list);
                    doctorModel.setRowCount(0);
                    for (Doctor d : list) {
                        doctorModel.addRow(new Object[]{
                                d.getDoctorCode(),
                                d.getFullName(),
                                d.getDepartmentName() != null ? d.getDepartmentName() : "-",
                                d.getSpecialization(),
                                d.getQualification(),
                                d.getContactNumber(),
                                CurrencyUtil.format(d.getConsultationFee()),
                                d.getEmploymentStatus()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load doctors: " + error.getMessage())
        );
    }

    public void loadStaff() {
        String query = txtStaffSearch.getText().trim();
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().listStaff(query.isEmpty() ? null : query, null),
                list -> {
                    currentStaff.clear();
                    currentStaff.addAll(list);
                    staffModel.setRowCount(0);
                    for (Staff s : list) {
                        staffModel.addRow(new Object[]{
                                s.getStaffCode(),
                                s.getFullName(),
                                s.getDepartmentName() != null ? s.getDepartmentName() : "-",
                                s.getRoleTitle(),
                                s.getQualification(),
                                s.getContactNumber(),
                                s.getEmploymentStatus()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load staff: " + error.getMessage())
        );
    }

    private Doctor getSelectedDoctor() {
        int r = doctorTable.getSelectedRow();
        if (r < 0 || r >= currentDoctors.size()) {
            NotificationToast.showWarning(this, "Please select a doctor first.");
            return null;
        }
        return currentDoctors.get(r);
    }

    private Staff getSelectedStaff() {
        int r = staffTable.getSelectedRow();
        if (r < 0 || r >= currentStaff.size()) {
            NotificationToast.showWarning(this, "Please select a staff member first.");
            return null;
        }
        return currentStaff.get(r);
    }

    private void onAddDoctor() {
        DoctorDialog d = new DoctorDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) {
            Doctor doc = d.getDoctor();
            AppController.executeAsync(
                    () -> AppController.getInstance().getDoctorStaffService().registerDoctor(doc),
                    res -> {
                        NotificationToast.showSuccess(this, "Doctor registered with code: " + res.getDoctorCode());
                        loadDoctors();
                    },
                    error -> NotificationToast.showError(this, "Failed to register doctor: " + error.getMessage())
            );
        }
    }

    private void onEditDoctor() {
        Doctor sel = getSelectedDoctor();
        if (sel == null) return;
        DoctorDialog d = new DoctorDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isSaved()) {
            Doctor doc = d.getDoctor();
            AppController.executeAsync(
                    () -> AppController.getInstance().getDoctorStaffService().updateDoctor(doc),
                    res -> {
                        NotificationToast.showSuccess(this, "Doctor records updated.");
                        loadDoctors();
                    },
                    error -> NotificationToast.showError(this, "Failed to update doctor: " + error.getMessage())
            );
        }
    }

    private void onManageSchedule() {
        Doctor sel = getSelectedDoctor();
        if (sel == null) return;
        DoctorScheduleDialog d = new DoctorScheduleDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
    }

    private void onToggleDoctorStatus() {
        Doctor sel = getSelectedDoctor();
        if (sel == null) return;
        String newStatus = "ACTIVE".equalsIgnoreCase(sel.getEmploymentStatus()) ? "INACTIVE" : "ACTIVE";
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().setDoctorStatus(sel.getDoctorId(), newStatus),
                res -> {
                    NotificationToast.showSuccess(this, "Doctor status updated to " + newStatus);
                    loadDoctors();
                },
                error -> NotificationToast.showError(this, "Failed to update status: " + error.getMessage())
        );
    }

    private void onAddStaff() {
        StaffDialog d = new StaffDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) {
            Staff s = d.getStaff();
            AppController.executeAsync(
                    () -> AppController.getInstance().getDoctorStaffService().registerStaff(s),
                    res -> {
                        NotificationToast.showSuccess(this, "Staff registered with code: " + res.getStaffCode());
                        loadStaff();
                    },
                    error -> NotificationToast.showError(this, "Failed to register staff: " + error.getMessage())
            );
        }
    }

    private void onEditStaff() {
        Staff sel = getSelectedStaff();
        if (sel == null) return;
        StaffDialog d = new StaffDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isSaved()) {
            Staff s = d.getStaff();
            AppController.executeAsync(
                    () -> AppController.getInstance().getDoctorStaffService().updateStaff(s),
                    res -> {
                        NotificationToast.showSuccess(this, "Staff records updated.");
                        loadStaff();
                    },
                    error -> NotificationToast.showError(this, "Failed to update staff: " + error.getMessage())
            );
        }
    }

    private void onToggleStaffStatus() {
        Staff sel = getSelectedStaff();
        if (sel == null) return;
        String newStatus = "ACTIVE".equalsIgnoreCase(sel.getEmploymentStatus()) ? "INACTIVE" : "ACTIVE";
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().setStaffStatus(sel.getStaffId(), newStatus),
                res -> {
                    NotificationToast.showSuccess(this, "Staff status updated to " + newStatus);
                    loadStaff();
                },
                error -> NotificationToast.showError(this, "Failed to update status: " + error.getMessage())
        );
    }
}
