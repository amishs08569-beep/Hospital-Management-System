package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to add or edit doctor credentials and departments.
 */
public class DoctorDialog extends JDialog {

    private final PlaceholderTextField txtName;
    private final JComboBox<String> cmbDepartment;
    private final PlaceholderTextField txtSpecialization;
    private final PlaceholderTextField txtQualification;
    private final PlaceholderTextField txtPhone;
    private final PlaceholderTextField txtEmail;
    private final PlaceholderTextField txtFee;
    private final JComboBox<String> cmbStatus;

    private final List<Department> departments = new ArrayList<>();
    private Doctor doctor;
    private boolean saved = false;

    public DoctorDialog(Window owner, Doctor existing) {
        super(owner, existing == null ? "Add Medical Doctor" : "Edit Doctor Credentials", ModalityType.APPLICATION_MODAL);
        this.doctor = existing != null ? existing : new Doctor();

        setSize(550, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        txtName = new PlaceholderTextField("Dr. Full Name (e.g. Dr. Sarah Jenkins)");
        cmbDepartment = new JComboBox<>();
        txtSpecialization = new PlaceholderTextField("Specialization (e.g. Cardiology)");
        txtQualification = new PlaceholderTextField("Qualification (e.g. MD, FACC)");
        txtPhone = new PlaceholderTextField("Phone Number");
        txtEmail = new PlaceholderTextField("Email Address");
        txtFee = new PlaceholderTextField("Consultation Fee (e.g. 150.00)");
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ON_LEAVE"});

        // Load departments synchronously or before showing
        try {
            departments.addAll(AppController.getInstance().getDoctorStaffService().listDepartments(true));
            for (Department d : departments) {
                cmbDepartment.addItem(d.getDepartmentName());
            }
        } catch (Exception e) {
            cmbDepartment.addItem("General Medicine");
        }

        content.add(UIHelper.createFormRow("Full Name *", txtName));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Department *", cmbDepartment));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Specialization *", txtSpecialization));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Qualification", txtQualification));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Phone Number *", txtPhone));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Email Address", txtEmail));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Consultation Fee *", txtFee));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Status", cmbStatus));

        if (existing != null) {
            txtName.setText(existing.getFullName());
            txtSpecialization.setText(existing.getSpecialization());
            txtQualification.setText(existing.getQualification());
            txtPhone.setText(existing.getContactNumber());
            txtEmail.setText(existing.getEmail());
            txtFee.setText(existing.getConsultationFee() != null ? existing.getConsultationFee().toString() : "100.00");
            cmbStatus.setSelectedItem(existing.getEmploymentStatus());

            if (existing.getDepartmentName() != null) {
                cmbDepartment.setSelectedItem(existing.getDepartmentName());
            }
        } else {
            txtFee.setText("100.00");
        }

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Doctor", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String spec = txtSpecialization.getText().trim();
        String phone = txtPhone.getText().trim();
        String feeStr = txtFee.getText().trim();

        if (name.isEmpty() || spec.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
            NotificationToast.showError(this, "Please fill in all mandatory fields (*).");
            return;
        }

        BigDecimal fee;
        try {
            fee = new BigDecimal(feeStr);
            if (fee.compareTo(BigDecimal.ZERO) < 0) {
                NotificationToast.showError(this, "Fee cannot be negative.");
                return;
            }
        } catch (Exception e) {
            NotificationToast.showError(this, "Please enter a valid numeric fee.");
            return;
        }

        int deptIdx = cmbDepartment.getSelectedIndex();
        if (deptIdx >= 0 && deptIdx < departments.size()) {
            doctor.setDepartmentId(departments.get(deptIdx).getDepartmentId());
        }

        doctor.setFullName(name);
        doctor.setSpecialization(spec);
        doctor.setQualification(txtQualification.getText().trim());
        doctor.setContactNumber(phone);
        doctor.setEmail(txtEmail.getText().trim());
        doctor.setConsultationFee(fee);
        doctor.setEmploymentStatus((String) cmbStatus.getSelectedItem());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Doctor getDoctor() { return doctor; }
}
