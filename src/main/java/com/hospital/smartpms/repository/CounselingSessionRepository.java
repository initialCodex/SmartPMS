package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.CounselingSession;
import com.hospital.smartpms.entity.CounselingSession.SessionStatus;
import com.hospital.smartpms.entity.CounselingSession.SessionType;
import com.hospital.smartpms.entity.CounselingSession.SessionMode;
import com.hospital.smartpms.entity.Counselor;
import com.hospital.smartpms.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {

    // Find sessions by patient
    List<CounselingSession> findByPatientOrderBySessionDateDesc(Patient patient);

    // Find sessions by counselor
    List<CounselingSession> findByCounselorOrderBySessionDateDesc(Counselor counselor);

    // Find sessions by status
    List<CounselingSession> findByStatus(SessionStatus status);

    // Find sessions by date range
    List<CounselingSession> findBySessionDateBetween(LocalDateTime start, LocalDateTime end);

    // Find sessions by counselor and date range
    List<CounselingSession> findByCounselorAndSessionDateBetween(
            Counselor counselor, LocalDateTime start, LocalDateTime end);

    // Find sessions by patient and date range
    List<CounselingSession> findByPatientAndSessionDateBetween(
            Patient patient, LocalDateTime start, LocalDateTime end);

    // Find today's sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE CAST(cs.sessionDate AS DATE) = CURRENT_DATE " +
            "ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findTodaysSessions();

    // Find today's sessions by counselor
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.counselor = :counselor " +
            "AND CAST(cs.sessionDate AS DATE) = CURRENT_DATE ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findTodaysSessionsByCounselor(@Param("counselor") Counselor counselor);

    // Find today's sessions by patient
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.patient = :patient " +
            "AND CAST(cs.sessionDate AS DATE) = CURRENT_DATE ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findTodaysSessionsByPatient(@Param("patient") Patient patient);

    // Find upcoming sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate > CURRENT_TIMESTAMP " +
            "AND cs.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessions();

    // Find upcoming sessions by counselor
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.counselor = :counselor " +
            "AND cs.sessionDate > CURRENT_TIMESTAMP " +
            "AND cs.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessionsByCounselor(@Param("counselor") Counselor counselor);

    // Find upcoming sessions by patient
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.patient = :patient " +
            "AND cs.sessionDate > CURRENT_TIMESTAMP " +
            "AND cs.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessionsByPatient(@Param("patient") Patient patient);

    // Find sessions by type
    List<CounselingSession> findBySessionType(SessionType sessionType);

    // Find sessions by mode
    List<CounselingSession> findBySessionMode(SessionMode sessionMode);

    // Find emergency sessions
    List<CounselingSession> findByEmergencySessionTrueOrderBySessionDateDesc();

    // Find sessions requiring follow-up
    List<CounselingSession> findByFollowUpRequiredTrueAndFollowUpDateIsNotNull();

    // Find overdue follow-ups
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.followUpRequired = TRUE " +
            "AND cs.followUpDate < CURRENT_TIMESTAMP AND cs.followUpDate IS NOT NULL")
    List<CounselingSession> findOverdueFollowUps();

    // Count sessions by status
    Long countByStatus(SessionStatus status);

    // Count sessions by counselor
    Long countByCounselor(Counselor counselor);

    // Count sessions by patient
    Long countByPatient(Patient patient);

    // Count completed sessions by patient
    Long countByPatientAndStatus(Patient patient, SessionStatus status);

    // Count completed sessions by counselor
    Long countByCounselorAndStatus(Counselor counselor, SessionStatus status);

    // Find sessions with ratings
    List<CounselingSession> findBySessionRatingIsNotNullOrderBySessionRatingDesc();

    // Find average rating by counselor
    @Query("SELECT AVG(cs.sessionRating) FROM CounselingSession cs WHERE cs.counselor = :counselor " +
            "AND cs.sessionRating IS NOT NULL")
    Double findAverageRatingByCounselor(@Param("counselor") Counselor counselor);

    // Find sessions needing reminder
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate BETWEEN :start AND :end " +
            "AND cs.status IN ('SCHEDULED', 'CONFIRMED') " +
            "AND cs.sessionReminderSent = FALSE")
    List<CounselingSession> findSessionsNeedingReminder(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Check for conflicting sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.counselor = :counselor " +
            "AND cs.sessionDate = :sessionDate " +
            "AND cs.status NOT IN ('CANCELLED', 'NO_SHOW')")
    Optional<CounselingSession> findConflictingSession(
            @Param("counselor") Counselor counselor, @Param("sessionDate") LocalDateTime sessionDate);

    // Find sessions by date
    @Query("SELECT cs FROM CounselingSession cs WHERE CAST(cs.sessionDate AS DATE) = :date " +
            "ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findSessionsByDate(@Param("date") LocalDate date);

    // Get session statistics
    @Query("SELECT cs.status, COUNT(cs) FROM CounselingSession cs GROUP BY cs.status")
    List<Object[]> countSessionsByStatus();

    @Query("SELECT cs.sessionType, COUNT(cs) FROM CounselingSession cs GROUP BY cs.sessionType")
    List<Object[]> countSessionsByType();

    @Query("SELECT cs.sessionMode, COUNT(cs) FROM CounselingSession cs GROUP BY cs.sessionMode")
    List<Object[]> countSessionsByMode();

    // Find recent sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate >= :since " +
            "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findRecentSessions(@Param("since") LocalDateTime since);

    // Find sessions by patient and status
    List<CounselingSession> findByPatientAndStatusOrderBySessionDateDesc(Patient patient, SessionStatus status);

    // Find sessions by counselor and status
    List<CounselingSession> findByCounselorAndStatusOrderBySessionDateDesc(Counselor counselor, SessionStatus status);

    // Find next session for patient
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.patient = :patient " +
            "AND cs.sessionDate > CURRENT_TIMESTAMP " +
            "AND cs.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findNextSessionByPatient(@Param("patient") Patient patient);

    // Find last completed session for patient
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.patient = :patient " +
            "AND cs.status = 'COMPLETED' " +
            "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findLastCompletedSessionByPatient(@Param("patient") Patient patient);

    // Monthly session count
    @Query("SELECT MONTH(cs.sessionDate), COUNT(cs) FROM CounselingSession cs " +
            "WHERE YEAR(cs.sessionDate) = :year " +
            "GROUP BY MONTH(cs.sessionDate) " +
            "ORDER BY MONTH(cs.sessionDate)")
    List<Object[]> getMonthlySessionCount(@Param("year") int year);

    // Check if patient has active session
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.patient = :patient " +
            "AND cs.status = 'IN_PROGRESS'")
    Optional<CounselingSession> findActiveSessionByPatient(@Param("patient") Patient patient);
}