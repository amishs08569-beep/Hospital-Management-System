package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.model.LabTest;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.LabOrderDialog;
import com.hospital.ui.dialogs.LabResultDialog;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;
import com.hospital.util.PdfGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Management panel for laboratory test catalog, orders, and diagnostic results.
 */
public class LabPanel extends JPanel {

    private final UserSession session;
    private final JTabbedPane tabbedPane;

    // Orders Tab
    private final JComboBox<String> cmbStatusFilter;
    private final JTable orderTable;
    private final DefaultTableModel orderModel;
    private final List<LabOrder> currentOrders = new ArrayList<>();

    // Catalog Tab
    private final JTable catalogTable;
    private final DefaultTableModel catalogModel;

    public LabPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FONT_HEADER);

        // --- TAB 1: ORDERS ---
        JPanel pnlOrders = new JPanel(new BorderLayout(0, 12));
        pnlOrders.setBackground(UIHelper.BACKGROUND);
        pnlOrders.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL ORDERS", "REQUESTED", "SAMPLE_COLLECTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"});
        cmbStatusFilter.setPreferredSize(new Dimension(160, 36));
        ModernButton btnFilter = new ModernButton("Filter", ModernButton.ButtonType.PRIMARY);
        btnFilter.addActionListener(e -> loadOrders());
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(btnFilter);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("➕ Order Lab Test", ModernButton.ButtonType.SUCCESS);
        btnNew.setEnabled(session.hasPermission(RolePermission.Permission.REQUEST_LAB_TEST));
        btnNew.addActionListener(e -> onNewOrder());

        ModernButton btnProgress = new ModernButton("🔬 In Progress", ModernButton.ButtonType.SECONDARY);
        btnProgress.setEnabled(session.hasPermission(RolePermission.Permission.ENTER_LAB_RESULT));
        btnProgress.addActionListener(e -> onUpdateStatus("IN_PROGRESS"));

        ModernButton btnEnterResult = new ModernButton("📝 Enter Results", ModernButton.ButtonType.PRIMARY);
        btnEnterResult.setEnabled(session.hasPermission(RolePermission.Permission.ENTER_LAB_RESULT));
        btnEnterResult.addActionListener(e -> onEnterResult());

        ModernButton btnPdf = new ModernButton("📄 Report PDF", ModernButton.ButtonType.SECONDARY);
        btnPdf.addActionListener(e -> onExportPdf());

        actionPanel.add(btnNew);
        actionPanel.add(btnProgress);
        actionPanel.add(btnEnterResult);
        actionPanel.add(btnPdf);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        pnlOrders.add(topBar, BorderLayout.NORTH);

        String[] orderCols = {"Order #", "Date", "Patient Name", "Ordering Doctor", "Priority", "Status"};
        orderModel = new DefaultTableModel(orderCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(orderModel);
        UIHelper.styleTable(orderTable);
        pnlOrders.add(UIHelper.createModernScrollPane(orderTable), BorderLayout.CENTER);

        // --- TAB 2: CATALOG ---
        JPanel pnlCatalog = new JPanel(new BorderLayout(0, 12));
        pnlCatalog.setBackground(UIHelper.BACKGROUND);
        pnlCatalog.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] catCols = {"Test Code", "Test Name", "Category", "Price", "Normal Range", "Units"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        catalogTable = new JTable(catalogModel);
        UIHelper.styleTable(catalogTable);
        pnlCatalog.add(UIHelper.createModernScrollPane(catalogTable), BorderLayout.CENTER);

        tabbedPane.addTab("Laboratory Test Orders", pnlOrders);
        tabbedPane.addTab("Diagnostic Test Catalog", pnlCatalog);

        add(tabbedPane, BorderLayout.CENTER);

        loadOrders();
        loadCatalog();
    }

    public void loadOrders() {
        String sel = (String) cmbStatusFilter.getSelectedItem();
        String filter = "ALL ORDERS".equals(sel) ? null : sel;

        AppController.executeAsync(
                () -> AppController.getInstance().getLabService().listOrders(filter, null),
                list -> {
                    currentOrders.clear();
                    currentOrders.addAll(list);
                    orderModel.setRowCount(0);
                    for (LabOrder o : list) {
                        orderModel.addRow(new Object[]{
                                o.getOrderNumber(),
                                DateUtil.formatDateTime(o.getOrderDate()),
                                o.getPatientName(),
                                o.getDoctorName(),
                                o.getPriority(),
                                o.getStatus()
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load lab orders: " + error.getMessage())
        );
    }

    public void loadCatalog() {
        AppController.executeAsync(
                () -> AppController.getInstance().getLabService().listTests(true),
                list -> {
                    catalogModel.setRowCount(0);
                    for (LabTest t : list) {
                        catalogModel.addRow(new Object[]{
                                t.getTestCode(),
                                t.getTestName(),
                                t.getCategory(),
                                CurrencyUtil.format(t.getPrice()),
                                t.getNormalRange() != null ? t.getNormalRange() : "-",
                                t.getUnits() != null ? t.getUnits() : "-"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load test catalog: " + error.getMessage())
        );
    }

    private LabOrder getSelected() {
        int r = orderTable.getSelectedRow();
        if (r < 0 || r >= currentOrders.size()) {
            NotificationToast.showWarning(this, "Please select an order from the table first.");
            return null;
        }
        return currentOrders.get(r);
    }

    private void onNewOrder() {
        LabOrderDialog d = new LabOrderDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            LabOrder order = d.getLabOrder();
            List<Integer> testIds = d.getSelectedTestIds();
            AppController.executeAsync(
                    () -> AppController.getInstance().getLabService().createOrder(order, testIds),
                    created -> {
                        NotificationToast.showSuccess(this, "Lab test order submitted! Order #" + created.getOrderNumber());
                        loadOrders();
                    },
                    error -> NotificationToast.showError(this, "Failed to order tests: " + error.getMessage())
            );
        }
    }

    private void onUpdateStatus(String newStatus) {
        LabOrder sel = getSelected();
        if (sel == null) return;
        AppController.executeAsync(
                () -> AppController.getInstance().getLabService().updateOrderStatus(sel.getOrderId(), newStatus),
                success -> {
                    NotificationToast.showSuccess(this, "Order status set to " + newStatus);
                    loadOrders();
                },
                error -> NotificationToast.showError(this, "Failed to update status: " + error.getMessage())
        );
    }

    private void onEnterResult() {
        LabOrder sel = getSelected();
        if (sel == null) return;
        LabResultDialog d = new LabResultDialog(SwingUtilities.getWindowAncestor(this), sel);
        d.setVisible(true);
        if (d.isSaved()) {
            LabResult res = d.getResult();
            AppController.executeAsync(
                    () -> {
                        AppController.getInstance().getLabService().enterResult(res);
                        AppController.getInstance().getLabService().updateOrderStatus(sel.getOrderId(), "COMPLETED");
                        return true;
                    },
                    done -> {
                        NotificationToast.showSuccess(this, "Results recorded and order completed!");
                        loadOrders();
                    },
                    error -> NotificationToast.showError(this, "Failed to save results: " + error.getMessage())
            );
        }
    }

    private void onExportPdf() {
        LabOrder sel = getSelected();
        if (sel == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("LabReport_" + sel.getOrderNumber() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            AppController.executeAsync(
                    () -> {
                        // Fetch fresh order details with results
                        LabOrder fresh = AppController.getInstance().getLabService().getOrderById(sel.getOrderId()).orElse(sel);
                        PdfGenerator.generateLabReportPdf(fresh, target);
                        return target;
                    },
                    file -> {
                        NotificationToast.showSuccess(this, "Lab Report PDF exported to: " + file.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        } catch (Exception ignored) {}
                    },
                    error -> NotificationToast.showError(this, "PDF export failed: " + error.getMessage())
            );
        }
    }
}
