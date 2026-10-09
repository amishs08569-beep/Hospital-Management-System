package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicineBatch {

    private Integer batchId;
    private Integer medicineId;
    private String medicineName;
    private String medicineCode;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private int currentQuantity;
    private BigDecimal purchasePrice = BigDecimal.ZERO;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private LocalDateTime createdAt;

    // Default constructor
    public MedicineBatch() {
    }

    // Constructor with quantity (original project compatibility)
    public MedicineBatch(Integer batchId, Integer medicineId, String batchNumber,
            LocalDate expiryDate, int quantity,
            BigDecimal purchasePrice, BigDecimal sellingPrice) {
        this.batchId = batchId;
        this.medicineId = medicineId;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.currentQuantity = quantity;
        this.purchasePrice = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        this.sellingPrice = sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
    }

    // Constructor with currentQuantity and manufacturingDate
    public MedicineBatch(Integer batchId, Integer medicineId, String batchNumber,
            LocalDate manufacturingDate, LocalDate expiryDate,
            int currentQuantity, BigDecimal purchasePrice, BigDecimal sellingPrice) {
        this.batchId = batchId;
        this.medicineId = medicineId;
        this.batchNumber = batchNumber;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.currentQuantity = currentQuantity;
        this.purchasePrice = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        this.sellingPrice = sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
    }

    // Full constructor
    public MedicineBatch(Integer batchId, Integer medicineId, String medicineName,
            String medicineCode, String batchNumber, LocalDate manufacturingDate,
            LocalDate expiryDate, int currentQuantity, BigDecimal purchasePrice,
            BigDecimal sellingPrice, LocalDateTime createdAt) {
        this.batchId = batchId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.medicineCode = medicineCode;
        this.batchNumber = batchNumber;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.currentQuantity = currentQuantity;
        this.purchasePrice = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        this.sellingPrice = sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // Quantity / currentQuantity aliases
    public int getQuantity() {
        return currentQuantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.currentQuantity = quantity;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        if (currentQuantity < 0) {
            throw new IllegalArgumentException("Current quantity cannot be negative.");
        }
        this.currentQuantity = currentQuantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Expiry helper methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiry(int daysThreshold) {
        return expiryDate != null && !isExpired() && expiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    @Override
    public String toString() {
        return "Batch " + batchNumber + " (Exp: " + expiryDate + ", Qty: " + currentQuantity + ")";
    }
}
