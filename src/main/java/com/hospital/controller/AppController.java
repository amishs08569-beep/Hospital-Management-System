package com.hospital.controller;

import com.hospital.service.*;
import com.hospital.service.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Central controller coordinating services and asynchronous UI task execution.
 */
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);
    private static final AppController INSTANCE = new AppController();

    private final AuthService authService;
    private final PatientService patientService;
    private final DoctorStaffService doctorStaffService;
    private final AppointmentService appointmentService;
    private final AdmissionService admissionService;
    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final PharmacyService pharmacyService;
    private final LabService labService;
    private final BillingService billingService;
    private final DashboardService dashboardService;
    private final ReportService reportService;
    private final AuditService auditService;

    private AppController() {
        this.auditService = new AuditServiceImpl();
        this.authService = new AuthServiceImpl();
        this.patientService = new PatientServiceImpl();
        this.doctorStaffService = new DoctorStaffServiceImpl();
        this.appointmentService = new AppointmentServiceImpl();
        this.admissionService = new AdmissionServiceImpl();
        this.medicalRecordService = new MedicalRecordServiceImpl();
        this.prescriptionService = new PrescriptionServiceImpl();
        this.pharmacyService = new PharmacyServiceImpl();
        this.labService = new LabServiceImpl();
        this.billingService = new BillingServiceImpl();
        this.dashboardService = new DashboardServiceImpl();
        this.reportService = new ReportServiceImpl();
    }

    public static AppController getInstance() {
        return INSTANCE;
    }

    public AuthService getAuthService() { return authService; }
    public PatientService getPatientService() { return patientService; }
    public DoctorStaffService getDoctorStaffService() { return doctorStaffService; }
    public AppointmentService getAppointmentService() { return appointmentService; }
    public AdmissionService getAdmissionService() { return admissionService; }
    public MedicalRecordService getMedicalRecordService() { return medicalRecordService; }
    public PrescriptionService getPrescriptionService() { return prescriptionService; }
    public PharmacyService getPharmacyService() { return pharmacyService; }
    public LabService getLabService() { return labService; }
    public BillingService getBillingService() { return billingService; }
    public DashboardService getDashboardService() { return dashboardService; }
    public ReportService getReportService() { return reportService; }
    public AuditService getAuditService() { return auditService; }

    /**
     * Executes a background task using SwingWorker so the UI remains completely responsive.
     *
     * @param task Callable task to execute off the EDT
     * @param onSuccess Callback invoked on the EDT with the task result
     * @param onError Callback invoked on the EDT if an exception occurs
     */
    public static <T> void executeAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    logger.error("Async operation failed: {}", cause.getMessage());
                    if (onError != null) {
                        onError.accept(cause instanceof Exception ? (Exception) cause : new Exception(cause));
                    }
                }
            }
        };
        worker.execute();
    }
}
