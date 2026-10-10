package com.hospital.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsData {
    private int totalPatients;
    private int totalDoctors;
    private int totalAppointments;
    private int upcomingAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private int pendingAppointments;
    private double averageDoctorRating;
    private int totalFeedbackCount;

    // Time-series and grouping data for real database charts
    private Map<String, Integer> appointmentsByMonth = new LinkedHashMap<>();
    private Map<String, Integer> appointmentsByStatus = new LinkedHashMap<>();
    private Map<String, Integer> doctorWorkload = new LinkedHashMap<>();
    private Map<String, Integer> departmentDistribution = new LinkedHashMap<>();
    private List<AuditLog> recentAuditLogs = new ArrayList<>();

    public AnalyticsData() {}

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

    public int getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(int upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }

    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

    public int getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public double getAverageDoctorRating() { return averageDoctorRating; }
    public void setAverageDoctorRating(double averageDoctorRating) { this.averageDoctorRating = averageDoctorRating; }

    public int getTotalFeedbackCount() { return totalFeedbackCount; }
    public void setTotalFeedbackCount(int totalFeedbackCount) { this.totalFeedbackCount = totalFeedbackCount; }

    public Map<String, Integer> getAppointmentsByMonth() { return appointmentsByMonth; }
    public void setAppointmentsByMonth(Map<String, Integer> appointmentsByMonth) { this.appointmentsByMonth = appointmentsByMonth; }

    public Map<String, Integer> getAppointmentsByStatus() { return appointmentsByStatus; }
    public void setAppointmentsByStatus(Map<String, Integer> appointmentsByStatus) { this.appointmentsByStatus = appointmentsByStatus; }

    public Map<String, Integer> getDoctorWorkload() { return doctorWorkload; }
    public void setDoctorWorkload(Map<String, Integer> doctorWorkload) { this.doctorWorkload = doctorWorkload; }

    public Map<String, Integer> getDepartmentDistribution() { return departmentDistribution; }
    public void setDepartmentDistribution(Map<String, Integer> departmentDistribution) { this.departmentDistribution = departmentDistribution; }

    public List<AuditLog> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLog> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
}
