package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.CounselorAvailability;
import com.hospital.smartpms.entity.CounselorAvailability.AvailabilityType;
import com.hospital.smartpms.entity.Counselor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CounselorAvailabilityRepository extends JpaRepository<CounselorAvailability, Long> {

    // Find availability by counselor
    List<CounselorAvailability> findByCounselor(Counselor counselor);

    // Find availability by counselor and day
    List<CounselorAvailability> findByCounselorAndDayOfWeek(Counselor counselor, DayOfWeek dayOfWeek);

    // Find availability by counselor and availability type
    List<CounselorAvailability> findByCounselorAndAvailabilityType(
            Counselor counselor, AvailabilityType availabilityType);

    // Find available slots for counselor on specific day
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor = :counselor " +
            "AND ca.dayOfWeek = :dayOfWeek AND ca.isAvailable = TRUE " +
            "AND ca.availabilityType = 'REGULAR'")
    List<CounselorAvailability> findRegularAvailabilityByCounselorAndDay(
            @Param("counselor") Counselor counselor, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    // Find specific date availability
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor = :counselor " +
            "AND ca.specificDate IS NOT NULL " +
            "AND CAST(ca.specificDate AS DATE) = CAST(:date AS DATE) " +
            "AND ca.isAvailable = TRUE")
    List<CounselorAvailability> findSpecificDateAvailability(
            @Param("counselor") Counselor counselor, @Param("date") LocalDateTime date);

    // Find available counselors for specific day and time
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.dayOfWeek = :dayOfWeek " +
            "AND ca.startTime <= :time AND ca.endTime > :time " +
            "AND ca.isAvailable = TRUE AND ca.availabilityType = 'REGULAR' " +
            "AND ca.counselor.availabilityStatus = 'AVAILABLE'")
    List<CounselorAvailability> findAvailableCounselorsForDayAndTime(
            @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("time") LocalTime time);

    // Find all available time slots for counselor
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor = :counselor " +
            "AND ca.isAvailable = TRUE " +
            "ORDER BY ca.dayOfWeek ASC, ca.startTime ASC")
    List<CounselorAvailability> findAllAvailableSlotsByCounselor(@Param("counselor") Counselor counselor);

    // Check if counselor is available at specific time
    @Query("SELECT COUNT(ca) > 0 FROM CounselorAvailability ca WHERE ca.counselor = :counselor " +
            "AND ((ca.dayOfWeek = :dayOfWeek AND ca.availabilityType = 'REGULAR') " +
            "OR (ca.specificDate IS NOT NULL AND CAST(ca.specificDate AS DATE) = CAST(:date AS DATE))) " +
            "AND ca.startTime <= :time AND ca.endTime > :time " +
            "AND ca.isAvailable = TRUE")
    boolean isCounselorAvailableAtTime(
            @Param("counselor") Counselor counselor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("date") LocalDateTime date,
            @Param("time") LocalTime time);

    // Find overlapping availability (for validation)
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.counselor = :counselor " +
            "AND ca.dayOfWeek = :dayOfWeek " +
            "AND ca.availabilityType = 'REGULAR' " +
            "AND ca.id != :excludeId " +
            "AND ((ca.startTime <= :startTime AND ca.endTime > :startTime) " +
            "OR (ca.startTime < :endTime AND ca.endTime >= :endTime) " +
            "OR (ca.startTime >= :startTime AND ca.endTime <= :endTime))")
    List<CounselorAvailability> findOverlappingAvailability(
            @Param("counselor") Counselor counselor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // Find availability by type
    List<CounselorAvailability> findByAvailabilityType(AvailabilityType availabilityType);

    // Find emergency availability
    List<CounselorAvailability> findByAvailabilityTypeAndIsAvailableTrue(AvailabilityType availabilityType);

    // Get total available hours per counselor
    @Query("SELECT ca.counselor, SUM(TIMESTAMPDIFF(MINUTE, ca.startTime, ca.endTime)) " +
            "FROM CounselorAvailability ca WHERE ca.isAvailable = TRUE " +
            "AND ca.availabilityType = 'REGULAR' " +
            "GROUP BY ca.counselor")
    List<Object[]> getTotalAvailableHoursBycounselor();

    // Find availability that needs update
    @Query("SELECT ca FROM CounselorAvailability ca WHERE ca.availabilityType = 'ONE_TIME' " +
            "AND ca.specificDate < CURRENT_TIMESTAMP")
    List<CounselorAvailability> findExpiredOneTimeAvailability();

    // Delete old one-time availability
    @Query("DELETE FROM CounselorAvailability ca WHERE ca.availabilityType = 'ONE_TIME' " +
            "AND ca.specificDate < :cutoffDate")
    void deleteExpiredOneTimeAvailability(@Param("cutoffDate") LocalDateTime cutoffDate);
}