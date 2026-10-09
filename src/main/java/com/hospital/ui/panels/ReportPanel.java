package com.hospital.ui.panels;

import com.hospital.config.AppConfig;
import com.hospital.controller.AppController;
import com.hospital.security.UserSession;
import com.hospital.ui.components.ModernButton;
import com.hospital.ui.components.NotificationToast;
import com.hospital.ui.components.PlaceholderTextField;
import com.hospital.ui.components.UIHelper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Management and executive reporting panel with data filtering, preview, and CSV/PDF exports.
 */
public class ReportPanel extends JPanel {

    private final UserSession session;
    private final JComboBox<String> cmbReportType;
    private final PlaceholderTextField txtFromDate;
    private final PlaceholderTextField txtToDate;

    private final JTable reportTable;
    private final DefaultTableModel tableModel;
    private final List<String[]> currentRows = new ArrayList<>();
    private String[] currentHeaders = new String[0];

    public ReportPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIHelper.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        cmbReportType = new JComboBox<>(new String[]{"APPOINTMENTS", "PATIENTS", "ADMISSIONS", "FINANCIAL", "INVENTORY", "LABORATORY"});
        cmbReportType.setPreferredSize(new Dimension(170, 36));

        txtFromDate = new PlaceholderTextField(LocalDate.now().minusDays(30).toString(), 10);
        txtFromDate.setPreferredSize(new Dimension(120, 36));

        txtToDate = new PlaceholderTextField(LocalDate.now().toString(), 10);
        txtToDate.setPreferredSize(new Dimension(120, 36));

        ModernButton btnGenerate = new ModernButton("📊 Generate Preview", ModernButton.ButtonType.PRIMARY);
        btnGenerate.addActionListener(e -> generateReport());

        filterPanel.add(new JLabel("Report Type:"));
        filterPanel.add(cmbReportType);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(txtFromDate);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(txtToDate);
        filterPanel.add(btnGenerate);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        ModernButton btnCsv = new ModernButton("📥 Export CSV", ModernButton.ButtonType.SUCCESS);
        btnCsv.addActionListener(e -> onExportCsv());

        ModernButton btnPdf = new ModernButton("📄 Export PDF", ModernButton.ButtonType.SECONDARY);
        btnPdf.addActionListener(e -> onExportPdf());

        actionPanel.add(btnCsv);
        actionPanel.add(btnPdf);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        UIHelper.styleTable(reportTable);

        add(UIHelper.createModernScrollPane(reportTable), BorderLayout.CENTER);

        generateReport();
    }

    public void generateReport() {
        String type = (String) cmbReportType.getSelectedItem();
        LocalDate from = parseDate(txtFromDate.getText().trim());
        LocalDate to = parseDate(txtToDate.getText().trim());

        AppController.executeAsync(
                () -> {
                    switch (type) {
                        case "APPOINTMENTS":
                            return new Object[]{
                                    new String[]{"Appt #", "Date", "Time", "Patient", "Doctor", "Department", "Status", "Reason"},
                                    AppController.getInstance().getReportService().getAppointmentsReportData(from, to)
                            };
                        case "PATIENTS":
                            return new Object[]{
                                    new String[]{"Patient Code", "Full Name", "Gender", "DOB", "Phone", "Email", "Blood Group", "Status"},
                                    AppController.getInstance().getReportService().getPatientsReportData(from, to)
                            };
                        case "ADMISSIONS":
                            return new Object[]{
                                    new String[]{"Admission #", "Patient", "Ward", "Bed", "Doctor", "Admitted", "Discharged", "Status", "Daily Rate"},
                                    AppController.getInstance().getReportService().getAdmissionsReportData(null)
                            };
                        case "FINANCIAL":
                            return new Object[]{
                                    new String[]{"Invoice #", "Date", "Patient", "Subtotal", "Discount", "Tax", "Total", "Paid", "Balance", "Status"},
                                    AppController.getInstance().getReportService().getFinancialReportData(from, to)
                            };
                        case "INVENTORY":
                            return new Object[]{
                                    new String[]{"Code", "Medicine Name", "Category", "Unit", "Total Stock", "Reorder Level", "Purchase Price", "Selling Price", "Alert"},
                                    AppController.getInstance().getReportService().getInventoryReportData(false)
                            };
                        case "LABORATORY":
                            return new Object[]{
                                    new String[]{"Order #", "Order Date", "Patient", "Ordering Doctor", "Priority", "Status", "Tests Count"},
                                    AppController.getInstance().getReportService().getLabOrdersReportData(null)
                            };
                        default:
                            return new Object[]{new String[0], new ArrayList<String[]>()};
                    }
                },
                result -> {
                    Object[] res = (Object[]) result;
                    currentHeaders = (String[]) res[0];
                    @SuppressWarnings("unchecked")
                    List<String[]> rows = (List<String[]>) res[1];
                    currentRows.clear();
                    currentRows.addAll(rows);

                    tableModel.setDataVector(rows.toArray(new Object[0][]), currentHeaders);
                    UIHelper.styleTable(reportTable);
                },
                error -> NotificationToast.showError(this, "Failed to generate report: " + error.getMessage())
        );
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private void onExportCsv() {
        if (currentRows.isEmpty()) {
            NotificationToast.showWarning(this, "No data available to export.");
            return;
        }

        String type = (String) cmbReportType.getSelectedItem();
        LocalDate from = parseDate(txtFromDate.getText().trim());
        LocalDate to = parseDate(txtToDate.getText().trim());

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Hospital_" + type + "_Report.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            AppController.executeAsync(
                    () -> {
                        AppController.getInstance().getReportService().exportReportToCsv(target, type, from, to);
                        return target;
                    },
                    file -> {
                        NotificationToast.showSuccess(this, "CSV Report exported successfully: " + file.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        } catch (Exception ignored) {}
                    },
                    error -> NotificationToast.showError(this, "CSV export failed: " + error.getMessage())
            );
        }
    }

    private void onExportPdf() {
        if (currentRows.isEmpty()) {
            NotificationToast.showWarning(this, "No data available to export.");
            return;
        }

        String type = (String) cmbReportType.getSelectedItem();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Hospital_" + type + "_Report.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            AppController.executeAsync(
                    () -> {
                        Document doc = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
                        PdfWriter.getInstance(doc, new FileOutputStream(target));
                        doc.open();

                        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30, 58, 138));
                        com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
                        com.lowagie.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

                        Paragraph p = new Paragraph(AppConfig.getHospitalName() + " - " + type + " REPORT\n", titleFont);
                        p.setAlignment(Element.ALIGN_CENTER);
                        doc.add(p);

                        Paragraph sub = new Paragraph("Generated On: " + LocalDate.now() + " | Records Count: " + currentRows.size() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
                        sub.setAlignment(Element.ALIGN_CENTER);
                        doc.add(sub);

                        PdfPTable table = new PdfPTable(currentHeaders.length);
                        table.setWidthPercentage(100);

                        for (String h : currentHeaders) {
                            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                            cell.setBackgroundColor(new Color(30, 58, 138));
                            cell.setPadding(5);
                            table.addCell(cell);
                        }

                        for (String[] row : currentRows) {
                            for (String val : row) {
                                PdfPCell cell = new PdfPCell(new Phrase(val != null ? val : "", bodyFont));
                                cell.setPadding(4);
                                table.addCell(cell);
                            }
                        }

                        doc.add(table);
                        doc.close();
                        return target;
                    },
                    file -> {
                        NotificationToast.showSuccess(this, "PDF Report exported successfully: " + file.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                        } catch (Exception ignored) {}
                    },
                    error -> NotificationToast.showError(this, "PDF export failed: " + error.getMessage())
            );
        }
    }
}
