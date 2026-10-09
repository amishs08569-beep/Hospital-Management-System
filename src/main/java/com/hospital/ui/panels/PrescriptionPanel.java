package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Prescription;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.PrescriptionDialog;
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
 * Management panel for issuing, viewing, and exporting medical prescriptions.
 */
public class PrescriptionPanel extends JPanel {

    private final UserSession session;
    private final JCheckBox chkPendingOnly;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<Prescription> currentPrescriptions = new ArrayList<>();

    public PrescriptionPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        chkPendingOnly = new JCheckBox("Pending Pharmacy Dispense Only", false);
        chkPendingOnly.setOpaque(false);
        chkPendingOnly.setFont(UIHelper.FONT_BODY);
        chkPendingOnly.addActionListener(e -> loadPrescriptions());

        ModernButton btnRefresh = new ModernButton("🔄 Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadPrescriptions());

        filterPanel.add(chkPendingOnly);
        filterPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("➕ Issue Prescription", ModernButton.ButtonType.SUCCESS);
        btnNew.setEnabled(session.hasPermission(RolePermission.Permission.CREATE_PRESCRIPTION));
        btnNew.addActionListener(e -> onNewPrescription());

        ModernButton btnPrint = new ModernButton("📄 Print / Export PDF", ModernButton.ButtonType.PRIMARY);
        btnPrint.addActionListener(e -> onExportPdf());

        ModernButton btnDispense = new ModernButton("💊 Mark Dispensed", ModernButton.ButtonType.SECONDARY);
        btnDispense.setEnabled(session.hasPermission(RolePermission.Permission.DISPENSE_MEDICINE));
        btnDispense.addActionListener(e -> onMarkDispensed());

        actionPanel.add(btnNew);
        actionPanel.add(btnPrint);
        actionPanel.add(btnDispense);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"Prescription #", "Date", "Patient Name", "Prescribing Doctor", "Diagnosis", "Items", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);

        add(UIHelper.createModernScrollPane(table), BorderLayout.CENTER);

        loadPrescriptions();
    }

    public void loadPrescriptions() {
        boolean pendingOnly = chkPendingOnly.isSelected();
        AppController.executeAsync(
                () -> AppController.getInstance().getPrescriptionService().listPrescriptions(pendingOnly),
                list -> {
                    currentPrescriptions.clear();
                    currentPrescriptions.addAll(list);
                    tableModel.setRowCount(0);
                    for (Prescription p : list) {
                        tableModel.addRow(new Object[]{
                                p.getPrescriptionNumber(),
                                DateUtil.formatDate(p.getPrescriptionDate()),
                                p.getPatientName(),
                                p.getDoctorName(),
                                p.getDiagnosis() != null ? p.getDiagnosis() : "-",
                                p.getItems() != null ? p.getItems().size() : 0,
                                p.isDispensed() ? "DISPENSED" : "PENDING DISPENSE"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load prescriptions: " + error.getMessage())
        );
    }

    private Prescription getSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= currentPrescriptions.size()) {
            NotificationToast.showWarning(this, "Please select a prescription first.");
            return null;
        }
        return currentPrescriptions.get(r);
    }

    private void onNewPrescription() {
        PrescriptionDialog d = new PrescriptionDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            Prescription p = d.getPrescription();
            AppController.executeAsync(
                    () -> AppController.getInstance().getPrescriptionService().createPrescription(p),
                    res -> {
                        NotificationToast.showSuccess(this, "Prescription created! ID: " + res.getPrescriptionNumber());
                        loadPrescriptions();
                    },
                    error -> NotificationToast.showError(this, "Failed to issue prescription: " + error.getMessage())
            );
        }
    }

    private void onExportPdf() {
        Prescription p = getSelected();
        if (p == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Prescription_" + p.getPrescriptionNumber() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            AppController.executeAsync(
                    () -> {
                        PdfGenerator.generatePrescriptionPdf(p, target);
                        return target;
                    },
                    file -> {
                        NotificationToast.showSuccess(this, "PDF generated successfully at: " + file.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        } catch (Exception ignored) {}
                    },
                    error -> NotificationToast.showError(this, "PDF generation failed: " + error.getMessage())
            );
        }
    }

    private void onMarkDispensed() {
        Prescription p = getSelected();
        if (p == null) return;

        if (p.isDispensed()) {
            NotificationToast.showInfo(this, "This prescription has already been dispensed.");
            return;
        }

        AppController.executeAsync(
                () -> AppController.getInstance().getPrescriptionService().markPrescriptionDispensed(p.getPrescriptionId()),
                success -> {
                    NotificationToast.showSuccess(this, "Prescription marked as dispensed.");
                    loadPrescriptions();
                },
                error -> NotificationToast.showError(this, "Failed to update status: " + error.getMessage())
        );
    }
}
