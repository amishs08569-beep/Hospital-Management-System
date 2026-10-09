package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Role;
import com.hospital.model.User;
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
 * Dialog to add or update user credentials, roles, and access permissions.
 */
public class UserDialog extends JDialog {

    private final PlaceholderTextField txtUsername;
    private final PlaceholderTextField txtFullName;
    private final PlaceholderTextField txtEmail;
    private final JPasswordField txtPassword;
    private final JComboBox<String> cmbRole;

    private final List<Role> roles = new ArrayList<>();
    private User user;
    private String plainPassword;
    private boolean saved = false;

    public UserDialog(Window owner, User existing) {
        super(owner, existing == null ? "Create New User Account" : "Edit User Account", ModalityType.APPLICATION_MODAL);
        this.user = existing != null ? existing : new User();

        setSize(520, 440);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        txtUsername = new PlaceholderTextField("Login username (e.g. jsmith)");
        txtFullName = new PlaceholderTextField("Full Legal Name (e.g. John Smith)");
        txtEmail = new PlaceholderTextField("email@apexcarehospital.com");
        txtPassword = new JPasswordField();
        cmbRole = new JComboBox<>();

        try {
            roles.addAll(AppController.getInstance().getAuthService().getAllRoles());
            for (Role r : roles) {
                cmbRole.addItem(r.getRoleName());
            }
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Username *", txtUsername));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Full Name *", txtFullName));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Email Address", txtEmail));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("System Role *", cmbRole));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow(existing == null ? "Password *" : "Reset Password", txtPassword));

        if (existing != null) {
            txtUsername.setText(existing.getUsername());
            txtUsername.setEditable(false);
            txtFullName.setText(existing.getFullName());
            txtEmail.setText(existing.getEmail());
            cmbRole.setSelectedItem(existing.getRoleName());
        }

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save User Account", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String uName = txtUsername.getText().trim();
        String fName = txtFullName.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (uName.isEmpty() || fName.isEmpty()) {
            NotificationToast.showError(this, "Username and Full Name are required.");
            return;
        }

        if (user.getUserId() == null && pass.isEmpty()) {
            NotificationToast.showError(this, "Initial password is required for new user creation.");
            return;
        }

        int rIdx = cmbRole.getSelectedIndex();
        if (rIdx >= 0 && rIdx < roles.size()) {
            user.setRoleId(roles.get(rIdx).getRoleId());
            user.setRoleName(roles.get(rIdx).getRoleName());
        }

        user.setUsername(uName);
        user.setFullName(fName);
        user.setEmail(txtEmail.getText().trim());
        if (user.getUserId() == null) {
            user.setActive(true);
        }
        this.plainPassword = pass;
        this.saved = true;

        dispose();
    }

    public boolean isSaved() { return saved; }
    public User getUser() { return user; }
    public String getPlainPassword() { return plainPassword; }
}
