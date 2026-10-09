package com.hospital.service;

import com.hospital.model.InventoryTransaction;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineBatch;

import java.util.List;
import java.util.Optional;

public interface PharmacyService {
    Medicine addMedicine(Medicine medicine);
    boolean updateMedicine(Medicine medicine);
    boolean setMedicineActive(int medicineId, boolean active);
    Optional<Medicine> getMedicineById(int medicineId);
    List<Medicine> listMedicines(String search, String category, boolean lowStockOnly);

    // Batches
    MedicineBatch addBatch(MedicineBatch batch);
    List<MedicineBatch> getBatchesForMedicine(int medicineId);
    List<MedicineBatch> getExpiringBatches(int daysThreshold);

    // Stock dispensing and receiving
    void dispenseStock(int batchId, int quantity, String referenceNumber, String notes);
    void receiveStock(int batchId, int quantity, String referenceNumber, String notes);

    List<InventoryTransaction> getRecentTransactions(int limit);
    int getLowStockCount();
    int getNearExpiryCount(int daysThreshold);
}
