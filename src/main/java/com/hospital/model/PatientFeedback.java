package com.hospital.model;

import java.time.LocalDateTime;

public class PatientFeedback {
    private Integer feedbackId;
    private Integer appointmentId;
    private String appointmentNumber;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private int rating; // 1 to 5
    private String comments;
    private LocalDateTime createdAt;

    public PatientFeedback() {}

    public PatientFeedback(Integer feedbackId, Integer appointmentId, Integer patientId,
                           Integer doctorId, int rating, String comments, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Integer getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Integer feedbackId) { this.feedbackId = feedbackId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
