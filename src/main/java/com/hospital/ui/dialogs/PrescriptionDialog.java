package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.*;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to prescribe medicines, dosage, and duration for patients.
 */
public class PrescriptionDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbDoctor;
    private final PlaceholderTextField txtDiagnosis;
    private final PlaceholderTextField txtNotes;

    // Items table
    private final JTable itemsTable;
    private final DefaultTableModel itemsModel;
    private final List<PrescriptionItem> itemsList = new ArrayList<>();

    // Inputs for adding medicine
    private final JComboBox<String> cmbMedicine;
    private final PlaceholderTextField txtDosage;
    private final PlaceholderTextField txtFrequency;
    private final PlaceholderTextField txtDuration;
    private final PlaceholderTextField txtQty;
    private final PlaceholderTextField txtInstructions;

    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Medicine> medicines = new ArrayList<>();

    private Prescription prescription = new Prescription();
    private boolean saved = false;

    public PrescriptionDialog(Window owner) {
        super(owner, "Issue Medical Prescription", ModalityType.APPLICATION_MODAL);
        setSize(800, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 20, 12, 20));
        header.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        txtDiagnosis = new PlaceholderTextField("Diagnosis summary (e.g. Viral URI, Hypertension)");
        txtNotes = new PlaceholderTextField("General advice / Diet / Hydration instructions");

        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");

            doctors.addAll(AppController.getInstance().getDoctorStaffService().listDoctors(null, null, true));
            for (Doctor d : doctors) cmbDoctor.addItem(d.getFullName() + " - " + d.getSpecialization());

            medicines.addAll(AppController.getInstance().getPharmacyService().listMedicines(null, null, false));
        } catch (Exception ignored) {}

        header.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        header.add(Box.createVerticalStrut(8));
        header.add(UIHelper.createFormRow("Prescribing Doctor *", cmbDoctor));
        header.add(Box.createVerticalStrut(8));
        header.add(UIHelper.createFormRow("Diagnosis *", txtDiagnosis));
        header.add(Box.createVerticalStrut(8));
        header.add(UIHelper.createFormRow("Doctor's Advice", txtNotes));
        header.add(Box.createVerticalStrut(12));

        // Center: Items table
        String[] cols = {"Medicine", "Dosage", "Frequency", "Duration", "Qty", "Instructions"};
        itemsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        UIHelper.styleTable(itemsTable);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(header, BorderLayout.NORTH);
        centerPanel.add(UIHelper.createModernScrollPane(itemsTable), BorderLayout.CENTER);

        // Add Medicine Bar
        JPanel addMedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        addMedPanel.setBackground(Color.WHITE);
        addMedPanel.setBorder(BorderFactory.createTitledBorder("Add Prescribed Medication"));

        cmbMedicine = new JComboBox<>();
        cmbMedicine.setPreferredSize(new Dimension(180, 32));
        for (Medicine m : medicines) {
            cmbMedicine.addItem(m.getMedicineName() + " (" + m.getStrength() + ")");
        }

        txtDosage = new PlaceholderTextField("500mg", 6);
        txtFrequency = new PlaceholderTextField("1-0-1", 6);
        txtDuration = new PlaceholderTextField("5 days", 6);
        txtQty = new PlaceholderTextField("10", 4);
        txtInstructions = new PlaceholderTextField("After food", 10);

        ModernButton btnAddItem = new ModernButton("➕ Add", ModernButton.ButtonType.SUCCESS);
        btnAddItem.addActionListener(e -> onAddItem());

        ModernButton btnRemoveItem = new ModernButton("🗑️ Remove", ModernButton.ButtonType.DANGER);
        btnRemoveItem.addActionListener(e -> onRemoveItem());

        addMedPanel.add(cmbMedicine);
        addMedPanel.add(txtDosage);
        addMedPanel.add(txtFrequency);
        addMedPanel.add(txtDuration);
        addMedPanel.add(txtQty);
        addMedPanel.add(txtInstructions);
        addMedPanel.add(btnAddItem);
        addMedPanel.add(btnRemoveItem);

        centerPanel.add(addMedPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Issue Prescription", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onAddItem() {
        int idx = cmbMedicine.getSelectedIndex();
        if (idx < 0 || idx >= medicines.size()) return;

        Medicine med = medicines.get(idx);
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            NotificationToast.showError(this, "Quantity must be a positive integer.");
            return;
        }

        PrescriptionItem item = new PrescriptionItem();
        item.setMedicineId(med.getMedicineId());
        item.setMedicineName(med.getMedicineName());
        item.setDosage(txtDosage.getText().trim().isEmpty() ? med.getStrength() : txtDosage.getText().trim());
        item.setFrequency(txtFrequency.getText().trim());
        item.setDuration(txtDuration.getText().trim());
        item.setQuantity(qty);
        item.setInstructions(txtInstructions.getText().trim());

        itemsList.add(item);
        itemsModel.addRow(new Object[]{
                item.getMedicineName(),
                item.getDosage(),
                item.getFrequency(),
                item.getDuration(),
                item.getQuantity(),
                item.getInstructions()
        });
    }

    private void onRemoveItem() {
        int r = itemsTable.getSelectedRow();
        if (r < 0 || r >= itemsList.size()) return;
        itemsList.remove(r);
        itemsModel.removeRow(r);
    }

    private void onSave() {
        int patIdx = cmbPatient.getSelectedIndex();
        int docIdx = cmbDoctor.getSelectedIndex();

        if (patIdx < 0 || docIdx < 0) {
            NotificationToast.showError(this, "Patient and Doctor selections are required.");
            return;
        }

        if (itemsList.isEmpty()) {
            NotificationToast.showError(this, "Please add at least one medication to the prescription.");
            return;
        }

        prescription.setPatientId(patients.get(patIdx).getPatientId());
        prescription.setDoctorId(doctors.get(docIdx).getDoctorId());
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setDiagnosis(txtDiagnosis.getText().trim());
        prescription.setNotes(txtNotes.getText().trim());
        prescription.setItems(itemsList);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Prescription getPrescription() { return prescription; }
}
