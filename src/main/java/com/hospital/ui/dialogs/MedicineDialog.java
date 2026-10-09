package com.hospital.ui.dialogs;

import com.hospital.model.Medicine;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Dialog to register or update pharmaceutical medications.
 */
public class MedicineDialog extends JDialog {

    private final PlaceholderTextField txtName;
    private final PlaceholderTextField txtGeneric;
    private final JComboBox<String> cmbCategory;
    private final PlaceholderTextField txtManufacturer;
    private final PlaceholderTextField txtStrength;
    private final JComboBox<String> cmbUnitType;
    private final PlaceholderTextField txtPurchasePrice;
    private final PlaceholderTextField txtSellingPrice;
    private final PlaceholderTextField txtReorderLevel;

    private Medicine medicine;
    private boolean saved = false;

    public MedicineDialog(Window owner, Medicine existing) {
        super(owner, existing == null ? "Register New Medication" : "Edit Medication Information", ModalityType.APPLICATION_MODAL);
        this.medicine = existing != null ? existing : new Medicine();

        setSize(580, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        txtName = new PlaceholderTextField("Brand Name (e.g. Amoxicillin)");
        txtGeneric = new PlaceholderTextField("Generic Chemical Name (e.g. Amoxicillin Trihydrate)");
        cmbCategory = new JComboBox<>(new String[]{"Antibiotics", "Analgesics & Pain", "Cardiovascular", "Antipyretic", "Vitamins & Minerals", "Respiratory", "Gastrointestinal", "Other"});
        txtManufacturer = new PlaceholderTextField("Pharmaceutical Manufacturer (e.g. Pfizer, Novartis)");
        txtStrength = new PlaceholderTextField("Dosage Strength (e.g. 500mg, 100ml)");
        cmbUnitType = new JComboBox<>(new String[]{"Tablets", "Capsules", "Syrup (Bottle)", "Injection (Vial)", "Ointment", "Drops"});
        txtPurchasePrice = new PlaceholderTextField("Cost / Purchase Price (e.g. 8.50)");
        txtSellingPrice = new PlaceholderTextField("Retail / Selling Price (e.g. 15.00)");
        txtReorderLevel = new PlaceholderTextField("Low-stock Alert Level (e.g. 50)");

        content.add(UIHelper.createFormRow("Medicine Name *", txtName));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Generic Name", txtGeneric));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Category", cmbCategory));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Manufacturer", txtManufacturer));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Strength", txtStrength));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Dosage Form", cmbUnitType));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Purchase Price *", txtPurchasePrice));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Selling Price *", txtSellingPrice));
        content.add(Box.createVerticalStrut(8));
        content.add(UIHelper.createFormRow("Reorder Level *", txtReorderLevel));

        if (existing != null) {
            txtName.setText(existing.getMedicineName());
            txtGeneric.setText(existing.getGenericName());
            cmbCategory.setSelectedItem(existing.getCategory());
            txtManufacturer.setText(existing.getManufacturer());
            txtStrength.setText(existing.getStrength());
            cmbUnitType.setSelectedItem(existing.getUnitType());
            txtPurchasePrice.setText(existing.getPurchasePrice() != null ? existing.getPurchasePrice().toString() : "0.00");
            txtSellingPrice.setText(existing.getSellingPrice() != null ? existing.getSellingPrice().toString() : "0.00");
            txtReorderLevel.setText(String.valueOf(existing.getReorderLevel()));
        } else {
            txtPurchasePrice.setText("10.00");
            txtSellingPrice.setText("15.00");
            txtReorderLevel.setText("30");
        }

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Medication", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String pPriceStr = txtPurchasePrice.getText().trim();
        String sPriceStr = txtSellingPrice.getText().trim();
        String reorderStr = txtReorderLevel.getText().trim();

        if (name.isEmpty() || pPriceStr.isEmpty() || sPriceStr.isEmpty() || reorderStr.isEmpty()) {
            NotificationToast.showError(this, "Please fill in all mandatory fields (*).");
            return;
        }

        BigDecimal pPrice;
        BigDecimal sPrice;
        int reorder;
        try {
            pPrice = new BigDecimal(pPriceStr);
            sPrice = new BigDecimal(sPriceStr);
            reorder = Integer.parseInt(reorderStr);
        } catch (Exception ex) {
            NotificationToast.showError(this, "Invalid price or reorder level numeric input.");
            return;
        }

        medicine.setMedicineName(name);
        medicine.setGenericName(txtGeneric.getText().trim());
        medicine.setCategory((String) cmbCategory.getSelectedItem());
        medicine.setManufacturer(txtManufacturer.getText().trim());
        medicine.setStrength(txtStrength.getText().trim());
        medicine.setUnitType((String) cmbUnitType.getSelectedItem());
        medicine.setPurchasePrice(pPrice);
        medicine.setSellingPrice(sPrice);
        medicine.setReorderLevel(reorder);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Medicine getMedicine() { return medicine; }
}
