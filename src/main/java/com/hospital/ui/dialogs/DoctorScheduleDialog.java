package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Doctor;
import com.hospital.model.DoctorSchedule;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to view and configure clinic schedules for a doctor.
 */
public class DoctorScheduleDialog extends JDialog {

    private final Doctor doctor;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<DoctorSchedule> schedules = new ArrayList<>();

    private final JComboBox<String> cmbDay;
    private final PlaceholderTextField txtStart;
    private final PlaceholderTextField txtEnd;
    private final PlaceholderTextField txtMaxPatients;

    public DoctorScheduleDialog(Window owner, Doctor doctor) {
        super(owner, "Weekly Clinic Schedule: " + doctor.getFullName(), ModalityType.APPLICATION_MODAL);
        this.doctor = doctor;

        setSize(650, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        // Schedule Table
        String[] cols = {"Day of Week", "Start Time", "End Time", "Max Patients"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        // Bottom Add / Delete Panel
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addRow.setOpaque(false);

        cmbDay = new JComboBox<>(new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"});
        txtStart = new PlaceholderTextField("09:00:00", 8);
        txtEnd = new PlaceholderTextField("17:00:00", 8);
        txtMaxPatients = new PlaceholderTextField("20", 5);

        ModernButton btnAdd = new ModernButton("➕ Add Shift", ModernButton.ButtonType.SUCCESS);
        btnAdd.addActionListener(e -> onAddSlot());

        ModernButton btnRemove = new ModernButton("🗑️ Remove Selected", ModernButton.ButtonType.DANGER);
        btnRemove.addActionListener(e -> onRemoveSlot());

        addRow.add(new JLabel("Day:"));
        addRow.add(cmbDay);
        addRow.add(new JLabel("Start:"));
        addRow.add(txtStart);
        addRow.add(new JLabel("End:"));
        addRow.add(txtEnd);
        addRow.add(new JLabel("Max:"));
        addRow.add(txtMaxPatients);
        addRow.add(btnAdd);
        addRow.add(btnRemove);

        bottomContainer.add(addRow, BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setOpaque(false);
        ModernButton btnClose = new ModernButton("Done", ModernButton.ButtonType.SECONDARY);
        btnClose.addActionListener(e -> dispose());
        closePanel.add(btnClose);
        bottomContainer.add(closePanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        loadSchedules();
    }

    private void loadSchedules() {
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().getDoctorSchedules(doctor.getDoctorId()),
                list -> {
                    schedules.clear();
                    schedules.addAll(list);
                    tableModel.setRowCount(0);
                    for (DoctorSchedule s : list) {
                        tableModel.addRow(new Object[]{
                                s.getDayOfWeek(),
                                DateUtil.formatTime(s.getStartTime()),
                                DateUtil.formatTime(s.getEndTime()),
                                s.getMaxPatients()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load schedules: " + error.getMessage())
        );
    }

    private void onAddSlot() {
        try {
            LocalTime start = LocalTime.parse(txtStart.getText().trim());
            LocalTime end = LocalTime.parse(txtEnd.getText().trim());
            if (end.isBefore(start) || end.equals(start)) {
                NotificationToast.showError(this, "End time must be after start time.");
                return;
            }
            int max = Integer.parseInt(txtMaxPatients.getText().trim());

            DoctorSchedule s = new DoctorSchedule();
            s.setDoctorId(doctor.getDoctorId());
            s.setDayOfWeek((String) cmbDay.getSelectedItem());
            s.setStartTime(start);
            s.setEndTime(end);
            s.setMaxPatients(max);

            AppController.executeAsync(
                    () -> AppController.getInstance().getDoctorStaffService().addDoctorSchedule(s),
                    created -> {
                        NotificationToast.showSuccess(this, "Shift schedule added.");
                        loadSchedules();
                    },
                    error -> NotificationToast.showError(this, "Failed to add shift: " + error.getMessage())
            );
        } catch (Exception ex) {
            NotificationToast.showError(this, "Invalid time format (use HH:mm:ss) or max patient number.");
        }
    }

    private void onRemoveSlot() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= schedules.size()) {
            NotificationToast.showWarning(this, "Select a schedule row to remove.");
            return;
        }

        DoctorSchedule s = schedules.get(row);
        AppController.executeAsync(
                () -> AppController.getInstance().getDoctorStaffService().removeDoctorSchedule(s.getScheduleId()),
                success -> {
                    NotificationToast.showSuccess(this, "Shift schedule removed.");
                    loadSchedules();
                },
                error -> NotificationToast.showError(this, "Failed to remove shift: " + error.getMessage())
        );
    }
}
