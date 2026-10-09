package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to receive stock inventory into a specific batch with expiry tracking.
 */
public class StockReceiptDialog extends JDialog {

    private final JComboBox<String> cmbMedicine;
    private final PlaceholderTextField txtBatchNumber;
    private final PlaceholderTextField txtExpiryDate;
    private final PlaceholderTextField txtQuantity;
    private final PlaceholderTextField txtCostPrice;
    private final PlaceholderTextField txtSellingPrice;

    private final List<Medicine> medicines = new ArrayList<>();
    private boolean saved = false;
    private MedicineBatch batch = new MedicineBatch();

    public StockReceiptDialog(Window owner) {
        super(owner, "Stock Receipt & Batch Inventory Inward", ModalityType.APPLICATION_MODAL);
        setSize(560, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        cmbMedicine = new JComboBox<>();
        txtBatchNumber = new PlaceholderTextField("Batch # (e.g. BATCH-2026-X1)");
        txtExpiryDate = new PlaceholderTextField(LocalDate.now().plusMonths(12).toString());
        txtQuantity = new PlaceholderTextField("100");
        txtCostPrice = new PlaceholderTextField("8.00");
        txtSellingPrice = new PlaceholderTextField("15.00");

        try {
            medicines.addAll(AppController.getInstance().getPharmacyService().listMedicines(null, null, false));
            for (Medicine m : medicines) {
                cmbMedicine.addItem(m.getMedicineName() + " (" + m.getStrength() + ")");
            }
        } catch (Exception ignored) {}

        content.add(UIHelper.createFormRow("Select Medicine *", cmbMedicine));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Batch Number *", txtBatchNumber));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Expiry Date (YYYY-MM-DD) *", txtExpiryDate));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Received Quantity *", txtQuantity));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Purchase Cost Price", txtCostPrice));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Selling Unit Price", txtSellingPrice));

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Receive Stock Inward", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        int idx = cmbMedicine.getSelectedIndex();
        if (idx < 0 || idx >= medicines.size()) {
            NotificationToast.showError(this, "Please select a medicine.");
            return;
        }

        String batchNo = txtBatchNumber.getText().trim();
        String expStr = txtExpiryDate.getText().trim();
        if (batchNo.isEmpty() || expStr.isEmpty()) {
            NotificationToast.showError(this, "Batch Number and Expiry Date are required.");
            return;
        }

        LocalDate exp;
        int qty;
        try {
            exp = LocalDate.parse(expStr);
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            NotificationToast.showError(this, "Invalid date format or positive quantity.");
            return;
        }

        if (exp.isBefore(LocalDate.now())) {
            NotificationToast.showError(this, "Cannot receive an already expired batch.");
            return;
        }

        Medicine m = medicines.get(idx);
        batch.setMedicineId(m.getMedicineId());
        batch.setBatchNumber(batchNo);
        batch.setExpiryDate(exp);
        batch.setCurrentQuantity(qty);
        try {
            batch.setPurchasePrice(new BigDecimal(txtCostPrice.getText().trim()));
            batch.setSellingPrice(new BigDecimal(txtSellingPrice.getText().trim()));
        } catch (Exception ex) {
            batch.setPurchasePrice(m.getPurchasePrice());
            batch.setSellingPrice(m.getSellingPrice());
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public MedicineBatch getBatch() { return batch; }
}
