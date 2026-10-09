package com.hospital.model;

public class Bed {
    private Integer bedId;
    private Integer wardId;
    private String wardName;
    private String bedNumber;
    private boolean occupied = false;
    private boolean active = true;

    public Bed() {}

    public Bed(Integer bedId, Integer wardId, String wardName, String bedNumber, boolean occupied) {
        this.bedId = bedId;
        this.wardId = wardId;
        this.wardName = wardName;
        this.bedNumber = bedNumber;
        this.occupied = occupied;
    }

    public Integer getBedId() { return bedId; }
    public void setBedId(Integer bedId) { this.bedId = bedId; }

    public Integer getWardId() { return wardId; }
    public void setWardId(Integer wardId) { this.wardId = wardId; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() {
        return occupied ? "OCCUPIED" : (active ? "AVAILABLE" : "MAINTENANCE");
    }

    public String getBedType() {
        return wardName != null && wardName.toUpperCase().contains("ICU") ? "ICU" : "Standard Care";
    }

    public java.math.BigDecimal getDailyRate() {
        return new java.math.BigDecimal("120.00");
    }

    @Override
    public String toString() {
        return (wardName != null ? wardName + " - " : "") + "Bed " + bedNumber + (occupied ? " [OCCUPIED]" : " [AVAILABLE]");
    }
}
