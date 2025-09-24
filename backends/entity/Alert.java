package com.hospital.smartpms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "recipient_type")
    @Enumerated(EnumType.STRING)
    private RecipientType recipientType;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    // Enums
    public enum AlertType {
        APPOINTMENT_REMINDER, // Reminder for upcoming appointments
        LOW_ATTENDANCE, // Alert for low attendance patterns
        NO_SHOW_WARNING, // Warning for no-show patterns
        EMERGENCY_NOTIFICATION, // Emergency alerts
        WAITLIST_NOTIFICATION, // Waitlist status updates
        APPOINTMENT_CANCELLATION, // Appointment cancelled
        APPOINTMENT_CONFIRMATION, // Appointment confirmed
        TREATMENT_REMINDER, // Treatment follow-up reminders
        SYSTEM_MAINTENANCE, // System maintenance notifications
        OVERDUE_CHECKIN // Patient overdue for check-in
    }

    public enum AlertPriority {
        LOW, // Non-urgent notifications
        NORMAL, // Standard notifications
        HIGH, // Important notifications
        URGENT, // Urgent notifications
        EMERGENCY // Emergency alerts
    }

    public enum AlertStatus {
        PENDING, // Alert scheduled but not sent
        SENT, // Alert sent successfully
        DELIVERED, // Alert delivered (for supported channels)
        READ, // Alert read by recipient
        FAILED, // Alert failed to send
        CANCELLED, // Alert cancelled before sending
        EXPIRED // Alert expired without being sent
    }

    public enum RecipientType {
        PATIENT, // Alert for patient
        DOCTOR, // Alert for doctor
        ADMIN, // Alert for admin
        STAFF, // Alert for staff
        SYSTEM // System-wide alert
    }

    // Constructors
    public Alert() {
        this.createdAt = LocalDateTime.now();
        this.status = AlertStatus.PENDING;
        this.priority = AlertPriority.NORMAL;
        this.retryCount = 0;
        this.maxRetries = 3;
    }

    public Alert(AlertType type, String title, String message) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
    }

    // Helper methods
    public boolean canRetry() {
        return retryCount < maxRetries && status == AlertStatus.FAILED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.status = AlertStatus.SENT;
        this.sentTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = AlertStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.status = AlertStatus.READ;
        this.readTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = AlertStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = AlertStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return scheduledTime != null &&
                scheduledTime.isBefore(LocalDateTime.now()) &&
                status == AlertStatus.PENDING;
    }

    public boolean isExpired() {
        // Consider alert expired if it's more than 24 hours overdue
        return scheduledTime != null &&
                scheduledTime.plusHours(24).isBefore(LocalDateTime.now()) &&
                status == AlertStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertPriority getPriority() {
        return priority;
    }

    public void setPriority(AlertPriority priority) {
        this.priority = priority;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.recipientType = RecipientType.PATIENT;
            this.recipientId = patient.getId();
            this.recipientEmail = patient.getEmail();
        }
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.recipientType = RecipientType.DOCTOR;
            this.recipientId = doctor.getId();
            this.recipientEmail = doctor.getEmail();
        }
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AlertStatus.PENDING;
        }
        if (priority == null) {
            priority = AlertPriority.NORMAL;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}