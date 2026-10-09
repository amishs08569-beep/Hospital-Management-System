package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Department;
import com.hospital.model.Staff;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to add or update general hospital staff members.
 */
public class StaffDialog extends JDialog {

    private final PlaceholderTextField txtName;
    private final JComboBox<String> cmbDepartment;
    private final PlaceholderTextField txtRoleTitle;
    private final PlaceholderTextField txtQualification;
    private final PlaceholderTextField txtPhone;
    private final PlaceholderTextField txtEmail;
    private final JComboBox<String> cmbStatus;

    private final List<Department> departments = new ArrayList<>();
    private Staff staff;
    private boolean saved = false;

    public StaffDialog(Window owner, Staff existing) {
        super(owner, existing == null ? "Add Hospital Staff Member" : "Edit Staff Member Details", ModalityType.APPLICATION_MODAL);
        this.staff = existing != null ? existing : new Staff();

        setSize(550, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        txtName = new PlaceholderTextField("Full Name (e.g. Rachel Adams)");
        cmbDepartment = new JComboBox<>();
        txtRoleTitle = new PlaceholderTextField("Role Title (e.g. Head Nurse, Lab Technician)");
        txtQualification = new PlaceholderTextField("Qualification (e.g. B.Sc Nursing)");
        txtPhone = new PlaceholderTextField("Contact Phone");
        txtEmail = new PlaceholderTextField("Email Address");
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "TERMINATED"});

        try {
            departments.addAll(AppController.getInstance().getDoctorStaffService().listDepartments(true));
            for (Department d : departments) {
                cmbDepartment.addItem(d.getDepartmentName());
            }
        } catch (Exception e) {
            cmbDepartment.addItem("Administration");
        }

        content.add(UIHelper.createFormRow("Full Name *", txtName));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Department *", cmbDepartment));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Role Title *", txtRoleTitle));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Qualification", txtQualification));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Phone Number *", txtPhone));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Email Address", txtEmail));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Status", cmbStatus));

        if (existing != null) {
            txtName.setText(existing.getFullName());
            txtRoleTitle.setText(existing.getRoleTitle());
            txtQualification.setText(existing.getQualification());
            txtPhone.setText(existing.getContactNumber());
            txtEmail.setText(existing.getEmail());
            cmbStatus.setSelectedItem(existing.getEmploymentStatus());

            if (existing.getDepartmentName() != null) {
                cmbDepartment.setSelectedItem(existing.getDepartmentName());
            }
        }

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Staff", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String role = txtRoleTitle.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || role.isEmpty() || phone.isEmpty()) {
            NotificationToast.showError(this, "Please fill in all mandatory fields (*).");
            return;
        }

        int deptIdx = cmbDepartment.getSelectedIndex();
        if (deptIdx >= 0 && deptIdx < departments.size()) {
            staff.setDepartmentId(departments.get(deptIdx).getDepartmentId());
        }

        staff.setFullName(name);
        staff.setRoleTitle(role);
        staff.setQualification(txtQualification.getText().trim());
        staff.setContactNumber(phone);
        staff.setEmail(txtEmail.getText().trim());
        staff.setEmploymentStatus((String) cmbStatus.getSelectedItem());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Staff getStaff() { return staff; }
}
