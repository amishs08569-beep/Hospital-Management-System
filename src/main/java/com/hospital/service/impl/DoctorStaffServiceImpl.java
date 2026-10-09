package com.hospital.service.impl;

import com.hospital.dao.DepartmentDao;
import com.hospital.dao.DoctorDao;
import com.hospital.dao.DoctorScheduleDao;
import com.hospital.dao.StaffDao;
import com.hospital.dao.impl.DepartmentDaoImpl;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.DoctorScheduleDaoImpl;
import com.hospital.dao.impl.StaffDaoImpl;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.DoctorSchedule;
import com.hospital.model.Staff;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.DoctorStaffService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DoctorStaffServiceImpl implements DoctorStaffService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorStaffServiceImpl.class);

    private final DoctorDao doctorDao;
    private final StaffDao staffDao;
    private final DepartmentDao departmentDao;
    private final DoctorScheduleDao scheduleDao;
    private final AuditService auditService;

    public DoctorStaffServiceImpl() {
        this.doctorDao = new DoctorDaoImpl();
        this.staffDao = new StaffDaoImpl();
        this.departmentDao = new DepartmentDaoImpl();
        this.scheduleDao = new DoctorScheduleDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public DoctorStaffServiceImpl(DoctorDao doctorDao, StaffDao staffDao, DepartmentDao departmentDao,
                                  DoctorScheduleDao scheduleDao, AuditService auditService) {
        this.doctorDao = doctorDao;
        this.staffDao = staffDao;
        this.departmentDao = departmentDao;
        this.scheduleDao = scheduleDao;
        this.auditService = auditService;
    }

    // ------------------------------------------------------------------------
    // Doctors
    // ------------------------------------------------------------------------

    @Override
    public Doctor registerDoctor(Doctor doctor) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DOCTORS);

        ValidationUtil.requireNonBlank(doctor.getFullName(), "Doctor Full Name");
        ValidationUtil.requireNonBlank(doctor.getSpecialization(), "Specialization");
        ValidationUtil.requireNonBlank(doctor.getQualification(), "Qualification");
        ValidationUtil.requireNonBlank(doctor.getContactNumber(), "Contact Number");
        ValidationUtil.requireNonBlank(doctor.getEmail(), "Email");
        ValidationUtil.validateEmail(doctor.getEmail(), "Email");
        ValidationUtil.validatePhone(doctor.getContactNumber(), "Contact");
        ValidationUtil.requirePositive(doctor.getConsultationFee(), "Consultation Fee");

        int id = doctorDao.insert(doctor);
        doctor.setDoctorId(id);
        auditService.log("CREATE", "DOCTOR", doctor.getEmployeeId(), "Registered doctor " + doctor.getFullName());
        logger.info("Registered doctor {} [{}]", doctor.getFullName(), doctor.getEmployeeId());
        return doctor;
    }

    @Override
    public boolean updateDoctor(Doctor doctor) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DOCTORS);

        ValidationUtil.requireNonBlank(doctor.getFullName(), "Doctor Full Name");
        ValidationUtil.requireNonBlank(doctor.getSpecialization(), "Specialization");
        ValidationUtil.requireNonBlank(doctor.getQualification(), "Qualification");
        ValidationUtil.validateEmail(doctor.getEmail(), "Email");
        ValidationUtil.validatePhone(doctor.getContactNumber(), "Contact");

        doctorDao.findById(doctor.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        boolean updated = doctorDao.update(doctor);
        if (updated) {
            auditService.log("UPDATE", "DOCTOR", doctor.getEmployeeId(), "Updated doctor details for " + doctor.getFullName());
        }
        return updated;
    }

    @Override
    public boolean setDoctorStatus(int doctorId, String status) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DOCTORS);
        Doctor doc = doctorDao.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        boolean updated = doctorDao.setStatus(doctorId, status);
        if (updated) {
            auditService.log("STATUS_CHANGE", "DOCTOR", doc.getEmployeeId(), "Doctor status changed to " + status);
        }
        return updated;
    }

    @Override
    public Optional<Doctor> getDoctorById(int doctorId) {
        return doctorDao.findById(doctorId);
    }

    @Override
    public List<Doctor> listDoctors(String search, Integer departmentId, boolean activeOnly) {
        return doctorDao.findAll(search, departmentId, activeOnly);
    }

    @Override
    public int getTotalDoctorCount() {
        return doctorDao.countAll();
    }

    // ------------------------------------------------------------------------
    // Schedules
    // ------------------------------------------------------------------------

    @Override
    public List<DoctorSchedule> getDoctorSchedules(int doctorId) {
        return scheduleDao.findByDoctorId(doctorId);
    }

    @Override
    public DoctorSchedule addDoctorSchedule(DoctorSchedule schedule) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DOCTORS);
        ValidationUtil.requireNonBlank(schedule.getDayOfWeek(), "Day of Week");
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }
        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        int id = scheduleDao.insert(schedule);
        schedule.setScheduleId(id);
        auditService.log("CREATE", "SCHEDULE", String.valueOf(id), "Added schedule for doctor ID " + schedule.getDoctorId());
        return schedule;
    }

    @Override
    public boolean removeDoctorSchedule(int scheduleId) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DOCTORS);
        boolean deleted = scheduleDao.delete(scheduleId);
        if (deleted) {
            auditService.log("DELETE", "SCHEDULE", String.valueOf(scheduleId), "Removed doctor schedule");
        }
        return deleted;
    }

    // ------------------------------------------------------------------------
    // Staff
    // ------------------------------------------------------------------------

    @Override
    public Staff registerStaff(Staff staff) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_STAFF);

        ValidationUtil.requireNonBlank(staff.getFullName(), "Staff Full Name");
        ValidationUtil.requireNonBlank(staff.getRoleTitle(), "Role Title");
        ValidationUtil.requireNonBlank(staff.getContactNumber(), "Contact Number");
        ValidationUtil.requireNonBlank(staff.getEmail(), "Email");
        ValidationUtil.validateEmail(staff.getEmail(), "Email");
        ValidationUtil.validatePhone(staff.getContactNumber(), "Contact");

        int id = staffDao.insert(staff);
        staff.setStaffId(id);
        auditService.log("CREATE", "STAFF", staff.getEmployeeId(), "Registered staff " + staff.getFullName() + " (" + staff.getRoleTitle() + ")");
        return staff;
    }

    @Override
    public boolean updateStaff(Staff staff) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_STAFF);

        ValidationUtil.requireNonBlank(staff.getFullName(), "Staff Full Name");
        ValidationUtil.requireNonBlank(staff.getRoleTitle(), "Role Title");
        ValidationUtil.validateEmail(staff.getEmail(), "Email");
        ValidationUtil.validatePhone(staff.getContactNumber(), "Contact");

        staffDao.findById(staff.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        boolean updated = staffDao.update(staff);
        if (updated) {
            auditService.log("UPDATE", "STAFF", staff.getEmployeeId(), "Updated staff details for " + staff.getFullName());
        }
        return updated;
    }

    @Override
    public boolean setStaffStatus(int staffId, String status) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_STAFF);
        Staff s = staffDao.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        boolean updated = staffDao.setStatus(staffId, status);
        if (updated) {
            auditService.log("STATUS_CHANGE", "STAFF", s.getEmployeeId(), "Staff status changed to " + status);
        }
        return updated;
    }

    @Override
    public Optional<Staff> getStaffById(int staffId) {
        return staffDao.findById(staffId);
    }

    @Override
    public List<Staff> listStaff(String search, Integer departmentId) {
        return staffDao.findAll(search, departmentId);
    }

    @Override
    public int getTotalStaffCount() {
        return staffDao.countAll();
    }

    // ------------------------------------------------------------------------
    // Departments
    // ------------------------------------------------------------------------

    @Override
    public List<Department> listDepartments(boolean activeOnly) {
        return departmentDao.findAll(activeOnly);
    }

    @Override
    public Department createDepartment(Department department) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DEPARTMENTS);
        ValidationUtil.requireNonBlank(department.getName(), "Department Name");

        int id = departmentDao.insert(department);
        department.setDepartmentId(id);
        auditService.log("CREATE", "DEPARTMENT", String.valueOf(id), "Created department " + department.getName());
        return department;
    }

    @Override
    public boolean updateDepartment(Department department) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_DEPARTMENTS);
        ValidationUtil.requireNonBlank(department.getName(), "Department Name");

        boolean updated = departmentDao.update(department);
        if (updated) {
            auditService.log("UPDATE", "DEPARTMENT", String.valueOf(department.getDepartmentId()), "Updated department " + department.getName());
        }
        return updated;
    }
}
