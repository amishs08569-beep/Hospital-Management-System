package com.hospital.ui.dialogs;

import com.hospital.config.AppConfig;
import com.hospital.controller.AppController;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Patient;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to construct itemized patient bills and generate official invoices.
 */
public class InvoiceDialog extends JDialog {

    private final JComboBox<String> cmbPatient;
    private final PlaceholderTextField txtAdmissionNo;
    private final PlaceholderTextField txtDiscountPercent;
    private final PlaceholderTextField txtTaxPercent;

    // Items table
    private final JTable itemsTable;
    private final DefaultTableModel itemsModel;
    private final List<InvoiceItem> itemsList = new ArrayList<>();

    // Add item controls
    private final PlaceholderTextField txtItemDesc;
    private final JComboBox<String> cmbItemType;
    private final PlaceholderTextField txtItemQty;
    private final PlaceholderTextField txtItemUnitPrice;

    // Summary Labels
    private final JLabel lblSubtotal;
    private final JLabel lblDiscount;
    private final JLabel lblTax;
    private final JLabel lblGrandTotal;

    private final List<Patient> patients = new ArrayList<>();
    private Invoice invoice = new Invoice();
    private boolean saved = false;

    public InvoiceDialog(Window owner) {
        super(owner, "Generate Patient Medical Invoice", ModalityType.APPLICATION_MODAL);
        setSize(850, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 20, 10, 20));
        header.setBackground(UIHelper.BACKGROUND);

        cmbPatient = new JComboBox<>();
        txtAdmissionNo = new PlaceholderTextField("Inpatient Admission # (e.g. ADM-2026-001, Optional)");
        txtDiscountPercent = new PlaceholderTextField("0.00");
        txtTaxPercent = new PlaceholderTextField(AppConfig.getTaxRate().toString());

        try {
            patients.addAll(AppController.getInstance().getPatientService().searchPatients(null, true));
            for (Patient p : patients) cmbPatient.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");
        } catch (Exception ignored) {}

        header.add(UIHelper.createFormRow("Select Patient *", cmbPatient));
        header.add(Box.createVerticalStrut(8));
        header.add(UIHelper.createFormRow("Admission Ref", txtAdmissionNo));
        header.add(Box.createVerticalStrut(8));

        // Center: Itemized Table
        String[] cols = {"Description", "Charge Category", "Qty", "Unit Price", "Total Price"};
        itemsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        UIHelper.styleTable(itemsTable);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerPanel.add(header, BorderLayout.NORTH);
        centerPanel.add(UIHelper.createModernScrollPane(itemsTable), BorderLayout.CENTER);

        // Add Item Row
        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        addItemPanel.setBackground(Color.WHITE);
        addItemPanel.setBorder(BorderFactory.createTitledBorder("Add Billing Line Item"));

        txtItemDesc = new PlaceholderTextField("Item Description (e.g. ICU Room 2 Days, Consultation)", 20);
        cmbItemType = new JComboBox<>(new String[]{"CONSULTATION", "ROOM_CHARGE", "LAB_TEST", "PHARMACY", "PROCEDURE", "OTHER"});
        txtItemQty = new PlaceholderTextField("1", 4);
        txtItemUnitPrice = new PlaceholderTextField("100.00", 8);

        ModernButton btnAddItem = new ModernButton("➕ Add Item", ModernButton.ButtonType.SUCCESS);
        btnAddItem.addActionListener(e -> onAddItem());

        ModernButton btnRemoveItem = new ModernButton("🗑️ Remove", ModernButton.ButtonType.DANGER);
        btnRemoveItem.addActionListener(e -> onRemoveItem());

        addItemPanel.add(txtItemDesc);
        addItemPanel.add(cmbItemType);
        addItemPanel.add(new JLabel("Qty:"));
        addItemPanel.add(txtItemQty);
        addItemPanel.add(new JLabel("Price:"));
        addItemPanel.add(txtItemUnitPrice);
        addItemPanel.add(btnAddItem);
        addItemPanel.add(btnRemoveItem);

        centerPanel.add(addItemPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Footer Summary & Actions
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        JPanel totalsPanel = new JPanel(new GridLayout(4, 2, 8, 4));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        lblSubtotal = new JLabel("$0.00");
        lblDiscount = new JLabel("$0.00");
        lblTax = new JLabel("$0.00");
        lblGrandTotal = new JLabel("$0.00");
        lblGrandTotal.setFont(UIHelper.FONT_TITLE);
        lblGrandTotal.setForeground(new Color(22, 163, 74));

        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(lblSubtotal);
        totalsPanel.add(new JLabel("Discount (%):"));
        totalsPanel.add(txtDiscountPercent);
        totalsPanel.add(new JLabel("Tax Rate (%):"));
        totalsPanel.add(txtTaxPercent);
        totalsPanel.add(new JLabel("Total Due:"));
        totalsPanel.add(lblGrandTotal);

        txtDiscountPercent.addActionListener(e -> recalculateTotals());
        txtTaxPercent.addActionListener(e -> recalculateTotals());

        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        actPanel.setOpaque(false);

        ModernButton btnRecalc = new ModernButton("🔄 Recalculate", ModernButton.ButtonType.SECONDARY);
        btnRecalc.addActionListener(e -> recalculateTotals());

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Issue Invoice", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        actPanel.add(btnRecalc);
        actPanel.add(btnCancel);
        actPanel.add(btnSave);

        footerPanel.add(totalsPanel, BorderLayout.WEST);
        footerPanel.add(actPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void onAddItem() {
        String desc = txtItemDesc.getText().trim();
        String type = (String) cmbItemType.getSelectedItem();
        int qty;
        BigDecimal price;

        try {
            qty = Integer.parseInt(txtItemQty.getText().trim());
            price = new BigDecimal(txtItemUnitPrice.getText().trim());
            if (qty <= 0 || price.compareTo(BigDecimal.ZERO) < 0) throw new Exception();
        } catch (Exception ex) {
            NotificationToast.showError(this, "Quantity must be positive and price cannot be negative.");
            return;
        }

        if (desc.isEmpty()) {
            NotificationToast.showError(this, "Item description is required.");
            return;
        }

        BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
        InvoiceItem item = new InvoiceItem();
        item.setDescription(desc);
        item.setItemType(type);
        item.setQuantity(qty);
        item.setUnitPrice(price);
        item.setTotalPrice(total);

        itemsList.add(item);
        itemsModel.addRow(new Object[]{desc, type, qty, CurrencyUtil.format(price), CurrencyUtil.format(total)});

        recalculateTotals();
    }

    private void onRemoveItem() {
        int r = itemsTable.getSelectedRow();
        if (r < 0 || r >= itemsList.size()) return;
        itemsList.remove(r);
        itemsModel.removeRow(r);
        recalculateTotals();
    }

    private void recalculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItem it : itemsList) {
            subtotal = subtotal.add(it.getTotalPrice());
        }

        BigDecimal discPct = BigDecimal.ZERO;
        try {
            discPct = new BigDecimal(txtDiscountPercent.getText().trim());
        } catch (Exception ignored) {}

        BigDecimal taxPct = BigDecimal.ZERO;
        try {
            taxPct = new BigDecimal(txtTaxPercent.getText().trim());
        } catch (Exception ignored) {}

        BigDecimal discountAmt = subtotal.multiply(discPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxable = subtotal.subtract(discountAmt);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) taxable = BigDecimal.ZERO;

        BigDecimal taxAmt = taxable.multiply(taxPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = taxable.add(taxAmt);

        lblSubtotal.setText(CurrencyUtil.format(subtotal));
        lblGrandTotal.setText(CurrencyUtil.format(grandTotal));

        invoice.setSubtotal(subtotal);
        invoice.setDiscountPercent(discPct);
        invoice.setDiscountAmount(discountAmt);
        invoice.setTaxPercent(taxPct);
        invoice.setTaxAmount(taxAmt);
        invoice.setTotalAmount(grandTotal);
    }

    private void onSave() {
        int pIdx = cmbPatient.getSelectedIndex();
        if (pIdx < 0 || itemsList.isEmpty()) {
            NotificationToast.showError(this, "Patient selection and at least one line item are required.");
            return;
        }

        recalculateTotals();

        invoice.setPatientId(patients.get(pIdx).getPatientId());
        invoice.setIssueDate(LocalDate.now());
        invoice.setItems(itemsList);
        invoice.setPaymentStatus("UNPAID");
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(invoice.getTotalAmount());

        String adm = txtAdmissionNo.getText().trim();
        if (!adm.isEmpty()) invoice.setAdmissionNumber(adm);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Invoice getInvoice() { return invoice; }
}
