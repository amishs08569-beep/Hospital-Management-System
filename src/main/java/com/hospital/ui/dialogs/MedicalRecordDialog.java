package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for documenting medical observations, diagnoses, and treatment plans.
 */
public class MedicalRecordDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbDoctor;
    private final PlaceholderTextField txtDiagnosis;
    private final JTextArea txtSymptoms;
    private final JTextArea txtObservations;
    private final JTextArea txtTreatmentPlan;
    private final PlaceholderTextField txtFollowUpDate;
    private final JTextArea txtFollowUpInstructions;

    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private MedicalRecord record = new MedicalRecord();
    private boolean saved = false;

    public MedicalRecordDialog(Window owner) {
        super(owner, "Document Clinical Medical Record", ModalityType.APPLICATION_MODAL);
        setSize(700, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        txtDiagnosis = new PlaceholderTextField("Clinical Diagnosis (e.g. Acute Bronchitis, Type 2 Diabetes)");
        txtSymptoms = new JTextArea(3, 20);
        txtSymptoms.setLineWrap(true);
        txtObservations = new JTextArea(3, 20);
        txtObservations.setLineWrap(true);
        txtTreatmentPlan = new JTextArea(3, 20);
        txtTreatmentPlan.setLineWrap(true);
        txtFollowUpDate = new PlaceholderTextField(LocalDate.now().plusDays(7).toString());
        txtFollowUpInstructions = new JTextArea(2, 20);
        txtFollowUpInstructions.setLineWrap(true);

        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");

            doctors.addAll(AppController.getInstance().getDoctorStaffService().listDoctors(null, null, true));
            for (Doctor d : doctors) cmbDoctor.addItem(d.getFullName() + " - " + d.getSpecialization());
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Attending Doctor *", cmbDoctor));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Primary Diagnosis *", txtDiagnosis));
        content.add(Box.createVerticalStrut(8));

        content.add(createAreaRow("Presenting Symptoms", txtSymptoms));
        content.add(Box.createVerticalStrut(8));
        content.add(createAreaRow("Clinical Observations", txtObservations));
        content.add(Box.createVerticalStrut(8));
        content.add(createAreaRow("Treatment Plan", txtTreatmentPlan));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Follow-up Date", txtFollowUpDate));
        content.add(Box.createVerticalStrut(8));
        content.add(createAreaRow("Follow-up Instructions", txtFollowUpInstructions));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Clinical Record", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createAreaRow(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UIHelper.FONT_BODY);
        l.setPreferredSize(new Dimension(140, 20));
        area.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        p.add(l, BorderLayout.WEST);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private void onSave() {
        int patIdx = cmbPatient.getSelectedIndex();
        int docIdx = cmbDoctor.getSelectedIndex();
        String diag = txtDiagnosis.getText().trim();

        if (patIdx < 0 || docIdx < 0 || diag.isEmpty()) {
            NotificationToast.showError(this, "Patient, Doctor, and Primary Diagnosis are required.");
            return;
        }

        LocalDate followUp = null;
        String fText = txtFollowUpDate.getText().trim();
        if (!fText.isEmpty()) {
            try {
                followUp = LocalDate.parse(fText);
            } catch (Exception e) {
                NotificationToast.showError(this, "Invalid follow-up date (use YYYY-MM-DD).");
                return;
            }
        }

        record.setPatientId(patients.get(patIdx).getPatientId());
        record.setDoctorId(doctors.get(docIdx).getDoctorId());
        record.setRecordDate(LocalDate.now());
        record.setDiagnosis(diag);
        record.setSymptoms(txtSymptoms.getText().trim());
        record.setPhysicalExamination(txtObservations.getText().trim());
        record.setTreatmentPlan(txtTreatmentPlan.getText().trim());
        record.setFollowUpDate(followUp);
        record.setFollowUpInstructions(txtFollowUpInstructions.getText().trim());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public MedicalRecord getRecord() { return record; }
}
