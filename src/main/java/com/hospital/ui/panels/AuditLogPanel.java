package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.AuditLog;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * System audit log panel for review of sensitive actions, logins, and clinical modifications.
 */
public class AuditLogPanel extends JPanel {

    private final UserSession session;
    private final PlaceholderTextField txtUserSearch;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public AuditLogPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        txtUserSearch = new PlaceholderTextField("Filter by Username...", 18);
        txtUserSearch.setPreferredSize(new Dimension(200, 36));

        ModernButton btnFilter = new ModernButton("Filter Logs", ModernButton.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadLogs());

        ModernButton btnRefresh = new ModernButton("🔄 Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> {
            txtUserSearch.setText("");
            loadLogs();
        });

        searchPanel.add(txtUserSearch);
        searchPanel.add(btnFilter);
        searchPanel.add(btnRefresh);

        topBar.add(searchPanel, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Timestamp", "User", "Action", "Entity Type", "Entity Ref", "Audit Details", "IP Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadLogs();
    }

    public void loadLogs() {
        String user = txtUserSearch.getText().trim();
        AppController.executeAsync(
                () -> AppController.getInstance().getAuditService().getRecentLogs(null, user.isEmpty() ? null : user, 200),
                logs -> {
                    tableModel.setRowCount(0);
                    for (AuditLog l : logs) {
                        tableModel.addRow(new Object[]{
                                l.getLogId(),
                                DateUtil.formatDateTime(l.getTimestamp()),
                                l.getUsername(),
                                l.getAction(),
                                l.getEntityType() != null ? l.getEntityType() : "-",
                                l.getEntityId() != null ? l.getEntityId() : "-",
                                l.getDetails() != null ? l.getDetails() : "-",
                                l.getIpAddress() != null ? l.getIpAddress() : "127.0.0.1"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load audit trail: " + error.getMessage())
        );
    }
}
