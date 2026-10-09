package com.hospital.ui.dialogs;

import com.hospital.controller.AppController;
import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Dialog for entering clinical laboratory test results.
 */
public class LabResultDialog extends JDialog {

    private final LabOrder order;
    private final PlaceholderTextField txtResultValue;
    private final PlaceholderTextField txtUnits;
    private final PlaceholderTextField txtRefRange;
    private final JComboBox<String> cmbFlag;
    private final JTextArea txtRemarks;

    private boolean saved = false;
    private LabResult result = new LabResult();

    public LabResultDialog(Window owner, LabOrder order) {
        super(owner, "Enter Laboratory Test Results: " + order.getOrderNumber(), ModalityType.APPLICATION_MODAL);
        this.order = order;

        setSize(560, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        JPanel banner = new JPanel(new GridLayout(2, 2, 8, 4));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new EmptyBorder(10, 12, 10, 12));
        banner.add(new JLabel("Order: " + order.getOrderNumber()));
        banner.add(new JLabel("Patient: " + order.getPatientName()));
        banner.add(new JLabel("Doctor: " + order.getDoctorName()));
        banner.add(new JLabel("Priority: " + order.getPriority()));

        content.add(banner);
        content.add(Box.createVerticalStrut(12));

        txtResultValue = new PlaceholderTextField("Diagnostic Finding / Measured Value (e.g. 112)");
        txtUnits = new PlaceholderTextField("Measurement Unit (e.g. mg/dL, mmol/L, %)");
        txtRefRange = new PlaceholderTextField("Normal Reference Range (e.g. 70 - 99 mg/dL)");
        cmbFlag = new JComboBox<>(new String[]{"NORMAL", "ABNORMAL", "CRITICAL"});
        txtRemarks = new JTextArea(3, 20);
        txtRemarks.setLineWrap(true);
        txtRemarks.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        content.add(UIHelper.createFormRow("Result Value *", txtResultValue));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Unit of Measure", txtUnits));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Reference Range", txtRefRange));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Result Flag", cmbFlag));
        content.add(Box.createVerticalStrut(10));

        JPanel pnlRem = new JPanel(new BorderLayout(8, 2));
        pnlRem.setOpaque(false);
        JLabel lblRem = new JLabel("Pathologist Remarks");
        lblRem.setFont(UIHelper.FONT_BODY);
        lblRem.setPreferredSize(new Dimension(140, 20));
        pnlRem.add(lblRem, BorderLayout.WEST);
        pnlRem.add(new JScrollPane(txtRemarks), BorderLayout.CENTER);
        content.add(pnlRem);

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Results & Complete", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String val = txtResultValue.getText().trim();
        if (val.isEmpty()) {
            NotificationToast.showError(this, "Result value is required.");
            return;
        }

        result.setOrderId(order.getOrderId());
        result.setResultDate(LocalDateTime.now());
        result.setResultValue(val);
        result.setUnits(txtUnits.getText().trim());
        result.setReferenceRange(txtRefRange.getText().trim());
        result.setAbnormalFlag((String) cmbFlag.getSelectedItem());
        result.setRemarks(txtRemarks.getText().trim());
        result.setTechnicianNotes("Processed and verified by Lab Staff");

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public LabResult getResult() { return result; }
}
