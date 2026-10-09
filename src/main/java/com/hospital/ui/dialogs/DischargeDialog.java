package com.hospital.ui.dialogs;

import com.hospital.model.Admission;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Dialog to finalize inpatient discharge and free allocated bed.
 */
public class DischargeDialog extends JDialog {

    private final Admission admission;
    private final JTextArea txtSummary;
    private boolean confirmed = false;
    private String dischargeSummary;

    public DischargeDialog(Window owner, Admission admission) {
        super(owner, "Discharge Patient: " + admission.getPatientName() + " (" + admission.getAdmissionNumber() + ")", ModalityType.APPLICATION_MODAL);
        this.admission = admission;

        setSize(550, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(UIHelper.BACKGROUND);

        JPanel info = new JPanel(new GridLayout(2, 2, 8, 4));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(10, 12, 10, 12));

        info.add(new JLabel("Admission #: " + admission.getAdmissionNumber()));
        info.add(new JLabel("Ward / Bed: " + admission.getWardName() + " (" + admission.getBedNumber() + ")"));
        info.add(new JLabel("Admitted: " + DateUtil.formatDateTime(admission.getAdmissionDate())));
        info.add(new JLabel("Attending: " + admission.getDoctorName()));

        content.add(info);
        content.add(Box.createVerticalStrut(14));

        JLabel lblSum = new JLabel("Discharge Clinical Summary & Instructions *");
        lblSum.setFont(UIHelper.FONT_HEADER);
        content.add(lblSum);
        content.add(Box.createVerticalStrut(6));

        txtSummary = new JTextArea(5, 20);
        txtSummary.setLineWrap(true);
        txtSummary.setText("Patient recovered and stable for discharge. Routine follow-up in 10 days.");
        content.add(new JScrollPane(txtSummary));

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnConfirm = new ModernButton("Finalize Discharge", ModernButton.ButtonType.PRIMARY);
        btnConfirm.addActionListener(e -> onConfirm());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onConfirm() {
        String sum = txtSummary.getText().trim();
        if (sum.isEmpty()) {
            NotificationToast.showError(this, "Please provide a discharge summary.");
            return;
        }

        this.dischargeSummary = sum;
        this.confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public String getDischargeSummary() { return dischargeSummary; }
}
