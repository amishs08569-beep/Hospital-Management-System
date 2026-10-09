package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to safely dispense medication from an active, non-expired batch.
 */
public class DispenseDialog extends JDialog {

    private final JComboBox<String> cmbMedicine;
    private final JComboBox<String> cmbBatch;
    private final PlaceholderTextField txtQuantity;
    private final PlaceholderTextField txtReference;
    private final PlaceholderTextField txtNotes;

    private final List<Medicine> medicines = new ArrayList<>();
    private final List<MedicineBatch> currentBatches = new ArrayList<>();
    private boolean dispensed = false;

    private int selectedBatchId = -1;
    private int quantityToDispense = 0;
    private String referenceNo = "";
    private String dispenseNotes = "";

    public DispenseDialog(Window owner) {
        super(owner, "Dispense Medication Outward", ModalityType.APPLICATION_MODAL);
        setSize(580, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        cmbMedicine = new JComboBox<>();
        cmbBatch = new JComboBox<>();
        txtQuantity = new PlaceholderTextField("Dispense Quantity (e.g. 10)");
        txtReference = new PlaceholderTextField("Prescription / Invoice # (e.g. RX-2026-001)");
        txtNotes = new PlaceholderTextField("Dispensing remarks (Optional)");

        cmbMedicine.addActionListener(e -> onMedicineSelected());

        try {
            medicines.addAll(AppController.getInstance().getPharmacyService().listMedicines(null, null, false));
            for (Medicine m : medicines) {
                cmbMedicine.addItem(m.getMedicineName() + " (" + m.getStrength() + ")");
            }
            if (!medicines.isEmpty()) onMedicineSelected();
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Select Medicine *", cmbMedicine));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Available Batch *", cmbBatch));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Dispense Quantity *", txtQuantity));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Prescription / Ref #", txtReference));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Remarks", txtNotes));

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnDispense = new ModernButton("Confirm Dispensing", ModernButton.ButtonType.PRIMARY);
        btnDispense.addActionListener(e -> onDispense());

        btnPanel.add(btnCancel);
        btnPanel.add(btnDispense);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onMedicineSelected() {
        int idx = cmbMedicine.getSelectedIndex();
        if (idx < 0 || idx >= medicines.size()) return;

        Medicine m = medicines.get(idx);
        AppController.executeAsync(
                () -> AppController.getInstance().getPharmacyService().getBatchesForMedicine(m.getMedicineId()),
                batches -> {
                    currentBatches.clear();
                    currentBatches.addAll(batches);
                    cmbBatch.removeAllItems();
                    for (MedicineBatch b : batches) {
                        boolean expired = b.getExpiryDate().isBefore(LocalDate.now());
                        String tag = expired ? " [EXPIRED!]" : "";
                        cmbBatch.addItem("Batch: " + b.getBatchNumber() + " (Qty: " + b.getCurrentQuantity() + ", Exp: " + DateUtil.formatDate(b.getExpiryDate()) + ")" + tag);
                    }
                },
                null
        );
    }

    private void onDispense() {
        int bIdx = cmbBatch.getSelectedIndex();
        if (bIdx < 0 || bIdx >= currentBatches.size()) {
            NotificationToast.showError(this, "Please select an available batch.");
            return;
        }

        MedicineBatch b = currentBatches.get(bIdx);
        if (b.getExpiryDate().isBefore(LocalDate.now())) {
            NotificationToast.showError(this, "Cannot dispense medicine from an EXPIRED batch! Please quarantine this batch.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            NotificationToast.showError(this, "Quantity must be a positive integer.");
            return;
        }

        if (qty > b.getCurrentQuantity()) {
            NotificationToast.showError(this, "Insufficient stock! Batch only has " + b.getCurrentQuantity() + " units available.");
            return;
        }

        this.selectedBatchId = b.getBatchId();
        this.quantityToDispense = qty;
        this.referenceNo = txtReference.getText().trim();
        this.dispenseNotes = txtNotes.getText().trim();
        this.dispensed = true;

        dispose();
    }

    public boolean isDispensed() { return dispensed; }
    public int getSelectedBatchId() { return selectedBatchId; }
    public int getQuantityToDispense() { return quantityToDispense; }
    public String getReferenceNo() { return referenceNo; }
    public String getDispenseNotes() { return dispenseNotes; }
}
