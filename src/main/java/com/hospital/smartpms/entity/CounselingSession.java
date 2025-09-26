package com.hospital.smartpms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "counseling_sessions")
public class CounselingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Counselor counselor;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false)
    private SessionMode sessionMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "session_fee")
    private Double sessionFee;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "session_notes", columnDefinition = "TEXT")
    private String sessionNotes;

    @Column(name = "counselor_notes", columnDefinition = "TEXT")
    private String counselorNotes;

    @Column(name = "patient_feedback", columnDefinition = "TEXT")
    private String patientFeedback;

    @Column(name = "session_rating")
    private Integer sessionRating; // 1-5 stars

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "prescription_notes", columnDefinition = "TEXT")
    private String prescriptionNotes;

    @Column(name = "homework_assignments", columnDefinition = "TEXT")
    private String homeworkAssignments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "emergency_session")
    private Boolean emergencySession = false;

    @Column(name = "virtual_meeting_link")
    private String virtualMeetingLink;

    @Column(name = "session_reminder_sent")
    private Boolean sessionReminderSent = false;

    // Enums
    public enum SessionType {
        INITIAL_CONSULTATION("Initial Consultation"),
        FOLLOW_UP("Follow-up Session"),
        CRISIS_INTERVENTION("Crisis Intervention"),
        GROUP_THERAPY("Group Therapy"),
        FAMILY_COUNSELING("Family Counseling"),
        COUPLES_THERAPY("Couples Therapy"),
        CAREER_COUNSELING("Career Counseling"),
        ACADEMIC_COUNSELING("Academic Counseling"),
        BEHAVIORAL_THERAPY("Behavioral Therapy"),
        COGNITIVE_THERAPY("Cognitive Therapy"),
        TRAUMA_THERAPY("Trauma Therapy"),
        ADDICTION_COUNSELING("Addiction Counseling"),
        STRESS_MANAGEMENT("Stress Management"),
        GRIEF_COUNSELING("Grief Counseling");

        private final String displayName;

        SessionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionMode {
        IN_PERSON("In-Person"),
        VIDEO_CALL("Video Call"),
        PHONE_CALL("Phone Call"),
        CHAT("Chat Session"),
        EMAIL("Email Consultation");

        private final String displayName;

        SessionMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionStatus {
        SCHEDULED("Scheduled"),
        CONFIRMED("Confirmed"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show"),
        RESCHEDULED("Rescheduled"),
        PENDING_PAYMENT("Pending Payment"),
        PAID("Paid");

        private final String displayName;

        SessionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public CounselingSession() {
        this.createdAt = LocalDateTime.now();
        this.status = SessionStatus.SCHEDULED;
        this.emergencySession = false;
        this.followUpRequired = false;
        this.sessionReminderSent = false;
    }

    public CounselingSession(Patient patient, Counselor counselor, LocalDateTime sessionDate, SessionType sessionType) {
        this();
        this.patient = patient;
        this.counselor = counselor;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.sessionMode = SessionMode.IN_PERSON; // default
        this.durationMinutes = counselor.getSessionDuration();
        this.sessionFee = counselor.getConsultationFee();
    }

    // Helper methods
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == SessionStatus.CANCELLED;
    }

    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    public boolean isScheduled() {
        return status == SessionStatus.SCHEDULED || status == SessionStatus.CONFIRMED;
    }

    public boolean isUpcoming() {
        return isScheduled() && sessionDate.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return sessionDate.isBefore(LocalDateTime.now());
    }

    public boolean isToday() {
        LocalDateTime now = LocalDateTime.now();
        return sessionDate.toLocalDate().equals(now.toLocalDate());
    }

    public boolean isVirtual() {
        return sessionMode == SessionMode.VIDEO_CALL ||
                sessionMode == SessionMode.PHONE_CALL ||
                sessionMode == SessionMode.CHAT;
    }

    public void startSession() {
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void completeSession() {
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelSession(String reason) {
        this.status = SessionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsNoShow() {
        this.status = SessionStatus.NO_SHOW;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSessionTypeDisplay() {
        return sessionType != null ? sessionType.getDisplayName() : "";
    }

    public String getSessionModeDisplay() {
        return sessionMode != null ? sessionMode.getDisplayName() : "";
    }

    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Counselor getCounselor() {
        return counselor;
    }

    public void setCounselor(Counselor counselor) {
        this.counselor = counselor;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public SessionMode getSessionMode() {
        return sessionMode;
    }

    public void setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getSessionFee() {
        return sessionFee;
    }

    public void setSessionFee(Double sessionFee) {
        this.sessionFee = sessionFee;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    public String getCounselorNotes() {
        return counselorNotes;
    }

    public void setCounselorNotes(String counselorNotes) {
        this.counselorNotes = counselorNotes;
    }

    public String getPatientFeedback() {
        return patientFeedback;
    }

    public void setPatientFeedback(String patientFeedback) {
        this.patientFeedback = patientFeedback;
    }

    public Integer getSessionRating() {
        return sessionRating;
    }

    public void setSessionRating(Integer sessionRating) {
        this.sessionRating = sessionRating;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getPrescriptionNotes() {
        return prescriptionNotes;
    }

    public void setPrescriptionNotes(String prescriptionNotes) {
        this.prescriptionNotes = prescriptionNotes;
    }

    public String getHomeworkAssignments() {
        return homeworkAssignments;
    }

    public void setHomeworkAssignments(String homeworkAssignments) {
        this.homeworkAssignments = homeworkAssignments;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Boolean getEmergencySession() {
        return emergencySession;
    }

    public void setEmergencySession(Boolean emergencySession) {
        this.emergencySession = emergencySession;
    }

    public String getVirtualMeetingLink() {
        return virtualMeetingLink;
    }

    public void setVirtualMeetingLink(String virtualMeetingLink) {
        this.virtualMeetingLink = virtualMeetingLink;
    }

    public Boolean getSessionReminderSent() {
        return sessionReminderSent;
    }

    public void setSessionReminderSent(Boolean sessionReminderSent) {
        this.sessionReminderSent = sessionReminderSent;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }
        if (emergencySession == null) {
            emergencySession = false;
        }
        if (followUpRequired == null) {
            followUpRequired = false;
        }
        if (sessionReminderSent == null) {
            sessionReminderSent = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}