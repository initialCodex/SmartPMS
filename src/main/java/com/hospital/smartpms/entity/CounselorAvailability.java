package com.hospital.smartpms.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "counselor_availability")
public class CounselorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "specific_date")
    private LocalDateTime specificDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_type")
    private AvailabilityType availabilityType;

    @Column(name = "max_sessions")
    private Integer maxSessions;

    @Column(name = "session_duration")
    private Integer sessionDuration; // in minutes

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum AvailabilityType {
        REGULAR("Regular Schedule"),
        ONE_TIME("One-time Availability"),
        EXCEPTION("Schedule Exception"),
        HOLIDAY("Holiday Schedule"),
        EMERGENCY("Emergency Availability");

        private final String displayName;

        AvailabilityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public CounselorAvailability() {
        this.createdAt = LocalDateTime.now();
        this.isAvailable = true;
        this.availabilityType = AvailabilityType.REGULAR;
    }

    public CounselorAvailability(Counselor counselor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this();
        this.counselor = counselor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionDuration = counselor.getSessionDuration();
    }

    // Helper methods
    public boolean isActiveForDate(LocalDateTime date) {
        if (!isAvailable) {
            return false;
        }

        if (availabilityType == AvailabilityType.ONE_TIME) {
            return specificDate != null &&
                    specificDate.toLocalDate().equals(date.toLocalDate());
        }

        if (availabilityType == AvailabilityType.REGULAR) {
            return dayOfWeek == date.getDayOfWeek();
        }

        return true;
    }

    public boolean isTimeSlotAvailable(LocalTime time) {
        if (!isAvailable) {
            return false;
        }

        return time.isAfter(startTime) && time.isBefore(endTime) ||
                time.equals(startTime);
    }

    public int getAvailableSlots() {
        if (maxSessions != null) {
            return maxSessions;
        }

        if (startTime != null && endTime != null && sessionDuration != null) {
            long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            return (int) (totalMinutes / sessionDuration);
        }

        return 0;
    }

    public String getAvailabilityTypeDisplay() {
        return availabilityType != null ? availabilityType.getDisplayName() : "";
    }

    public String getDayOfWeekDisplay() {
        return dayOfWeek != null ? dayOfWeek.toString() : "";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Counselor getCounselor() {
        return counselor;
    }

    public void setCounselor(Counselor counselor) {
        this.counselor = counselor;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDateTime getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDateTime specificDate) {
        this.specificDate = specificDate;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(AvailabilityType availabilityType) {
        this.availabilityType = availabilityType;
    }

    public Integer getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(Integer maxSessions) {
        this.maxSessions = maxSessions;
    }

    public Integer getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(Integer sessionDuration) {
        this.sessionDuration = sessionDuration;
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
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (availabilityType == null) {
            availabilityType = AvailabilityType.REGULAR;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}