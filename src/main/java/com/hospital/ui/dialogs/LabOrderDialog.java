package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Doctor;
import com.hospital.model.LabOrder;
import com.hospital.model.LabTest;
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
 * Dialog to order diagnostic laboratory tests.
 */
public class LabOrderDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbDoctor;
    private final JComboBox<String> cmbPriority;
    private final PlaceholderTextField txtNotes;
    private final DefaultListModel<String> testListModel;
    private final JList<String> testList;

    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<LabTest> allTests = new ArrayList<>();
    private final List<Integer> selectedTestIds = new ArrayList<>();

    private LabOrder labOrder = new LabOrder();
    private boolean saved = false;

    public LabOrderDialog(Window owner) {
        super(owner, "Order Diagnostic Laboratory Tests", ModalityType.APPLICATION_MODAL);
        setSize(650, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        cmbPriority = new JComboBox<>(new String[]{"NORMAL", "URGENT", "STAT"});
        txtNotes = new PlaceholderTextField("Clinical indications / Pre-test fasting notes");

        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");

            doctors.addAll(AppController.getInstance().getDoctorStaffService().listDoctors(null, null, true));
            for (Doctor d : doctors) cmbDoctor.addItem(d.getFullName() + " - " + d.getSpecialization());

            allTests.addAll(AppController.getInstance().getLabService().listTests(true));
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Ordering Doctor *", cmbDoctor));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Test Priority", cmbPriority));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Clinical Notes", txtNotes));
        content.add(Box.createVerticalStrut(10));

        JLabel lblTests = new JLabel("Select Laboratory Tests (Hold Ctrl/Shift to multi-select) *");
        lblTests.setFont(UIHelper.FONT_HEADER);
        content.add(lblTests);
        content.add(Box.createVerticalStrut(6));

        testListModel = new DefaultListModel<>();
        for (LabTest t : allTests) {
            testListModel.addElement(t.getTestName() + " [" + t.getCategory() + " - " + CurrencyUtil.format(t.getPrice()) + "]");
        }
        testList = new JList<>(testListModel);
        testList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        testList.setFont(UIHelper.FONT_BODY);

        content.add(new JScrollPane(testList));
        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Submit Lab Order", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        int patIdx = cmbPatient.getSelectedIndex();
        int docIdx = cmbDoctor.getSelectedIndex();
        int[] selIndices = testList.getSelectedIndices();

        if (patIdx < 0 || docIdx < 0 || selIndices.length == 0) {
            NotificationToast.showError(this, "Please select Patient, Doctor, and at least one Lab Test.");
            return;
        }

        selectedTestIds.clear();
        for (int idx : selIndices) {
            selectedTestIds.add(allTests.get(idx).getTestId());
        }

        labOrder.setPatientId(patients.get(patIdx).getPatientId());
        labOrder.setDoctorId(doctors.get(docIdx).getDoctorId());
        labOrder.setOrderDate(LocalDateTime.now());
        labOrder.setPriority((String) cmbPriority.getSelectedItem());
        labOrder.setClinicalNotes(txtNotes.getText().trim());
        labOrder.setStatus("REQUESTED");

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public LabOrder getLabOrder() { return labOrder; }
    public List<Integer> getSelectedTestIds() { return selectedTestIds; }
}
