package com.hospital.service;

import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.model.LabTest;

import java.util.List;
import java.util.Optional;

public interface LabService {
    // Tests
    List<LabTest> listTests(boolean activeOnly);
    LabTest addTest(LabTest test);
    boolean updateTest(LabTest test);
    Optional<LabTest> getTestById(int testId);

    // Orders
    LabOrder createOrder(LabOrder order, List<Integer> testIds);
    boolean updateOrderStatus(int orderId, String status);
    Optional<LabOrder> getOrderById(int orderId);
    List<LabOrder> listOrders(String status, Integer patientId);
    int getPendingOrdersCount();

    // Results
    boolean enterResult(LabResult result);
    List<LabResult> getResultsForOrder(int orderId);
}
