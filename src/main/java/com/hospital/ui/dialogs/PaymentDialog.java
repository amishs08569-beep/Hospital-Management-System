package com.hospital.ui.dialogs;

import com.hospital.model.Invoice;
import com.hospital.model.Payment;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Dialog to collect patient invoice payments (cash, card, insurance, UPI).
 */
public class PaymentDialog extends JDialog {

    private final Invoice invoice;
    private final PlaceholderTextField txtAmount;
    private final JComboBox<String> cmbMethod;
    private final PlaceholderTextField txtRef;
    private final PlaceholderTextField txtNotes;

    private boolean saved = false;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private String txnRef;
    private String notes;

    public PaymentDialog(Window owner, Invoice invoice) {
        super(owner, "Record Payment: " + invoice.getInvoiceNumber(), ModalityType.APPLICATION_MODAL);
        this.invoice = invoice;

        setSize(540, 460);
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
        info.add(new JLabel("Patient: " + invoice.getPatientName()));
        info.add(new JLabel("Total Bill: " + CurrencyUtil.format(invoice.getTotalAmount())));
        info.add(new JLabel("Already Paid: " + CurrencyUtil.format(invoice.getPaidAmount())));
        JLabel lblBal = new JLabel("Balance Due: " + CurrencyUtil.format(invoice.getBalanceAmount()));
        lblBal.setFont(UIHelper.FONT_BOLD);
        lblBal.setForeground(new Color(220, 38, 38));
        info.add(lblBal);

        content.add(info);
        content.add(Box.createVerticalStrut(14));

        txtAmount = new PlaceholderTextField(invoice.getBalanceAmount().toString());
        cmbMethod = new JComboBox<>(new String[]{"CASH", "CREDIT_CARD", "DEBIT_CARD", "UPI / DIGITAL", "INSURANCE", "BANK_TRANSFER"});
        txtRef = new PlaceholderTextField("Transaction reference / Receipt # (e.g. TXN-9481)");
        txtNotes = new PlaceholderTextField("Payment notes (Optional)");

        content.add(UIHelper.createFormRow("Payment Amount *", txtAmount));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Payment Method *", cmbMethod));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Transaction Ref #", txtRef));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Remarks", txtNotes));

        add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Process Payment", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String amtStr = txtAmount.getText().trim();
        BigDecimal amt;
        try {
            amt = new BigDecimal(amtStr);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                NotificationToast.showError(this, "Payment amount must be greater than zero.");
                return;
            }
            if (amt.compareTo(invoice.getBalanceAmount()) > 0) {
                NotificationToast.showError(this, "Payment amount cannot exceed balance due (" + CurrencyUtil.format(invoice.getBalanceAmount()) + ").");
                return;
            }
        } catch (Exception e) {
            NotificationToast.showError(this, "Please enter a valid numeric payment amount.");
            return;
        }

        this.paidAmount = amt;
        this.paymentMethod = (String) cmbMethod.getSelectedItem();
        this.txnRef = txtRef.getText().trim();
        this.notes = txtNotes.getText().trim();
        this.saved = true;

        dispose();
    }

    public boolean isSaved() { return saved; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTxnRef() { return txnRef; }
    public String getNotes() { return notes; }
}
