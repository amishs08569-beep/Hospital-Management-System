package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for scheduling or rescheduling appointments.
 */
public class AppointmentDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbDoctor;
    private final PlaceholderTextField txtDate;
    private final PlaceholderTextField txtStartTime;
    private final PlaceholderTextField txtEndTime;
    private final PlaceholderTextField txtReason;

    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private Appointment appointment;
    private boolean saved = false;

    public AppointmentDialog(Window owner, Appointment existing) {
        super(owner, existing == null ? "Book Clinic Appointment" : "Reschedule Appointment", ModalityType.APPLICATION_MODAL);
        this.appointment = existing != null ? existing : new Appointment();

        setSize(580, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        txtDate = new PlaceholderTextField(LocalDate.now().toString());
        txtStartTime = new PlaceholderTextField("10:00:00");
        txtEndTime = new PlaceholderTextField("10:30:00");
        txtReason = new PlaceholderTextField("Reason for consultation (e.g. Routine checkup, fever)");

        // Preload patients and doctors
        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) {
                cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");
            }

            doctors.addAll(AppController.getInstance().getDoctorStaffService().listDoctors(null, null, true));
            for (Doctor d : doctors) {
                cmbDoctor.addItem(d.getFullName() + " - " + d.getSpecialization());
            }
        } catch (Exception e) {
            // fallback
        }

        content.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Select Doctor *", cmbDoctor));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Date (YYYY-MM-DD) *", txtDate));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Start Time (HH:mm) *", txtStartTime));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("End Time (HH:mm) *", txtEndTime));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Reason / Symptoms", txtReason));

        if (existing != null) {
            txtDate.setText(existing.getAppointmentDate() != null ? existing.getAppointmentDate().toString() : "");
            txtStartTime.setText(existing.getAppointmentTime() != null ? existing.getAppointmentTime().toString() : "");
            txtEndTime.setText(existing.getEndTime() != null ? existing.getEndTime().toString() : "");
            txtReason.setText(existing.getReason());
        }

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Confirm Booking", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        int patIdx = cmbPatient.getSelectedIndex();
        int docIdx = cmbDoctor.getSelectedIndex();
        if (patIdx < 0 || patIdx >= patients.size() || docIdx < 0 || docIdx >= doctors.size()) {
            NotificationToast.showError(this, "Please select both a patient and a doctor.");
            return;
        }

        LocalDate date;
        LocalTime start;
        LocalTime end;
        try {
            date = LocalDate.parse(txtDate.getText().trim());
            start = LocalTime.parse(txtStartTime.getText().trim());
            end = LocalTime.parse(txtEndTime.getText().trim());
            if (end.isBefore(start) || end.equals(start)) {
                NotificationToast.showError(this, "End time must be after start time.");
                return;
            }
        } catch (Exception e) {
            NotificationToast.showError(this, "Invalid Date (YYYY-MM-DD) or Time (HH:mm:ss) format.");
            return;
        }

        appointment.setPatientId(patients.get(patIdx).getPatientId());
        appointment.setDoctorId(doctors.get(docIdx).getDoctorId());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(start);
        appointment.setEndTime(end);
        appointment.setReason(txtReason.getText().trim());
        if (appointment.getStatus() == null) {
            appointment.setStatus("SCHEDULED");
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Appointment getAppointment() { return appointment; }
}
