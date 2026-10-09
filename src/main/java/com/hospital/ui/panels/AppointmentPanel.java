package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Appointment;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.AppointmentDialog;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment booking, rescheduling, status management, and schedule conflict resolution.
 */
public class AppointmentPanel extends JPanel {

    private final UserSession session;
    private final PlaceholderTextField txtDateFilter;
    private final JComboBox<String> cmbStatusFilter;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<Appointment> currentAppointments = new ArrayList<>();

    public AppointmentPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        txtDateFilter = new PlaceholderTextField("Date: YYYY-MM-DD (e.g. " + LocalDate.now() + ")", 14);
        txtDateFilter.setPreferredSize(new Dimension(170, 36));

        cmbStatusFilter = new JComboBox<>(new String[]{"ALL STATUSES", "SCHEDULED", "CHECKED_IN", "COMPLETED", "CANCELLED", "NO_SHOW"});
        cmbStatusFilter.setPreferredSize(new Dimension(150, 36));

        ModernButton btnFilter = new ModernButton("Filter", ModernButton.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadAppointments());

        ModernButton btnToday = new ModernButton("Today", ModernButton.ButtonType.SECONDARY);
        btnToday.addActionListener(e -> {
            txtDateFilter.setText(LocalDate.now().toString());
            loadAppointments();
        });

        ModernButton btnClear = new ModernButton("Clear", ModernButton.ButtonType.SECONDARY);
        btnClear.addActionListener(e -> {
            txtDateFilter.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            loadAppointments();
        });

        filterPanel.add(txtDateFilter);
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(btnFilter);
        filterPanel.add(btnToday);
        filterPanel.add(btnClear);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnBook = new ModernButton("➕ Book Appointment", ModernButton.ButtonType.SUCCESS);
        btnBook.setEnabled(session.hasPermission(RolePermission.Permission.BOOK_APPOINTMENT));
        btnBook.addActionListener(e -> onBookAppointment());

        ModernButton btnCheckIn = new ModernButton("✅ Check In", ModernButton.ButtonType.SECONDARY);
        btnCheckIn.setEnabled(session.hasPermission(RolePermission.Permission.UPDATE_APPOINTMENT));
        btnCheckIn.addActionListener(e -> onUpdateStatus("CHECKED_IN"));

        ModernButton btnComplete = new ModernButton("🩺 Complete", ModernButton.ButtonType.PRIMARY);
        btnComplete.setEnabled(session.hasPermission(RolePermission.Permission.UPDATE_APPOINTMENT));
        btnComplete.addActionListener(e -> onUpdateStatus("COMPLETED"));

        ModernButton btnCancel = new ModernButton("❌ Cancel", ModernButton.ButtonType.DANGER);
        btnCancel.setEnabled(session.hasPermission(RolePermission.Permission.CANCEL_APPOINTMENT));
        btnCancel.addActionListener(e -> onCancelAppointment());

        actionPanel.add(btnBook);
        actionPanel.add(btnCheckIn);
        actionPanel.add(btnComplete);
        actionPanel.add(btnCancel);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Appointment Table
        String[] cols = {"Appt #", "Date", "Time Window", "Patient Name", "Doctor", "Department", "Status", "Reason"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadAppointments();
    }

    public void loadAppointments() {
        LocalDate filterDate = null;
        String dateText = txtDateFilter.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateText);
            } catch (Exception ignored) {}
        }

        String statusSelected = (String) cmbStatusFilter.getSelectedItem();
        String filterStatus = "ALL STATUSES".equals(statusSelected) ? null : statusSelected;

        final LocalDate finalDate = filterDate;
        AppController.executeAsync(
                () -> AppController.getInstance().getAppointmentService().listAppointments(finalDate, null, null, filterStatus),
                list -> {
                    currentAppointments.clear();
                    currentAppointments.addAll(list);
                    tableModel.setRowCount(0);
                    for (Appointment a : list) {
                        String timeWin = DateUtil.formatTime(a.getAppointmentTime()) + " - " + DateUtil.formatTime(a.getEndTime());
                        tableModel.addRow(new Object[]{
                                a.getAppointmentNumber(),
                                DateUtil.formatDate(a.getAppointmentDate()),
                                timeWin,
                                a.getPatientName(),
                                a.getDoctorName(),
                                a.getDepartmentName(),
                                a.getStatus(),
                                a.getReason() != null ? a.getReason() : "-"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load appointments: " + error.getMessage())
        );
    }

    private Appointment getSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= currentAppointments.size()) {
            NotificationToast.showWarning(this, "Please select an appointment from the table first.");
            return null;
        }
        return currentAppointments.get(r);
    }

    private void onBookAppointment() {
        AppointmentDialog d = new AppointmentDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) {
            Appointment a = d.getAppointment();
            AppController.executeAsync(
                    () -> AppController.getInstance().getAppointmentService().bookAppointment(a),
                    res -> {
                        NotificationToast.showSuccess(this, "Appointment scheduled! Token: " + res.getAppointmentNumber());
                        loadAppointments();
                    },
                    error -> NotificationToast.showError(this, "Booking failed: " + error.getMessage())
            );
        }
    }

    private void onUpdateStatus(String newStatus) {
        Appointment a = getSelected();
        if (a == null) return;

        AppController.executeAsync(
                () -> AppController.getInstance().getAppointmentService().updateStatus(a.getAppointmentId(), newStatus),
                res -> {
                    NotificationToast.showSuccess(this, "Appointment updated to " + newStatus);
                    loadAppointments();
                },
                error -> NotificationToast.showError(this, "Failed to update status: " + error.getMessage())
        );
    }

    private void onCancelAppointment() {
        Appointment a = getSelected();
        if (a == null) return;

        String reason = JOptionPane.showInputDialog(this, "Please enter cancellation reason:", "Cancel Appointment", JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            return;
        }

        AppController.executeAsync(
                () -> AppController.getInstance().getAppointmentService().cancelAppointment(a.getAppointmentId(), reason.trim()),
                res -> {
                    NotificationToast.showSuccess(this, "Appointment cancelled.");
                    loadAppointments();
                },
                error -> NotificationToast.showError(this, "Failed to cancel appointment: " + error.getMessage())
        );
    }
}
