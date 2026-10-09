package com.hospital.service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<String[]> getAppointmentsReportData(LocalDate fromDate, LocalDate toDate);
    List<String[]> getPatientsReportData(LocalDate fromDate, LocalDate toDate);
    List<String[]> getAdmissionsReportData(String status);
    List<String[]> getFinancialReportData(LocalDate fromDate, LocalDate toDate);
    List<String[]> getInventoryReportData(boolean lowStockOnly);
    List<String[]> getLabOrdersReportData(String status);

    void exportReportToCsv(File file, String reportType, LocalDate fromDate, LocalDate toDate) throws Exception;
}
