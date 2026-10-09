package com.hospital.model;

import java.math.BigDecimal;

public class Ward {
    private Integer wardId;
    private String wardName;
    private String wardType; // General, ICU, Pediatric, Maternity, Emergency, Private
    private int totalBeds;
    private int availableBeds;
    private BigDecimal dailyRate = new BigDecimal("100.00");
    private String description;

    public Ward() {}

    public Ward(Integer wardId, String wardName, String wardType, int totalBeds, BigDecimal dailyRate, String description) {
        this.wardId = wardId;
        this.wardName = wardName;
        this.wardType = wardType;
        this.totalBeds = totalBeds;
        this.dailyRate = dailyRate;
        this.description = description;
    }

    public Integer getWardId() { return wardId; }
    public void setWardId(Integer wardId) { this.wardId = wardId; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }

    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return wardName + " (" + wardType + " - $" + dailyRate + "/day)";
    }
}
