package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.User;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.UserDialog;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * System administrative user account and permission management panel.
 */
public class UserManagementPanel extends JPanel {

    private final UserSession session;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<User> currentUsers = new ArrayList<>();

    public UserManagementPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("System User Accounts & Security Credentials");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.TEXT_DARK);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("➕ Create User", ModernButton.ButtonType.SUCCESS);
        btnNew.addActionListener(e -> onNewUser());

        ModernButton btnEdit = new ModernButton("✏️ Edit User", ModernButton.ButtonType.SECONDARY);
        btnEdit.addActionListener(e -> onEditUser());

        ModernButton btnToggle = new ModernButton("Deactivate / Activate", ModernButton.ButtonType.DANGER);
        btnToggle.addActionListener(e -> onToggleUser());

        ModernButton btnRefresh = new ModernButton("🔄 Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadUsers());

        actionPanel.add(btnNew);
        actionPanel.add(btnEdit);
        actionPanel.add(btnToggle);
        actionPanel.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Full Name", "Assigned Role", "Email Address", "Last Login", "Account Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadUsers();
    }

    public void loadUsers() {
        AppController.executeAsync(
                () -> AppController.getInstance().getAuthService().getAllUsers(),
                users -> {
                    currentUsers.clear();
                    currentUsers.addAll(users);
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        tableModel.addRow(new Object[]{
                                u.getUserId(),
                                u.getUsername(),
                                u.getFullName(),
                                u.getRoleName(),
                                u.getEmail() != null ? u.getEmail() : "-",
                                u.getLastLogin() != null ? DateUtil.formatDateTime(u.getLastLogin()) : "Never",
                                u.isActive() ? "ACTIVE" : "INACTIVE"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load user accounts: " + error.getMessage())
        );
    }

    private User getSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= currentUsers.size()) {
            NotificationToast.showWarning(this, "Please select a user account first.");
            return null;
        }
        return currentUsers.get(r);
    }

    private void onNewUser() {
        UserDialog d = new UserDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) {
            User u = d.getUser();
            String pass = d.getPlainPassword();
            AppController.executeAsync(
                    () -> AppController.getInstance().getAuthService().createUser(u, pass),
                    res -> {
                        NotificationToast.showSuccess(this, "User created successfully! Username: " + res.getUsername());
                        loadUsers();
                    },
                    error -> NotificationToast.showError(this, "Failed to create user: " + error.getMessage())
            );
        }
    }

    private void onEditUser() {
        User sel = getSelected();
        if (sel == null) return;
        UserDialog d = new UserDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isSaved()) {
            User u = d.getUser();
            String pass = d.getPlainPassword();
            AppController.executeAsync(
                    () -> {
                        AppController.getInstance().getAuthService().updateUser(u);
                        if (pass != null && !pass.trim().isEmpty()) {
                            AppController.getInstance().getAuthService().changePassword(u.getUserId(), null, pass.trim());
                        }
                        return true;
                    },
                    res -> {
                        NotificationToast.showSuccess(this, "User account updated successfully.");
                        loadUsers();
                    },
                    error -> NotificationToast.showError(this, "Failed to update user: " + error.getMessage())
            );
        }
    }

    private void onToggleUser() {
        User sel = getSelected();
        if (sel == null) return;
        if (sel.getUserId().equals(session.getUserId())) {
            NotificationToast.showWarning(this, "You cannot deactivate your own active session!");
            return;
        }

        boolean newActive = !sel.isActive();
        AppController.executeAsync(
                () -> AppController.getInstance().getAuthService().setUserActive(sel.getUserId(), newActive),
                res -> {
                    NotificationToast.showSuccess(this, "User account status updated.");
                    loadUsers();
                },
                error -> NotificationToast.showError(this, "Failed to update user status: " + error.getMessage())
        );
    }
}
