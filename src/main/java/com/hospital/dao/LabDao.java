package com.hospital.dao;

import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.hospital.model.LabTest;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface LabDao {
    // Tests Catalog
    List<LabTest> findAllTests(boolean activeOnly);
    Optional<LabTest> findTestById(int testId);
    int insertTest(LabTest test);
    boolean updateTest(LabTest test);

    // Orders
    List<LabOrder> findAllOrders(String status, Integer patientId);
    Optional<LabOrder> findOrderById(int orderId);
    int insertOrder(LabOrder order, List<Integer> testIds, Connection conn);
    boolean updateOrderStatus(int orderId, String status);

    // Results
    List<LabResult> findResultsByOrderId(int orderId);
    int insertResult(LabResult result, Connection conn);
    boolean updateResult(LabResult result, Connection conn);

    int countPendingOrders();
    String generateNextOrderNumber();
}
