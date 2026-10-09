package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private Integer transactionId;
    private Integer batchId;
    private Integer medicineId;
    private String medicineName;
    private String batchNumber;
    private String transactionType; // PURCHASE, DISPENSED, ADJUSTMENT, RETURN
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String referenceNumber;
    private String notes;
    private Integer performedByUserId;
    private String performedByUsername;
    private LocalDateTime createdAt;

    public InventoryTransaction() {}

    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }

    public Integer getMedicineId() { return medicineId; }
    public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(Integer performedByUserId) { this.performedByUserId = performedByUserId; }

    public String getPerformedByUsername() { return performedByUsername; }
    public void setPerformedByUsername(String performedByUsername) { this.performedByUsername = performedByUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return transactionType + ": " + quantity + " x " + medicineName;
    }
}
