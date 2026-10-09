package com.hospital.service.impl;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DoctorDao;
import com.hospital.dao.LabDao;
import com.hospital.dao.PatientDao;
import com.hospital.dao.impl.DoctorDaoImpl;
import com.hospital.dao.impl.LabDaoImpl;
import com.hospital.dao.impl.PatientDaoImpl;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.model.LabTest;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.service.AuditService;
import com.hospital.service.LabService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LabServiceImpl implements LabService {

    private static final Logger logger = LoggerFactory.getLogger(LabServiceImpl.class);

    private final LabDao labDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AuditService auditService;

    public LabServiceImpl() {
        this.labDao = new LabDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public LabServiceImpl(LabDao labDao, PatientDao patientDao, DoctorDao doctorDao, AuditService auditService) {
        this.labDao = labDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.auditService = auditService;
    }

    @Override
    public List<LabTest> listTests(boolean activeOnly) {
        return labDao.findAllTests(activeOnly);
    }

    @Override
    public LabTest addTest(LabTest test) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_LAB_CATALOG);

        ValidationUtil.requireNonBlank(test.getTestName(), "Test Name");
        ValidationUtil.requireNonBlank(test.getTestCode(), "Test Code");
        ValidationUtil.requireNonBlank(test.getCategory(), "Category");
        ValidationUtil.requirePositive(test.getPrice(), "Price");

        int id = labDao.insertTest(test);
        test.setTestId(id);
        auditService.log("CREATE", "LAB_TEST", test.getTestCode(), "Added lab test " + test.getTestName());
        logger.info("Added lab test {} [{}]", test.getTestName(), test.getTestCode());
        return test;
    }

    @Override
    public boolean updateTest(LabTest test) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_LAB_CATALOG);

        ValidationUtil.requireNonBlank(test.getTestName(), "Test Name");
        ValidationUtil.requirePositive(test.getPrice(), "Price");

        labDao.findTestById(test.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        boolean updated = labDao.updateTest(test);
        if (updated) {
            auditService.log("UPDATE", "LAB_TEST", test.getTestCode(), "Updated lab test " + test.getTestName());
        }
        return updated;
    }

    @Override
    public Optional<LabTest> getTestById(int testId) {
        return labDao.findTestById(testId);
    }

    @Override
    public LabOrder createOrder(LabOrder order, List<Integer> testIds) {
        SecurityContext.requirePermission(RolePermission.Permission.REQUEST_LAB_TEST);

        if (order.getPatientId() == null) throw new ValidationException("Patient is required.");
        if (order.getDoctorId() == null) throw new ValidationException("Ordering doctor is required.");
        if (testIds == null || testIds.isEmpty()) throw new ValidationException("At least one lab test must be selected.");

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        patientDao.findById(order.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        doctorDao.findById(order.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int id = labDao.insertOrder(order, testIds, conn);
                order.setOrderId(id);
                conn.commit();

                auditService.log("CREATE", "LAB_ORDER", order.getOrderNumber(),
                        "Created lab order with " + testIds.size() + " test(s) for Patient ID " + order.getPatientId());
                logger.info("Created lab order {}", order.getOrderNumber());
                return order;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating lab order: {}", e.getMessage());
            throw new DatabaseException("Failed to create lab order: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        SecurityContext.requirePermission(RolePermission.Permission.ENTER_LAB_RESULT);

        LabOrder order = labDao.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found"));

        boolean updated = labDao.updateOrderStatus(orderId, status);
        if (updated) {
            auditService.log("STATUS_CHANGE", "LAB_ORDER", order.getOrderNumber(),
                    "Changed status to " + status);
        }
        return updated;
    }

    @Override
    public Optional<LabOrder> getOrderById(int orderId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_LAB_RESULTS);
        return labDao.findOrderById(orderId);
    }

    @Override
    public List<LabOrder> listOrders(String status, Integer patientId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_LAB_RESULTS);
        return labDao.findAllOrders(status, patientId);
    }

    @Override
    public int getPendingOrdersCount() {
        return labDao.countPendingOrders();
    }

    @Override
    public boolean enterResult(LabResult result) {
        SecurityContext.requirePermission(RolePermission.Permission.ENTER_LAB_RESULT);

        ValidationUtil.requireNonBlank(result.getResultValue(), "Result Value");

        result.setPerformedByUserId(SecurityContext.getCurrentUserId());
        if (result.getResultDate() == null) {
            result.setResultDate(LocalDateTime.now());
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean success;
                if (result.getResultId() != null && result.getResultId() > 0) {
                    success = labDao.updateResult(result, conn);
                } else {
                    int id = labDao.insertResult(result, conn);
                    result.setResultId(id);
                    success = id > 0;
                }

                // If all results are entered, mark order as COMPLETED
                List<LabResult> allResults = labDao.findResultsByOrderId(result.getOrderId());
                boolean anyPending = false;
                for (LabResult r : allResults) {
                    if (r.getResultValue() == null || "Pending".equalsIgnoreCase(r.getResultValue().trim())) {
                        anyPending = true;
                        break;
                    }
                }
                if (!anyPending) {
                    labDao.updateOrderStatus(result.getOrderId(), "COMPLETED");
                } else {
                    labDao.updateOrderStatus(result.getOrderId(), "IN_PROGRESS");
                }

                conn.commit();
                auditService.log("ENTER_RESULT", "LAB_RESULT", String.valueOf(result.getResultId()),
                        "Entered result for test ID " + result.getTestId() + ": " + result.getResultValue());
                return success;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error entering lab result: {}", e.getMessage());
            throw new DatabaseException("Failed to record lab result: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LabResult> getResultsForOrder(int orderId) {
        SecurityContext.requirePermission(RolePermission.Permission.VIEW_LAB_RESULTS);
        return labDao.findResultsByOrderId(orderId);
    }
}
