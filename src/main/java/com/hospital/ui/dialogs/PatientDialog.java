package com.hospital.ui.dialogs;

import com.hospital.model.Patient;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.DateUtil;
import com.hospital.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Dialog for registering or updating patient records.
 */
public class PatientDialog extends JDialog {

    private final PlaceholderTextField txtName;
    private final PlaceholderTextField txtDob;
    private final JComboBox<String> cmbGender;
    private final JComboBox<String> cmbBloodGroup;
    private final PlaceholderTextField txtPhone;
    private final PlaceholderTextField txtEmail;
    private final JTextArea txtAddress;
    private final PlaceholderTextField txtEmergencyContact;
    private final PlaceholderTextField txtAllergies;
    private final JTextArea txtMedicalHistory;

    private Patient patient;
    private boolean saved = false;

    public PatientDialog(Window owner, Patient existing) {
        super(owner, existing == null ? "Register New Patient" : "Edit Patient Information", ModalityType.APPLICATION_MODAL);
        this.patient = existing != null ? existing : new Patient();

        setSize(650, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIHelper.BACKGROUND);

        txtName = new PlaceholderTextField("Full Legal Name (e.g. John Doe)");
        txtDob = new PlaceholderTextField("YYYY-MM-DD (e.g. 1990-05-15)");
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbBloodGroup = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Unknown"});
        txtPhone = new PlaceholderTextField("10-digit Phone Number");
        txtEmail = new PlaceholderTextField("email@example.com (Optional)");

        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        txtEmergencyContact = new PlaceholderTextField("Name and Phone (e.g. Jane Doe - 555-0199)");
        txtAllergies = new PlaceholderTextField("Known Allergies (e.g. Penicillin, Peanuts, None)");

        txtMedicalHistory = new JTextArea(3, 20);
        txtMedicalHistory.setLineWrap(true);
        txtMedicalHistory.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        content.add(UIHelper.createFormRow("Full Name *", txtName));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Date of Birth *", txtDob));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Gender", cmbGender));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Blood Group", cmbBloodGroup));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Phone Number *", txtPhone));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Email Address", txtEmail));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Emergency Contact", txtEmergencyContact));
        content.add(Box.createVerticalStrut(10));
        content.add(UIHelper.createFormRow("Known Allergies", txtAllergies));
        content.add(Box.createVerticalStrut(10));

        JPanel pnlAddr = new JPanel(new BorderLayout(8, 4));
        pnlAddr.setOpaque(false);
        JLabel lblAddr = new JLabel("Residential Address");
        lblAddr.setFont(UIHelper.FONT_BODY);
        lblAddr.setPreferredSize(new Dimension(140, 20));
        pnlAddr.add(lblAddr, BorderLayout.WEST);
        pnlAddr.add(new JScrollPane(txtAddress), BorderLayout.CENTER);
        content.add(pnlAddr);
        content.add(Box.createVerticalStrut(10));

        JPanel pnlHist = new JPanel(new BorderLayout(8, 4));
        pnlHist.setOpaque(false);
        JLabel lblHist = new JLabel("Medical History");
        lblHist.setFont(UIHelper.FONT_BODY);
        lblHist.setPreferredSize(new Dimension(140, 20));
        pnlHist.add(lblHist, BorderLayout.WEST);
        pnlHist.add(new JScrollPane(txtMedicalHistory), BorderLayout.CENTER);
        content.add(pnlHist);

        // Prepopulate if editing
        if (existing != null) {
            txtName.setText(existing.getFullName());
            txtDob.setText(existing.getDateOfBirth() != null ? existing.getDateOfBirth().toString() : "");
            cmbGender.setSelectedItem(existing.getGender());
            cmbBloodGroup.setSelectedItem(existing.getBloodGroup() != null ? existing.getBloodGroup() : "Unknown");
            txtPhone.setText(existing.getContactNumber());
            txtEmail.setText(existing.getEmail());
            txtEmergencyContact.setText(existing.getEmergencyContact());
            txtAllergies.setText(existing.getAllergies());
            txtAddress.setText(existing.getAddress());
            txtMedicalHistory.setText(existing.getMedicalHistory());
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSave = new ModernButton("Save Patient", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String dobStr = txtDob.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            NotificationToast.showError(this, "Full Name is required.");
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
            if (dob.isAfter(LocalDate.now())) {
                NotificationToast.showError(this, "Date of birth cannot be in the future.");
                return;
            }
        } catch (Exception e) {
            NotificationToast.showError(this, "Please enter a valid Date of Birth in YYYY-MM-DD format.");
            return;
        }

        if (phone.isEmpty()) {
            NotificationToast.showError(this, "Contact phone number is required.");
            return;
        }

        patient.setFullName(name);
        patient.setDateOfBirth(dob);
        patient.setGender((String) cmbGender.getSelectedItem());
        patient.setBloodGroup((String) cmbBloodGroup.getSelectedItem());
        patient.setContactNumber(phone);
        patient.setEmail(txtEmail.getText().trim());
        patient.setAddress(txtAddress.getText().trim());
        patient.setEmergencyContact(txtEmergencyContact.getText().trim());
        patient.setAllergies(txtAllergies.getText().trim());
        patient.setMedicalHistory(txtMedicalHistory.getText().trim());

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Patient getPatient() {
        return patient;
    }
}
