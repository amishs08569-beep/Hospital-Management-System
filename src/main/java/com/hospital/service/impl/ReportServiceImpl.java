package com.hospital.service.impl;

import com.hospital.dao.*;
import com.hospital.dao.impl.*;
import com.hospital.model.*;
import com.hospital.service.ReportService;
import com.hospital.util.CsvExporter;
import com.hospital.util.CurrencyUtil;
import com.hospital.util.DateUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final AppointmentDao appointmentDao = new AppointmentDaoImpl();
    private final PatientDao patientDao = new PatientDaoImpl();
    private final AdmissionDao admissionDao = new AdmissionDaoImpl();
    private final BillingDao billingDao = new BillingDaoImpl();
    private final MedicineDao medicineDao = new MedicineDaoImpl();
    private final LabDao labDao = new LabDaoImpl();

    @Override
    public List<String[]> getAppointmentsReportData(LocalDate fromDate, LocalDate toDate) {
        List<String[]> rows = new ArrayList<>();
        List<Appointment> appts = appointmentDao.findAll(null, null, null, null);
        for (Appointment a : appts) {
            if (fromDate != null && a.getAppointmentDate().isBefore(fromDate)) continue;
            if (toDate != null && a.getAppointmentDate().isAfter(toDate)) continue;

            rows.add(new String[]{
                    a.getAppointmentNumber(),
                    DateUtil.formatDate(a.getAppointmentDate()),
                    DateUtil.formatTime(a.getAppointmentTime()),
                    a.getPatientName(),
                    a.getDoctorName(),
                    a.getDepartmentName(),
                    a.getStatus(),
                    a.getReason() != null ? a.getReason() : ""
            });
        }
        return rows;
    }

    @Override
    public List<String[]> getPatientsReportData(LocalDate fromDate, LocalDate toDate) {
        List<String[]> rows = new ArrayList<>();
        List<Patient> list = patientDao.findAll(null, false);
        for (Patient p : list) {
            rows.add(new String[]{
                    p.getPatientCode(),
                    p.getFullName(),
                    p.getGender(),
                    DateUtil.formatDate(p.getDateOfBirth()),
                    p.getContactNumber(),
                    p.getEmail() != null ? p.getEmail() : "",
                    p.getBloodGroup() != null ? p.getBloodGroup() : "",
                    p.isActive() ? "Active" : "Inactive"
            });
        }
        return rows;
    }

    @Override
    public List<String[]> getAdmissionsReportData(String status) {
        List<String[]> rows = new ArrayList<>();
        List<Admission> list = admissionDao.findAll(status, null);
        for (Admission a : list) {
            rows.add(new String[]{
                    a.getAdmissionNumber(),
                    a.getPatientName(),
                    a.getWardName(),
                    a.getBedNumber(),
                    a.getAttendingDoctorName(),
                    DateUtil.formatDateTime(a.getAdmissionDate()),
                    a.getDischargeDate() != null ? DateUtil.formatDateTime(a.getDischargeDate()) : "Ongoing",
                    a.getStatus(),
                    CurrencyUtil.format(a.getDailyRate())
            });
        }
        return rows;
    }

    @Override
    public List<String[]> getFinancialReportData(LocalDate fromDate, LocalDate toDate) {
        List<String[]> rows = new ArrayList<>();
        List<Invoice> list = billingDao.findAll(null, null);
        for (Invoice inv : list) {
            if (fromDate != null && inv.getIssueDate().isBefore(fromDate)) continue;
            if (toDate != null && inv.getIssueDate().isAfter(toDate)) continue;

            rows.add(new String[]{
                    inv.getInvoiceNumber(),
                    DateUtil.formatDate(inv.getIssueDate()),
                    inv.getPatientName(),
                    CurrencyUtil.format(inv.getSubtotal()),
                    CurrencyUtil.format(inv.getDiscountAmount()),
                    CurrencyUtil.format(inv.getTaxAmount()),
                    CurrencyUtil.format(inv.getTotalAmount()),
                    CurrencyUtil.format(inv.getPaidAmount()),
                    CurrencyUtil.format(inv.getBalanceAmount()),
                    inv.getPaymentStatus()
            });
        }
        return rows;
    }

    @Override
    public List<String[]> getInventoryReportData(boolean lowStockOnly) {
        List<String[]> rows = new ArrayList<>();
        List<Medicine> list = medicineDao.findAll(null, null, lowStockOnly);
        for (Medicine m : list) {
            rows.add(new String[]{
                    m.getCode(),
                    m.getName(),
                    m.getCategory(),
                    m.getUnit(),
                    String.valueOf(m.getTotalStock()),
                    String.valueOf(m.getReorderLevel()),
                    CurrencyUtil.format(m.getPurchasePrice()),
                    CurrencyUtil.format(m.getSellingPrice()),
                    m.isLowStock() ? "LOW STOCK" : "NORMAL"
            });
        }
        return rows;
    }

    @Override
    public List<String[]> getLabOrdersReportData(String status) {
        List<String[]> rows = new ArrayList<>();
        List<LabOrder> list = labDao.findAllOrders(status, null);
        for (LabOrder o : list) {
            rows.add(new String[]{
                    o.getOrderNumber(),
                    DateUtil.formatDateTime(o.getOrderDate()),
                    o.getPatientName(),
                    o.getDoctorName(),
                    o.getPriority(),
                    o.getStatus(),
                    String.valueOf(o.getResults().size())
            });
        }
        return rows;
    }

    @Override
    public void exportReportToCsv(File file, String reportType, LocalDate fromDate, LocalDate toDate) throws Exception {
        String[] headers;
        List<String[]> data;

        switch (reportType.toUpperCase()) {
            case "APPOINTMENTS":
                headers = new String[]{"Appt Number", "Date", "Time", "Patient", "Doctor", "Department", "Status", "Reason"};
                data = getAppointmentsReportData(fromDate, toDate);
                break;
            case "PATIENTS":
                headers = new String[]{"Patient Code", "Full Name", "Gender", "DOB", "Phone", "Email", "Blood Group", "Status"};
                data = getPatientsReportData(fromDate, toDate);
                break;
            case "ADMISSIONS":
                headers = new String[]{"Admission #", "Patient", "Ward", "Bed", "Doctor", "Admitted At", "Discharged At", "Status", "Daily Rate"};
                data = getAdmissionsReportData(null);
                break;
            case "FINANCIAL":
                headers = new String[]{"Invoice #", "Date", "Patient", "Subtotal", "Discount", "Tax", "Total", "Paid", "Balance", "Status"};
                data = getFinancialReportData(fromDate, toDate);
                break;
            case "INVENTORY":
                headers = new String[]{"Code", "Medicine Name", "Category", "Unit", "Total Stock", "Reorder Level", "Purchase Price", "Selling Price", "Alert"};
                data = getInventoryReportData(false);
                break;
            case "LABORATORY":
                headers = new String[]{"Order #", "Order Date", "Patient", "Ordering Doctor", "Priority", "Status", "Tests Count"};
                data = getLabOrdersReportData(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }

        CsvExporter.exportToFile(file, headers, data);
    }
}
