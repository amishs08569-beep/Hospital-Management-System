package com.hospital.model;

public class PrescriptionItem {
    private Integer itemId;
    private Integer prescriptionId;
    private Integer medicineId;
    private String medicineName;
    private String dosage; // e.g. 500mg
    private String frequency; // e.g. 1-0-1 after meals
    private String duration; // e.g. 5 days
    private int quantity = 1;
    private String instructions;

    public PrescriptionItem() {}

    public PrescriptionItem(Integer itemId, Integer prescriptionId, Integer medicineId,
                            String medicineName, String dosage, String frequency,
                            String duration, int quantity, String instructions) {
        this.itemId = itemId;
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
        this.instructions = instructions;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Integer prescriptionId) { this.prescriptionId = prescriptionId; }

    public Integer getMedicineId() { return medicineId; }
    public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    @Override
    public String toString() {
        return medicineName + " " + dosage + " (" + frequency + ", " + duration + ")";
    }
}
