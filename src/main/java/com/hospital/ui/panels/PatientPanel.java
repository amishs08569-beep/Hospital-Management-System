package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Patient;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.*;
import com.hospital.ui.dialogs.PatientDialog;
import com.hospital.ui.dialogs.PatientProfileDialog;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for registering, updating, and viewing patients.
 */
public class PatientPanel extends JPanel {

    private final UserSession session;
    private final PlaceholderTextField txtSearch;
    private final JCheckBox chkActiveOnly;
    private final JTable patientTable;
    private final DefaultTableModel tableModel;
    private final List<Patient> currentPatients = new ArrayList<>();

    public PatientPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        txtSearch = new PlaceholderTextField("Search by Patient Name, ID, or Phone...", 24);
        txtSearch.setPreferredSize(new Dimension(280, 36));

        chkActiveOnly = new JCheckBox("Active Records Only", true);
        chkActiveOnly.setOpaque(false);
        chkActiveOnly.setFont(UIHelper.FONT_BODY);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnSearch.addActionListener(e -> loadPatients());

        ModernButton btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            chkActiveOnly.setSelected(true);
            loadPatients();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(chkActiveOnly);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        ModernButton btnRegister = new ModernButton("➕ Register Patient", ModernButton.ButtonType.SUCCESS);
        btnRegister.setEnabled(session.hasPermission(RolePermission.Permission.REGISTER_PATIENT));
        btnRegister.addActionListener(e -> onRegister());

        ModernButton btnEdit = new ModernButton("✏️ Edit", ModernButton.ButtonType.SECONDARY);
        btnEdit.setEnabled(session.hasPermission(RolePermission.Permission.UPDATE_PATIENT));
        btnEdit.addActionListener(e -> onEdit());

        ModernButton btnProfile = new ModernButton("👤 Profile History", ModernButton.ButtonType.PRIMARY);
        btnProfile.addActionListener(e -> onViewProfile());

        ModernButton btnToggleActive = new ModernButton("Deactivate / Activate", ModernButton.ButtonType.DANGER);
        btnToggleActive.setEnabled(session.hasPermission(RolePermission.Permission.DEACTIVATE_PATIENT));
        btnToggleActive.addActionListener(e -> onToggleActive());

        actionsPanel.add(btnRegister);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnProfile);
        actionsPanel.add(btnToggleActive);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(actionsPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Patients Table
        String[] columns = {"Patient Code", "Full Name", "Gender", "Age / DOB", "Contact Number", "Blood Group", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        patientTable = new JTable(tableModel);
        UIHelper.styleTable(patientTable);

        add(UIHelper.createModernScrollPane(patientTable), BorderLayout.CENTER);

        // Load initially
        loadPatients();
    }

    public void loadPatients() {
        String query = txtSearch.getText().trim();
        boolean activeOnly = chkActiveOnly.isSelected();

        AppController.executeAsync(
                () -> AppController.getInstance().getPatientService().searchPatients(query.isEmpty() ? null : query, activeOnly),
                patients -> {
                    currentPatients.clear();
                    currentPatients.addAll(patients);
                    tableModel.setRowCount(0);

                    for (Patient p : patients) {
                        String ageStr = "-";
                        if (p.getDateOfBirth() != null) {
                            int age = Period.between(p.getDateOfBirth(), LocalDate.now()).getYears();
                            ageStr = age + " yrs (" + DateUtil.formatDate(p.getDateOfBirth()) + ")";
                        }

                        tableModel.addRow(new Object[]{
                                p.getPatientCode(),
                                p.getFullName(),
                                p.getGender(),
                                ageStr,
                                p.getContactNumber(),
                                p.getBloodGroup() != null ? p.getBloodGroup() : "-",
                                p.isActive() ? "ACTIVE" : "INACTIVE"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load patients: " + error.getMessage())
        );
    }

    private Patient getSelectedPatient() {
        int row = patientTable.getSelectedRow();
        if (row < 0 || row >= currentPatients.size()) {
            NotificationToast.showWarning(this, "Please select a patient from the table first.");
            return null;
        }
        return currentPatients.get(row);
    }

    private void onRegister() {
        PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Patient p = dialog.getPatient();
            AppController.executeAsync(
                    () -> AppController.getInstance().getPatientService().registerPatient(p),
                    savedPatient -> {
                        NotificationToast.showSuccess(this, "Patient successfully registered with ID: " + savedPatient.getPatientCode());
                        loadPatients();
                    },
                    error -> NotificationToast.showError(this, "Registration failed: " + error.getMessage())
            );
        }
    }

    private void onEdit() {
        Patient selected = getSelectedPatient();
        if (selected == null) return;

        PatientDialog dialog = new PatientDialog(SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Patient updated = dialog.getPatient();
            AppController.executeAsync(
                    () -> AppController.getInstance().getPatientService().updatePatient(updated),
                    success -> {
                        NotificationToast.showSuccess(this, "Patient records updated successfully.");
                        loadPatients();
                    },
                    error -> NotificationToast.showError(this, "Update failed: " + error.getMessage())
            );
        }
    }

    private void onViewProfile() {
        Patient selected = getSelectedPatient();
        if (selected == null) return;
        PatientProfileDialog dialog = new PatientProfileDialog(SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
    }

    private void onToggleActive() {
        Patient selected = getSelectedPatient();
        if (selected == null) return;

        String action = selected.isActive() ? "deactivate" : "activate";
        if (!NotificationToast.confirm(this, "Confirm " + action, "Are you sure you want to " + action + " patient " + selected.getFullName() + "?")) {
            return;
        }

        AppController.executeAsync(
                () -> {
                    if (selected.isActive()) {
                        return AppController.getInstance().getPatientService().deactivatePatient(selected.getPatientId());
                    } else {
                        return AppController.getInstance().getPatientService().activatePatient(selected.getPatientId());
                    }
                },
                success -> {
                    NotificationToast.showSuccess(this, "Patient status updated successfully.");
                    loadPatients();
                },
                error -> NotificationToast.showError(this, "Action failed: " + error.getMessage())
        );
    }
}
