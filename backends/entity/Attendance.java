package com.hospital.smartpms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_in_method")
    @Enumerated(EnumType.STRING)
    private CheckInMethod checkInMethod;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "location")
    private String location;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum AttendanceStatus {
        SCHEDULED, // Appointment scheduled, not yet checked in
        CHECKED_IN, // Patient has checked in
        PRESENT, // Patient is present at appointment
        NO_SHOW, // Patient did not show up
        LATE, // Patient checked in late
        CANCELLED // Appointment was cancelled
    }

    public enum CheckInMethod {
        QR_CODE, // Checked in using QR code
        MANUAL, // Manually checked in by staff
        ONLINE, // Online check-in via website/app
        PHONE, // Checked in via phone call
        KIOSK // Self-service kiosk check-in
    }

    // Constructors
    public Attendance() {
        this.createdAt = LocalDateTime.now();
        this.status = AttendanceStatus.SCHEDULED;
    }

    public Attendance(Appointment appointment) {
        this();
        this.appointment = appointment;
        this.generateQrCode();
    }

    // Methods
    public void checkIn(CheckInMethod method) {
        this.checkInTime = LocalDateTime.now();
        this.checkInMethod = method;
        this.status = AttendanceStatus.CHECKED_IN;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPresent() {
        this.status = AttendanceStatus.PRESENT;
        this.updatedAt = LocalDateTime.now();
    }

    public void markNoShow() {
        this.status = AttendanceStatus.NO_SHOW;
        this.updatedAt = LocalDateTime.now();
    }

    public void markLate() {
        this.status = AttendanceStatus.LATE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLate() {
        if (checkInTime == null || appointment == null) {
            return false;
        }

        LocalDateTime appointmentDateTime = appointment.getAppointmentDate();
        return checkInTime.isAfter(appointmentDateTime.plusMinutes(15)); // 15 minutes grace period
    }

    private void generateQrCode() {
        if (appointment != null) {
            this.qrCode = "ATTEND-" + appointment.getId() + "-" + System.currentTimeMillis();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public CheckInMethod getCheckInMethod() {
        return checkInMethod;
    }

    public void setCheckInMethod(CheckInMethod checkInMethod) {
        this.checkInMethod = checkInMethod;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AttendanceStatus.SCHEDULED;
        }
        if (qrCode == null && appointment != null) {
            generateQrCode();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}