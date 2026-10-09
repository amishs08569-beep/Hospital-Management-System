package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Admission;
import com.hospital.model.Bed;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for registering an inpatient admission and allocating an available bed.
 */
public class AdmissionDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbDoctor;
    private final JComboBox<String> cmbBed;
    private final PlaceholderTextField txtReason;

    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Bed> availableBeds = new ArrayList<>();

    private Admission admission = new Admission();
    private boolean saved = false;

    public AdmissionDialog(Window owner) {
        super(owner, "Inpatient Admission & Bed Allocation", ModalityType.APPLICATION_MODAL);
        setSize(600, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        cmbBed = new JComboBox<>();
        txtReason = new PlaceholderTextField("Reason for admission / Initial diagnosis");

        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) {
                cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");
            }

            doctors.addAll(AppController.getInstance().getDoctorStaffService().listDoctors(null, null, true));
            for (Doctor d : doctors) {
                cmbDoctor.addItem(d.getFullName() + " - " + d.getSpecialization());
            }

            availableBeds.addAll(AppController.getInstance().getAdmissionService().listAvailableBeds());
            for (Bed b : availableBeds) {
                cmbBed.addItem("Bed " + b.getBedNumber() + " [" + b.getWardName() + " - " + b.getBedType() + " @ " + CurrencyUtil.format(b.getDailyRate()) + "/day]");
            }
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        content.add(Box.createVerticalStrut(12));
        content.add(UIHelper.createFormRow("Attending Doctor *", cmbDoctor));
        content.add(Box.createVerticalStrut(12));
        content.add(UIHelper.createFormRow("Allocate Bed *", cmbBed));
        content.add(Box.createVerticalStrut(12));
        content.add(UIHelper.createFormRow("Admission Reason", txtReason));

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Confirm Admission", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        int patIdx = cmbPatient.getSelectedIndex();
        int docIdx = cmbDoctor.getSelectedIndex();
        int bedIdx = cmbBed.getSelectedIndex();

        if (patIdx < 0 || docIdx < 0 || bedIdx < 0) {
            NotificationToast.showError(this, "Please select Patient, Attending Doctor, and an Available Bed.");
            return;
        }

        admission.setPatientId(patients.get(patIdx).getPatientId());
        admission.setDoctorId(doctors.get(docIdx).getDoctorId());
        admission.setBedId(availableBeds.get(bedIdx).getBedId());
        admission.setAdmissionDate(LocalDateTime.now());
        admission.setReasonForAdmission(txtReason.getText().trim());
        admission.setStatus("ADMITTED");

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Admission getAdmission() { return admission; }
}
