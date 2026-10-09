package com.hospital.model;

import java.math.BigDecimal;

public class LabTest {
    private Integer testId;
    private String testCode;
    private String testName;
    private String category; // Hematology, Biochemistry, Microbiology, Radiology, Pathology
    private BigDecimal price = BigDecimal.ZERO;
    private String normalRange;
    private String unit;
    private String description;
    private boolean active = true;

    public LabTest() {}

    public LabTest(Integer testId, String testCode, String testName, String category,
                   BigDecimal price, String normalRange, String unit, String description) {
        this.testId = testId;
        this.testCode = testCode;
        this.testName = testName;
        this.category = category;
        this.price = price;
        this.normalRange = normalRange;
        this.unit = unit;
        this.description = description;
    }

    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getUnits() { return unit; }
    public void setUnits(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return testName + " [" + testCode + "] - $" + price;
    }
}
