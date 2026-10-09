package com.hospital.ui;

import com.hospital.config.AppConfig;
import com.hospital.config.DatabaseConfig;
import com.hospital.controller.AppController;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.StatusBadge;
import com.hospital.ui.components.UIHelper;
import com.hospital.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window featuring sidebar navigation, role-based module views, and status reporting.
 */
public class MainFrame extends JFrame {

    private final UserSession session;
    private final CardLayout cardLayout;
    private final JPanel contentArea;
    private final Map<String, ModernButton> navButtons = new HashMap<>();

    private final JLabel lblModuleTitle;
    private final JLabel lblModuleSub;
    private String activeModule = "DASHBOARD";

    public MainFrame(UserSession session) {
        this.session = session;
        setTitle(AppConfig.getHospitalName() + " - Management Information System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 850);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.BACKGROUND);

        // ==========================================
        // LEFT SIDEBAR
        // ==========================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(24, 43, 73)); // Deep Hospital Navy

        // Sidebar Brand
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel lblLogo = new JLabel("🏥 APEX CARE");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblTag = new JLabel("HOSPITAL MANAGEMENT");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTag.setForeground(new Color(147, 197, 253));

        brandPanel.add(lblLogo);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblTag);
        brandPanel.add(Box.createVerticalStrut(14));

        // User Profile Card in Sidebar
        JPanel userCard = new JPanel(new BorderLayout(8, 4));
        userCard.setBackground(new Color(30, 58, 98));
        userCard.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblUserIcon = new JLabel("👤");
        lblUserIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        userCard.add(lblUserIcon, BorderLayout.WEST);

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel lblUserName = new JLabel(session.getUser() != null ? session.getUser().getFullName() : session.getUsername());
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserName.setForeground(Color.WHITE);

        StatusBadge badge = new StatusBadge(session.getRoleName());

        userInfo.add(lblUserName);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(badge);
        userCard.add(userInfo, BorderLayout.CENTER);

        brandPanel.add(userCard);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Menu Navigation
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        addNavButton(menuPanel, "📊 Dashboard", "DASHBOARD", true);

        // Role-based navigation items
        boolean isAdmin = session.getRole() == RolePermission.RoleType.ADMIN;
        boolean isDoctor = session.getRole() == RolePermission.RoleType.DOCTOR;
        boolean isReceptionist = session.getRole() == RolePermission.RoleType.RECEPTIONIST;
        boolean isPharmacist = session.getRole() == RolePermission.RoleType.PHARMACIST;
        boolean isLabStaff = session.getRole() == RolePermission.RoleType.LAB_STAFF;
        boolean isBillingStaff = session.getRole() == RolePermission.RoleType.BILLING_STAFF;

        if (isAdmin || isDoctor || isReceptionist) {
            addNavButton(menuPanel, "👥 Patients", "PATIENTS", true);
        }
        if (isAdmin) {
            addNavButton(menuPanel, "👨‍⚕️ Doctors & Staff", "DOCTORS", true);
        }
        if (isAdmin || isDoctor || isReceptionist) {
            addNavButton(menuPanel, "📅 Appointments", "APPOINTMENTS", true);
        }
        if (isAdmin || isDoctor || isReceptionist) {
            addNavButton(menuPanel, "🛏️ Admissions & Beds", "ADMISSIONS", true);
        }
        if (isAdmin || isDoctor) {
            addNavButton(menuPanel, "📋 Medical Records", "RECORDS", true);
        }
        if (isAdmin || isDoctor || isPharmacist) {
            addNavButton(menuPanel, "💊 Prescriptions", "PRESCRIPTIONS", true);
        }
        if (isAdmin || isDoctor || isLabStaff) {
            addNavButton(menuPanel, "🧪 Laboratory Tests", "LAB", true);
        }
        if (isAdmin || isPharmacist) {
            addNavButton(menuPanel, "🏥 Pharmacy Stock", "PHARMACY", true);
        }
        if (isAdmin || isBillingStaff) {
            addNavButton(menuPanel, "💳 Invoices & Billing", "BILLING", true);
        }
        if (isAdmin || isBillingStaff) {
            addNavButton(menuPanel, "📈 Reports & Exports", "REPORTS", true);
        }
        if (isAdmin) {
            addNavButton(menuPanel, "🛡️ System Audit Trail", "AUDIT", true);
            addNavButton(menuPanel, "⚙️ User Accounts", "USERS", true);
        }

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(null);
        sidebar.add(menuScroll, BorderLayout.CENTER);

        // Sidebar Footer: Logout
        JPanel sidebarFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        sidebarFooter.setOpaque(false);
        ModernButton btnLogout = new ModernButton("🚪 Sign Out", ModernButton.ButtonType.DANGER);
        btnLogout.setPreferredSize(new Dimension(220, 38));
        btnLogout.addActionListener(e -> onLogout());
        sidebarFooter.add(btnLogout);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // ==========================================
        // MAIN BODY (HEADER + CONTENT + STATUSBAR)
        // ==========================================
        JPanel mainBody = new JPanel(new BorderLayout());
        mainBody.setOpaque(false);

        // Header Bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(Color.WHITE);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        lblModuleTitle = new JLabel("Executive Dashboard");
        lblModuleTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblModuleTitle.setForeground(UIHelper.TEXT_DARK);

        lblModuleSub = new JLabel("Real-time clinical, operational, and financial telemetry");
        lblModuleSub.setFont(UIHelper.FONT_SUBTITLE);
        lblModuleSub.setForeground(UIHelper.TEXT_MUTED);

        headerLeft.add(lblModuleTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblModuleSub);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        JLabel lblDateTime = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        lblDateTime.setFont(UIHelper.FONT_BODY);
        lblDateTime.setForeground(UIHelper.TEXT_MUTED);
        headerRight.add(lblDateTime);

        headerBar.add(headerLeft, BorderLayout.WEST);
        headerBar.add(headerRight, BorderLayout.EAST);
        mainBody.add(headerBar, BorderLayout.NORTH);

        // Central Content Area with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Instantiate and add panels
        contentArea.add(new DashboardPanel(session), "DASHBOARD");
        if (isAdmin || isDoctor || isReceptionist) contentArea.add(new PatientPanel(session), "PATIENTS");
        if (isAdmin) contentArea.add(new DoctorStaffPanel(session), "DOCTORS");
        if (isAdmin || isDoctor || isReceptionist) contentArea.add(new AppointmentPanel(session), "APPOINTMENTS");
        if (isAdmin || isDoctor || isReceptionist) contentArea.add(new AdmissionPanel(session), "ADMISSIONS");
        if (isAdmin || isDoctor) contentArea.add(new MedicalRecordPanel(session), "RECORDS");
        if (isAdmin || isDoctor || isPharmacist) contentArea.add(new PrescriptionPanel(session), "PRESCRIPTIONS");
        if (isAdmin || isDoctor || isLabStaff) contentArea.add(new LabPanel(session), "LAB");
        if (isAdmin || isPharmacist) contentArea.add(new PharmacyPanel(session), "PHARMACY");
        if (isAdmin || isBillingStaff) contentArea.add(new BillingPanel(session), "BILLING");
        if (isAdmin || isBillingStaff) contentArea.add(new ReportPanel(session), "REPORTS");
        if (isAdmin) contentArea.add(new AuditLogPanel(session), "AUDIT");
        if (isAdmin) contentArea.add(new UserManagementPanel(session), "USERS");

        mainBody.add(contentArea, BorderLayout.CENTER);

        // Status Bar at Bottom
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR),
                new EmptyBorder(6, 16, 6, 16)
        ));

        String dbIndicator = DatabaseConfig.isH2Fallback() ?
                "🟡 Database: In-Memory Demo Mode (H2 MySQL Compatibility)" :
                "🟢 Database: MySQL 8.0 Server Online";
        JLabel lblDbStatus = new JLabel(dbIndicator);
        lblDbStatus.setFont(UIHelper.FONT_SMALL);

        JLabel lblUserStatus = new JLabel("Authenticated: " + session.getUsername() + " | Role: " + session.getRoleName());
        lblUserStatus.setFont(UIHelper.FONT_SMALL);
        lblUserStatus.setForeground(UIHelper.TEXT_MUTED);

        JLabel lblCopyright = new JLabel("Apex Care Hospital Management System v1.0.0");
        lblCopyright.setFont(UIHelper.FONT_SMALL);
        lblCopyright.setForeground(UIHelper.TEXT_MUTED);

        statusBar.add(lblDbStatus, BorderLayout.WEST);
        statusBar.add(lblUserStatus, BorderLayout.CENTER);
        statusBar.add(lblCopyright, BorderLayout.EAST);
        mainBody.add(statusBar, BorderLayout.SOUTH);

        root.add(mainBody, BorderLayout.CENTER);
        add(root);

        // Highlight initial button
        updateNavSelection("DASHBOARD");
    }

    private void addNavButton(JPanel container, String text, String cardName, boolean visible) {
        if (!visible) return;

        ModernButton btn = new ModernButton(text, ModernButton.ButtonType.SECONDARY);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(236, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.addActionListener(e -> navigateTo(cardName));

        navButtons.put(cardName, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private void navigateTo(String cardName) {
        this.activeModule = cardName;
        cardLayout.show(contentArea, cardName);
        updateNavSelection(cardName);
        updateHeaderInfo(cardName);
    }

    private void updateNavSelection(String activeKey) {
        for (Map.Entry<String, ModernButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(activeKey);
            ModernButton btn = entry.getValue();
            if (active) {
                btn.setBackground(new Color(37, 99, 235));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(30, 58, 98));
                btn.setForeground(new Color(226, 232, 240));
            }
        }
    }

    private void updateHeaderInfo(String cardName) {
        switch (cardName) {
            case "DASHBOARD":
                lblModuleTitle.setText("Clinical & Operations Dashboard");
                lblModuleSub.setText("Key performance metrics, live appointments, and bed telemetry");
                break;
            case "PATIENTS":
                lblModuleTitle.setText("Patient Records & Demographics");
                lblModuleSub.setText("Registration, medical histories, profile lookups, and patient tracking");
                break;
            case "DOCTORS":
                lblModuleTitle.setText("Medical Doctors & Clinical Staff");
                lblModuleSub.setText("Employee credentials, specialization, fee schedules, and duty shifts");
                break;
            case "APPOINTMENTS":
                lblModuleTitle.setText("Outpatient Clinic Appointments");
                lblModuleSub.setText("Consultation booking, conflict prevention, check-ins, and statuses");
                break;
            case "ADMISSIONS":
                lblModuleTitle.setText("Inpatient Admissions & Bed Allocation");
                lblModuleSub.setText("Ward capacity tracking, inpatient registration, and clinical discharge");
                break;
            case "RECORDS":
                lblModuleTitle.setText("Clinical Medical Records & Diagnoses");
                lblModuleSub.setText("Physician examination notes, clinical findings, and treatment plans");
                break;
            case "PRESCRIPTIONS":
                lblModuleTitle.setText("Medical Prescriptions");
                lblModuleSub.setText("Itemized medications, dosages, frequency, and PDF generation");
                break;
            case "LAB":
                lblModuleTitle.setText("Diagnostic Laboratory Management");
                lblModuleSub.setText("Test order dispatch, result entry, reference ranges, and diagnostic reports");
                break;
            case "PHARMACY":
                lblModuleTitle.setText("Pharmacy & Medication Inventory");
                lblModuleSub.setText("Drug catalog, batch tracking, expiry monitoring, and stock dispensing");
                break;
            case "BILLING":
                lblModuleTitle.setText("Hospital Invoicing & Payments");
                lblModuleSub.setText("Itemized invoice generation, tax & discounts, partial/full payments, receipts");
                break;
            case "REPORTS":
                lblModuleTitle.setText("Analytical Reports & Exports");
                lblModuleSub.setText("Operational, clinical, and financial summaries exported to CSV & PDF");
                break;
            case "AUDIT":
                lblModuleTitle.setText("Security Audit Trail");
                lblModuleSub.setText("System telemetry, user actions, and compliance logging");
                break;
            case "USERS":
                lblModuleTitle.setText("System User Accounts & Security");
                lblModuleSub.setText("Role-based access control, account provisioning, and password resets");
                break;
        }
    }

    private void onLogout() {
        if (NotificationToast.confirm(this, "Sign Out", "Are you sure you want to sign out of this session?")) {
            AppController.getInstance().getAuthService().logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
