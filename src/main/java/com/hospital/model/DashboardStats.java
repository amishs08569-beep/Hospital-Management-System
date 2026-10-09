package com.hospital.model;

import java.math.BigDecimal;

public class DashboardStats {
    private int totalPatients;
    private int totalDoctors;
    private int totalStaff;
    private int todayAppointments;
    private int currentAdmissions;
    private int pendingLabTests;
    private int lowStockMedicines;
    private int expiringMedicines;
    private int unpaidInvoicesCount;
    private BigDecimal outstandingBalance = BigDecimal.ZERO;
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;

    public DashboardStats() {}

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }

    public int getTotalStaff() { return totalStaff; }
    public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }

    public int getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(int todayAppointments) { this.todayAppointments = todayAppointments; }

    public int getCurrentAdmissions() { return currentAdmissions; }
    public void setCurrentAdmissions(int currentAdmissions) { this.currentAdmissions = currentAdmissions; }

    public int getPendingLabTests() { return pendingLabTests; }
    public void setPendingLabTests(int pendingLabTests) { this.pendingLabTests = pendingLabTests; }

    public int getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(int lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }

    public int getExpiringMedicines() { return expiringMedicines; }
    public void setExpiringMedicines(int expiringMedicines) { this.expiringMedicines = expiringMedicines; }

    public int getUnpaidInvoicesCount() { return unpaidInvoicesCount; }
    public void setUnpaidInvoicesCount(int unpaidInvoicesCount) { this.unpaidInvoicesCount = unpaidInvoicesCount; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
}
