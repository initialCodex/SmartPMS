package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.Alert;
import com.hospital.smartpms.entity.Alert.AlertStatus;
import com.hospital.smartpms.entity.Alert.AlertType;
import com.hospital.smartpms.entity.Alert.AlertPriority;
import com.hospital.smartpms.entity.Alert.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Find alerts by status
    List<Alert> findByStatus(AlertStatus status);

    // Find alerts by type
    List<Alert> findByType(AlertType type);

    // Find alerts by priority
    List<Alert> findByPriority(AlertPriority priority);

    // Find alerts by recipient
    List<Alert> findByRecipientTypeAndRecipientId(RecipientType recipientType, Long recipientId);

    // Find alerts by patient
    List<Alert> findByPatientId(Long patientId);

    // Find alerts by doctor
    List<Alert> findByDoctorId(Long doctorId);

    // Find alerts by appointment
    List<Alert> findByAppointmentId(Long appointmentId);

    // Find pending alerts that need to be sent
    @Query("SELECT a FROM Alert a WHERE a.status = 'PENDING' AND " +
            "(a.scheduledTime IS NULL OR a.scheduledTime <= :currentTime)")
    List<Alert> findPendingAlertsToSend(@Param("currentTime") LocalDateTime currentTime);

    // Find failed alerts that can be retried
    @Query("SELECT a FROM Alert a WHERE a.status = 'FAILED' AND a.retryCount < a.maxRetries")
    List<Alert> findFailedAlertsForRetry();

    // Find overdue alerts
    @Query("SELECT a FROM Alert a WHERE a.status = 'PENDING' AND " +
            "a.scheduledTime IS NOT NULL AND a.scheduledTime < :currentTime")
    List<Alert> findOverdueAlerts(@Param("currentTime") LocalDateTime currentTime);

    // Find expired alerts (more than 24 hours overdue)
    @Query("SELECT a FROM Alert a WHERE a.status = 'PENDING' AND " +
            "a.scheduledTime IS NOT NULL AND a.scheduledTime < :expiredTime")
    List<Alert> findExpiredAlerts(@Param("expiredTime") LocalDateTime expiredTime);

    // Find alerts by date range
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find alerts for specific recipient within date range
    @Query("SELECT a FROM Alert a WHERE a.recipientType = :recipientType AND " +
            "a.recipientId = :recipientId AND a.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY a.createdAt DESC")
    List<Alert> findByRecipientAndDateRange(@Param("recipientType") RecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find unread alerts for recipient
    @Query("SELECT a FROM Alert a WHERE a.recipientType = :recipientType AND " +
            "a.recipientId = :recipientId AND a.status NOT IN ('READ', 'CANCELLED', 'EXPIRED') " +
            "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Alert> findUnreadAlertsByRecipient(@Param("recipientType") RecipientType recipientType,
            @Param("recipientId") Long recipientId);

    // Count unread alerts for recipient
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.recipientType = :recipientType AND " +
            "a.recipientId = :recipientId AND a.status NOT IN ('READ', 'CANCELLED', 'EXPIRED')")
    Long countUnreadAlertsByRecipient(@Param("recipientType") RecipientType recipientType,
            @Param("recipientId") Long recipientId);

    // Find alerts by priority and status
    List<Alert> findByPriorityAndStatus(AlertPriority priority, AlertStatus status);

    // Find recent alerts (last 24 hours)
    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);

    // Find alerts scheduled for future
    @Query("SELECT a FROM Alert a WHERE a.scheduledTime > :currentTime AND a.status = 'PENDING' " +
            "ORDER BY a.scheduledTime ASC")
    List<Alert> findScheduledAlerts(@Param("currentTime") LocalDateTime currentTime);

    // Find alerts by type and status
    List<Alert> findByTypeAndStatus(AlertType type, AlertStatus status);

    // Count alerts by status
    @Query("SELECT a.status, COUNT(a) FROM Alert a GROUP BY a.status")
    List<Object[]> countAlertsByStatus();

    // Count alerts by type
    @Query("SELECT a.type, COUNT(a) FROM Alert a GROUP BY a.type")
    List<Object[]> countAlertsByType();

    // Count alerts by priority
    @Query("SELECT a.priority, COUNT(a) FROM Alert a GROUP BY a.priority")
    List<Object[]> countAlertsByPriority();

    // Find alerts with specific email
    List<Alert> findByRecipientEmail(String email);

    // Find alerts that need cleanup (old and processed)
    @Query("SELECT a FROM Alert a WHERE a.status IN ('READ', 'DELIVERED', 'EXPIRED', 'CANCELLED') " +
            "AND a.updatedAt < :cleanupDate")
    List<Alert> findAlertsForCleanup(@Param("cleanupDate") LocalDateTime cleanupDate);

    // Find high priority pending alerts
    @Query("SELECT a FROM Alert a WHERE a.priority IN ('HIGH', 'URGENT', 'EMERGENCY') " +
            "AND a.status = 'PENDING' ORDER BY a.priority DESC, a.createdAt ASC")
    List<Alert> findHighPriorityPendingAlerts();

    // Find alerts by appointment and type
    List<Alert> findByAppointmentIdAndType(Long appointmentId, AlertType type);

    // Find latest alert for patient by type
    @Query("SELECT a FROM Alert a WHERE a.patient.id = :patientId AND a.type = :type " +
            "ORDER BY a.createdAt DESC")
    List<Alert> findLatestAlertByPatientAndType(@Param("patientId") Long patientId,
            @Param("type") AlertType type);

    // Check if alert exists for appointment and type
    boolean existsByAppointmentIdAndType(Long appointmentId, AlertType type);

    // Delete old processed alerts
    @Query("DELETE FROM Alert a WHERE a.status IN ('READ', 'DELIVERED', 'EXPIRED', 'CANCELLED') " +
            "AND a.updatedAt < :cleanupDate")
    void deleteProcessedAlertsBefore(@Param("cleanupDate") LocalDateTime cleanupDate);

    // Find alerts with failed status that exceeded max retries
    @Query("SELECT a FROM Alert a WHERE a.status = 'FAILED' AND a.retryCount >= a.maxRetries")
    List<Alert> findFailedAlertsExceededRetries();
}