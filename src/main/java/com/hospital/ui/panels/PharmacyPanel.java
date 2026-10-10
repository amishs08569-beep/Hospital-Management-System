package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.DispenseDialog;
import com.hospital.ui.dialogs.MedicineDialog;
import com.hospital.ui.dialogs.StockReceiptDialog;
import com.hospital.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for medicine inventory, batch receipt, low-stock warnings,
 * and dispensing.
 */
public class PharmacyPanel extends JPanel {

    private final UserSession session;
    private final PlaceholderTextField txtSearch;
    private final JCheckBox chkLowStockOnly;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<Medicine> currentMedicines = new ArrayList<>();

    public PharmacyPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        txtSearch = new PlaceholderTextField("Search Medication Name, Category, Formula...", 24);
        txtSearch.setPreferredSize(new Dimension(280, 36));

        chkLowStockOnly = new JCheckBox("Low Stock Only", false);
        chkLowStockOnly.setFont(UIHelper.FONT_BODY);
        chkLowStockOnly.setOpaque(false);
        chkLowStockOnly.addActionListener(e -> loadMedicines());

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnSearch.addActionListener(e -> loadMedicines());

        ModernButton btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            chkLowStockOnly.setSelected(false);
            loadMedicines();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(chkLowStockOnly);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("➕ Add Medicine", ModernButton.ButtonType.SUCCESS);
        btnNew.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_MEDICINES));
        btnNew.addActionListener(e -> onNewMedicine());

        ModernButton btnReceive = new ModernButton("📥 Stock Inward", ModernButton.ButtonType.PRIMARY);
        btnReceive.setEnabled(session.hasPermission(RolePermission.Permission.MANAGE_INVENTORY));
        btnReceive.addActionListener(e -> onReceiveStock());

        ModernButton btnDispense = new ModernButton("💊 Dispense", ModernButton.ButtonType.SECONDARY);
        btnDispense.setEnabled(session.hasPermission(RolePermission.Permission.DISPENSE_MEDICINE));
        btnDispense.addActionListener(e -> onDispense());

        actionPanel.add(btnNew);
        actionPanel.add(btnReceive);
        actionPanel.add(btnDispense);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = { "Item Code", "Medicine Name", "Category", "Dosage Form", "Unit Price", "Stock Qty",
                "Reorder Level", "Inventory Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadMedicines();
    }

    public void loadMedicines() {
        String query = txtSearch.getText().trim();
        boolean lowStock = chkLowStockOnly.isSelected();

        AppController.executeAsync(
                () -> AppController.getInstance().getPharmacyService().listMedicines(query.isEmpty() ? null : query,
                        null, lowStock),
                list -> {
                    currentMedicines.clear();
                    currentMedicines.addAll(list);
                    tableModel.setRowCount(0);
                    for (Medicine m : list) {
                        String status = "IN STOCK";
                        if (m.getTotalStock() <= 0) {
                            status = "OUT OF STOCK";
                        } else if (m.getTotalStock() <= m.getReorderLevel()) {
                            status = "LOW STOCK";
                        }

                        tableModel.addRow(new Object[] {
                                m.getMedicineCode(),
                                m.getMedicineName() + " (" + m.getStrength() + ")",
                                m.getCategory(),
                                m.getUnitType(),
                                CurrencyUtil.format(m.getSellingPrice()),
                                m.getTotalStock(),
                                m.getReorderLevel(),
                                status
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load medicine inventory: " + error.getMessage()));
    }

    private void onNewMedicine() {
        MedicineDialog d = new MedicineDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) {
            Medicine m = d.getMedicine();
            AppController.executeAsync(
                    () -> AppController.getInstance().getPharmacyService().addMedicine(m),
                    res -> {
                        NotificationToast.showSuccess(this, "Medication added! Code: " + res.getMedicineCode());
                        loadMedicines();
                    },
                    error -> NotificationToast.showError(this, "Failed to register medicine: " + error.getMessage()));
        }
    }

    private void onReceiveStock() {
        StockReceiptDialog d = new StockReceiptDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            MedicineBatch b = d.getBatch();
            AppController.executeAsync(
                    () -> AppController.getInstance().getPharmacyService().addBatch(b),
                    res -> {
                        NotificationToast.showSuccess(this,
                                "Stock batch received successfully! Batch #" + res.getBatchNumber());
                        loadMedicines();
                    },
                    error -> NotificationToast.showError(this, "Failed to receive stock: " + error.getMessage()));
        }
    }

    private void onDispense() {
        DispenseDialog d = new DispenseDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isDispensed()) {
            int batchId = d.getSelectedBatchId();
            int qty = d.getQuantityToDispense();
            String ref = d.getReferenceNo();
            String notes = d.getDispenseNotes();

            AppController.executeAsync(
                    () -> {
                        AppController.getInstance().getPharmacyService().dispenseStock(batchId, qty, ref, notes);
                        return true;
                    },
                    res -> {
                        NotificationToast.showSuccess(this, "Dispensed " + qty + " units successfully.");
                        loadMedicines();
                    },
                    error -> NotificationToast.showError(this, "Dispensing failed: " + error.getMessage()));
        }
    }
}
