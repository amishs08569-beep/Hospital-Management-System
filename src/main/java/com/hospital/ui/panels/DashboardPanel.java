package com.hospital.ui.panels;

import com.hospital.controller.AppController;
import com.hospital.model.Admission;
import com.hospital.model.Appointment;
import com.hospital.model.DashboardStats;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.ModernCard;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.UIHelper;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Main analytics and executive overview dashboard.
 */
public class DashboardPanel extends JPanel {

    private final UserSession session;
    private final ModernCard cardPatients;
    private final ModernCard cardAppointments;
    private final ModernCard cardAdmissions;
    private final ModernCard cardLab;
    private final ModernCard cardLowStock;
    private final ModernCard cardExpiring;
    private final ModernCard cardUnpaid;
    private final ModernCard cardRevenue;

    private final JTable apptTable;
    private final DefaultTableModel apptModel;
    private final JTable admissionTable;
    private final DefaultTableModel admissionModel;

    public DashboardPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top bar with Welcome and Refresh
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome back, " + session.getUsername() + " (" + session.getRoleName() + ")");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(UIHelper.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Real-time clinical, operational, and financial telemetry");
        lblSubtitle.setFont(UIHelper.FONT_SUBTITLE);
        lblSubtitle.setForeground(UIHelper.TEXT_MUTED);

        welcomePanel.add(lblWelcome);
        welcomePanel.add(Box.createVerticalStrut(4));
        welcomePanel.add(lblSubtitle);

        ModernButton btnRefresh = new ModernButton("🔄 Refresh Metrics", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshData());

        topBar.add(welcomePanel, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Cards Grid (2 rows x 4 columns)
        JPanel cardsPanel = new JPanel(new GridLayout(2, 4, 14, 14));
        cardsPanel.setOpaque(false);

        cardPatients = new ModernCard("Total Patients", "...", "Registered in system", new Color(37, 99, 235));
        cardAppointments = new ModernCard("Today's Appointments", "...", "Scheduled today", new Color(16, 185, 129));
        cardAdmissions = new ModernCard("Active Admissions", "...", "Currently admitted", new Color(99, 102, 241));
        cardLab = new ModernCard("Pending Lab Orders", "...", "Awaiting testing", new Color(245, 158, 11));

        cardLowStock = new ModernCard("Low-Stock Medicines", "...", "Below reorder level", new Color(239, 68, 68));
        cardExpiring = new ModernCard("Expiring in 60 Days", "...", "Near-expiry alert", new Color(249, 115, 22));
        cardUnpaid = new ModernCard("Unpaid Invoices", "...", "Outstanding balances", new Color(236, 72, 153));
        cardRevenue = new ModernCard("Monthly Revenue", "...", "Collections this month", new Color(13, 148, 136));

        cardsPanel.add(cardPatients);
        cardsPanel.add(cardAppointments);
        cardsPanel.add(cardAdmissions);
        cardsPanel.add(cardLab);
        cardsPanel.add(cardLowStock);
        cardsPanel.add(cardExpiring);
        cardsPanel.add(cardUnpaid);
        cardsPanel.add(cardRevenue);

        // Center Split / Tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);

        // Left Table: Today's Appointments
        JPanel pnlAppts = new JPanel(new BorderLayout(0, 8));
        pnlAppts.setOpaque(false);
        JLabel lblApptTitle = new JLabel("Today's Appointments");
        lblApptTitle.setFont(UIHelper.FONT_HEADER);
        lblApptTitle.setForeground(UIHelper.TEXT_DARK);
        pnlAppts.add(lblApptTitle, BorderLayout.NORTH);

        String[] apptCols = {"Time", "Patient", "Doctor", "Status"};
        apptModel = new DefaultTableModel(apptCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        apptTable = new JTable(apptModel);
        UIHelper.styleTable(apptTable);
        pnlAppts.add(UIHelper.createModernScrollPane(apptTable), BorderLayout.CENTER);

        // Right Table: Active Inpatient Admissions
        JPanel pnlAdm = new JPanel(new BorderLayout(0, 8));
        pnlAdm.setOpaque(false);
        JLabel lblAdmTitle = new JLabel("Current Inpatient Admissions");
        lblAdmTitle.setFont(UIHelper.FONT_HEADER);
        lblAdmTitle.setForeground(UIHelper.TEXT_DARK);
        pnlAdm.add(lblAdmTitle, BorderLayout.NORTH);

        String[] admCols = {"Bed", "Patient", "Ward", "Admitted Date"};
        admissionModel = new DefaultTableModel(admCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        admissionTable = new JTable(admissionModel);
        UIHelper.styleTable(admissionTable);
        pnlAdm.add(UIHelper.createModernScrollPane(admissionTable), BorderLayout.CENTER);

        tablesPanel.add(pnlAppts);
        tablesPanel.add(pnlAdm);

        // Content layout
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.add(cardsPanel, BorderLayout.NORTH);
        centerContainer.add(tablesPanel, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        // Load data on display
        refreshData();
    }

    public void refreshData() {
        AppController.executeAsync(
                () -> {
                    DashboardStats stats = AppController.getInstance().getDashboardService().getDashboardStatistics();
                    List<Appointment> todayAppts = AppController.getInstance().getAppointmentService()
                            .listAppointments(LocalDate.now(), null, null, null);
                    List<Admission> activeAdmissions = AppController.getInstance().getAdmissionService()
                            .listAdmissions("ADMITTED", null);
                    return new Object[]{stats, todayAppts, activeAdmissions};
                },
                result -> {
                    Object[] data = (Object[]) result;
                    DashboardStats stats = (DashboardStats) data[0];
                    @SuppressWarnings("unchecked")
                    List<Appointment> appts = (List<Appointment>) data[1];
                    @SuppressWarnings("unchecked")
                    List<Admission> adms = (List<Admission>) data[2];

                    cardPatients.setValue(String.valueOf(stats.getTotalPatients()));
                    cardAppointments.setValue(String.valueOf(stats.getTodayAppointments()));
                    cardAdmissions.setValue(String.valueOf(stats.getCurrentAdmissions()));
                    cardLab.setValue(String.valueOf(stats.getPendingLabTests()));
                    cardLowStock.setValue(String.valueOf(stats.getLowStockMedicines()));
                    cardExpiring.setValue(String.valueOf(stats.getExpiringMedicines()));
                    cardUnpaid.setValue(String.valueOf(stats.getUnpaidInvoicesCount()));
                    cardRevenue.setValue(CurrencyUtil.format(stats.getMonthlyRevenue()));

                    // Populate Today Appointments
                    apptModel.setRowCount(0);
                    for (Appointment a : appts) {
                        apptModel.addRow(new Object[]{
                                DateUtil.formatTime(a.getAppointmentTime()),
                                a.getPatientName(),
                                a.getDoctorName(),
                                a.getStatus()
                        });
                    }

                    // Populate Active Admissions
                    admissionModel.setRowCount(0);
                    for (Admission adm : adms) {
                        admissionModel.addRow(new Object[]{
                                adm.getBedNumber() != null ? adm.getBedNumber() : "-",
                                adm.getPatientName(),
                                adm.getWardName(),
                                DateUtil.formatDateTime(adm.getAdmissionDate())
                        });
                    }
                },
                error -> NotificationToast.showError(this, "Failed to load dashboard data: " + error.getMessage())
        );
    }
}
