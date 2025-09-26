package com.hospital.smartpms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "counselors")
public class Counselor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "license_number")
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", nullable = false)
    private Specialization specialization;

    @Enumerated(EnumType.STRING)
    @Column(name = "counselor_type", nullable = false)
    private CounselorType counselorType;

    @Column(columnDefinition = "TEXT")
    private String qualifications;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @Column(name = "session_duration")
    private Integer sessionDuration = 60; // minutes

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "office_location")
    private String officeLocation;

    @Column(name = "languages_spoken")
    private String languagesSpoken;

    @Column(name = "online_consultation")
    private Boolean onlineConsultation = true;

    @Column(name = "in_person_consultation")
    private Boolean inPersonConsultation = true;

    // Relationships
    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CounselingSession> counselingSessions = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CounselorAvailability> availability = new ArrayList<>();

    // Enums
    public enum Specialization {
        GENERAL_COUNSELING("General Counseling"),
        MENTAL_HEALTH("Mental Health"),
        DEPRESSION_ANXIETY("Depression & Anxiety"),
        STRESS_MANAGEMENT("Stress Management"),
        TRAUMA_THERAPY("Trauma Therapy"),
        FAMILY_COUNSELING("Family Counseling"),
        COUPLES_THERAPY("Couples Therapy"),
        ADDICTION_COUNSELING("Addiction Counseling"),
        CAREER_COUNSELING("Career Counseling"),
        ACADEMIC_COUNSELING("Academic Counseling"),
        LIFE_COACHING("Life Coaching"),
        GRIEF_COUNSELING("Grief Counseling"),
        BEHAVIORAL_THERAPY("Behavioral Therapy"),
        COGNITIVE_THERAPY("Cognitive Therapy"),
        CHILD_PSYCHOLOGY("Child Psychology"),
        ADOLESCENT_COUNSELING("Adolescent Counseling");

        private final String displayName;

        Specialization(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CounselorType {
        PSYCHOLOGIST("Psychologist"),
        PSYCHIATRIST("Psychiatrist"),
        LICENSED_COUNSELOR("Licensed Professional Counselor"),
        SOCIAL_WORKER("Clinical Social Worker"),
        THERAPIST("Therapist"),
        CAREER_COUNSELOR("Career Counselor"),
        LIFE_COACH("Life Coach"),
        ADDICTION_SPECIALIST("Addiction Specialist"),
        FAMILY_THERAPIST("Family Therapist"),
        MARRIAGE_COUNSELOR("Marriage Counselor");

        private final String displayName;

        CounselorType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AvailabilityStatus {
        AVAILABLE("Available"),
        BUSY("Busy"),
        ON_LEAVE("On Leave"),
        RETIRED("Retired"),
        TEMPORARILY_UNAVAILABLE("Temporarily Unavailable");

        private final String displayName;

        AvailabilityStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Counselor() {
        this.createdAt = LocalDateTime.now();
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.sessionDuration = 60;
        this.onlineConsultation = true;
        this.inPersonConsultation = true;
    }

    public Counselor(String firstName, String lastName, String email, String password) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAvailable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE;
    }

    public boolean acceptsOnlineConsultations() {
        return onlineConsultation != null && onlineConsultation;
    }

    public boolean acceptsInPersonConsultations() {
        return inPersonConsultation != null && inPersonConsultation;
    }

    public String getSpecializationDisplay() {
        return specialization != null ? specialization.getDisplayName() : "";
    }

    public String getCounselorTypeDisplay() {
        return counselorType != null ? counselorType.getDisplayName() : "";
    }

    public String getAvailabilityStatusDisplay() {
        return availabilityStatus != null ? availabilityStatus.getDisplayName() : "";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

    public CounselorType getCounselorType() {
        return counselorType;
    }

    public void setCounselorType(CounselorType counselorType) {
        this.counselorType = counselorType;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Integer getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(Integer sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public Boolean getOnlineConsultation() {
        return onlineConsultation;
    }

    public void setOnlineConsultation(Boolean onlineConsultation) {
        this.onlineConsultation = onlineConsultation;
    }

    public Boolean getInPersonConsultation() {
        return inPersonConsultation;
    }

    public void setInPersonConsultation(Boolean inPersonConsultation) {
        this.inPersonConsultation = inPersonConsultation;
    }

    public List<CounselingSession> getCounselingSessions() {
        return counselingSessions;
    }

    public void setCounselingSessions(List<CounselingSession> counselingSessions) {
        this.counselingSessions = counselingSessions;
    }

    public List<CounselorAvailability> getAvailability() {
        return availability;
    }

    public void setAvailability(List<CounselorAvailability> availability) {
        this.availability = availability;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (availabilityStatus == null) {
            availabilityStatus = AvailabilityStatus.AVAILABLE;
        }
        if (sessionDuration == null) {
            sessionDuration = 60;
        }
        if (onlineConsultation == null) {
            onlineConsultation = true;
        }
        if (inPersonConsultation == null) {
            inPersonConsultation = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}