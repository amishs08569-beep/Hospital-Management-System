package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.dialogs.MedicalRecordDialog;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to manage and view patient medical records, clinical notes, and treatment histories.
 */
public class MedicalRecordPanel extends JPanel {

    private final UserSession session;
    private final JComboBox<String> cmbPatients;
    private final JTable recordTable;
    private final DefaultTableModel tableModel;
    private final JTextArea txtDetailArea;

    private final List<Patient> patientList = new ArrayList<>();
    private final List<MedicalRecord> currentRecords = new ArrayList<>();

    public MedicalRecordPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Filter & Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        cmbPatients = new JComboBox<>();
        cmbPatients.setPreferredSize(new Dimension(300, 36));
        cmbPatients.addActionListener(e -> loadRecordsForSelectedPatient());

        ModernButton btnReloadPatients = new ModernButton("🔄 Refresh Patients", ModernButton.ButtonType.SECONDARY);
        btnReloadPatients.addActionListener(e -> loadPatients());

        filterPanel.add(new JLabel("Patient:"));
        filterPanel.add(cmbPatients);
        filterPanel.add(btnReloadPatients);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNewRecord = new ModernButton("➕ Document Clinical Record", ModernButton.ButtonType.SUCCESS);
        btnNewRecord.setEnabled(session.hasPermission(RolePermission.Permission.CREATE_MEDICAL_RECORD));
        btnNewRecord.addActionListener(e -> onNewRecord());

        actionPanel.add(btnNewRecord);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Split: Records Table on top, Clinical Note details below
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);

        String[] cols = {"Record #", "Date", "Patient Name", "Consulting Doctor", "Diagnosis", "Follow-up Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recordTable = new JTable(tableModel);
        UIHelper.styleTable(recordTable);
        recordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailView();
            }
        });

        splitPane.setTopComponent(UIHelper.createModernScrollPane(recordTable));

        // Detail View
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 6));
        pnlDetail.setBackground(Color.WHITE);
        pnlDetail.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblDetailTitle = new JLabel("Clinical Notes & Treatment Details");
        lblDetailTitle.setFont(UIHelper.FONT_HEADER);
        lblDetailTitle.setForeground(UIHelper.TEXT_DARK);
        pnlDetail.add(lblDetailTitle, BorderLayout.NORTH);

        txtDetailArea = new JTextArea();
        txtDetailArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtDetailArea.setEditable(false);
        txtDetailArea.setLineWrap(true);
        pnlDetail.add(new JScrollPane(txtDetailArea), BorderLayout.CENTER);

        splitPane.setBottomComponent(pnlDetail);
        add(splitPane, BorderLayout.CENTER);

        loadPatients();
    }

    private void loadPatients() {
        AppController.executeAsync(
                () -> AppController.getInstance().getPatientService().searchPatients(null, false),
                list -> {
                    patientList.clear();
                    patientList.addAll(list);
                    cmbPatients.removeAllItems();
                    for (Patient p : list) {
                        cmbPatients.addItem(p.getFullName() + " (" + p.getPatientCode() + ")");
                    }
                    if (!list.isEmpty()) {
                        cmbPatients.setSelectedIndex(0);
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load patients: " + error.getMessage())
        );
    }

    private void loadRecordsForSelectedPatient() {
        int idx = cmbPatients.getSelectedIndex();
        if (idx < 0 || idx >= patientList.size()) return;

        Patient p = patientList.get(idx);
        AppController.executeAsync(
                () -> AppController.getInstance().getMedicalRecordService().getPatientHistory(p.getPatientId()),
                records -> {
                    currentRecords.clear();
                    currentRecords.addAll(records);
                    tableModel.setRowCount(0);
                    txtDetailArea.setText("Select a clinical record above to review full diagnosis and treatment.");
                    for (MedicalRecord r : records) {
                        tableModel.addRow(new Object[]{
                                r.getRecordNumber(),
                                DateUtil.formatDate(r.getRecordDate()),
                                r.getPatientName(),
                                r.getDoctorName(),
                                r.getDiagnosis(),
                                r.getFollowUpDate() != null ? DateUtil.formatDate(r.getFollowUpDate()) : "-"
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load clinical records: " + error.getMessage())
        );
    }

    private void updateDetailView() {
        int r = recordTable.getSelectedRow();
        if (r < 0 || r >= currentRecords.size()) {
            txtDetailArea.setText("");
            return;
        }

        MedicalRecord rec = currentRecords.get(r);
        StringBuilder sb = new StringBuilder();
        sb.append("RECORD NUMBER: ").append(rec.getRecordNumber()).append("\n");
        sb.append("DATE: ").append(DateUtil.formatDate(rec.getRecordDate())).append("\n");
        sb.append("DOCTOR: ").append(rec.getDoctorName()).append("\n");
        sb.append("PRIMARY DIAGNOSIS: ").append(rec.getDiagnosis()).append("\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append("PRESENTING SYMPTOMS:\n").append(rec.getSymptoms() != null ? rec.getSymptoms() : "-").append("\n\n");
        sb.append("PHYSICAL / CLINICAL EXAMINATION:\n").append(rec.getPhysicalExamination() != null ? rec.getPhysicalExamination() : "-").append("\n\n");
        sb.append("TREATMENT PLAN:\n").append(rec.getTreatmentPlan() != null ? rec.getTreatmentPlan() : "-").append("\n\n");
        sb.append("FOLLOW-UP INSTRUCTIONS:\n").append(rec.getFollowUpInstructions() != null ? rec.getFollowUpInstructions() : "-").append("\n");
        if (rec.getFollowUpDate() != null) {
            sb.append("NEXT APPOINTMENT SCHEDULED FOR: ").append(DateUtil.formatDate(rec.getFollowUpDate())).append("\n");
        }

        txtDetailArea.setText(sb.toString());
        txtDetailArea.setCaretPosition(0);
    }

    private void onNewRecord() {
        MedicalRecordDialog d = new MedicalRecordDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) {
            MedicalRecord rec = d.getRecord();
            AppController.executeAsync(
                    () -> AppController.getInstance().getMedicalRecordService().createMedicalRecord(rec),
                    res -> {
                        NotificationToast.showSuccess(this, "Medical record created! Token: " + res.getRecordNumber());
                        loadRecordsForSelectedPatient();
                    },
                    error -> NotificationToast.showError(this, "Failed to create medical record: " + error.getMessage())
            );
        }
    }
}
