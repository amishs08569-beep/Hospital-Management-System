package com.hospital.service.impl;

import com.hospital.dao.*;
import com.hospital.dao.impl.*;
import com.hospital.model.DashboardStats;
import com.hospital.service.DashboardService;

public class DashboardServiceImpl implements DashboardService {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final StaffDao staffDao;
    private final AppointmentDao appointmentDao;
    private final AdmissionDao admissionDao;
    private final LabDao labDao;
    private final MedicineDao medicineDao;
    private final BillingDao billingDao;

    public DashboardServiceImpl() {
        this.patientDao = new PatientDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.staffDao = new StaffDaoImpl();
        this.appointmentDao = new AppointmentDaoImpl();
        this.admissionDao = new AdmissionDaoImpl();
        this.labDao = new LabDaoImpl();
        this.medicineDao = new MedicineDaoImpl();
        this.billingDao = new BillingDaoImpl();
    }

    @Override
    public DashboardStats getDashboardStatistics() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalPatients(patientDao.countAll());
        stats.setTotalDoctors(doctorDao.countAll());
        stats.setTotalStaff(staffDao.countAll());
        stats.setTodayAppointments(appointmentDao.countTodayAppointments());
        stats.setCurrentAdmissions(admissionDao.countActiveAdmissions());
        stats.setPendingLabTests(labDao.countPendingOrders());
        stats.setLowStockMedicines(medicineDao.countLowStock());
        stats.setExpiringMedicines(medicineDao.countNearExpiry(60));
        stats.setUnpaidInvoicesCount(billingDao.countUnpaidInvoices());
        stats.setOutstandingBalance(billingDao.getTotalOutstandingBalance());
        stats.setTodayRevenue(billingDao.getTodayRevenue());
        stats.setMonthlyRevenue(billingDao.getMonthlyRevenue());
        return stats;
    }
}
