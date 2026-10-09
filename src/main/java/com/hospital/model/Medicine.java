package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Medicine {
    private Integer medicineId;
    private String code;
    private String name;
    private String genericName;
    private String category; // Antibiotic, Analgesic, Antipyretic, Antihypertensive, etc.
    private String manufacturer;
    private String unit = "Tablet"; // Tablet, Capsule, Syrup, Injection, Ointment
    private BigDecimal purchasePrice = BigDecimal.ZERO;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private int reorderLevel = 20;
    private int totalStock = 0;
    private boolean active = true;
    private LocalDateTime createdAt;

    public Medicine() {
    }

    public Medicine(Integer medicineId, String code, String name, String genericName,
            String category, String manufacturer, String unit,
            BigDecimal purchasePrice, BigDecimal sellingPrice, int reorderLevel) {
        this.medicineId = medicineId;
        this.code = code;
        this.name = name;
        this.genericName = genericName;
        this.category = category;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.reorderLevel = reorderLevel;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMedicineCode() {
        return code;
    }

    public void setMedicineCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMedicineName() {
        return name;
    }

    public void setMedicineName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    private String strength = "Standard";

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitType() {
        return unit;
    }

    public void setUnitType(String unitType) {
        this.unit = unitType;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public boolean isLowStock() {
        return totalStock <= reorderLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + unit + ") - Stock: " + totalStock;
    }
}
