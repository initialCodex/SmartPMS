package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    List<Waitlist> findByPatientOrderByCreatedAtDesc(Patient patient);

    List<Waitlist> findByDoctorOrderByPriorityDescCreatedAtAsc(Doctor doctor);

    List<Waitlist> findByStatusOrderByPriorityDescCreatedAtAsc(Waitlist.WaitlistStatus status);

    @Query("SELECT w FROM Waitlist w WHERE w.doctor = :doctor AND w.status = :status ORDER BY w.priority DESC, w.createdAt ASC")
    List<Waitlist> findByDoctorAndStatusOrderByPriorityDescCreatedAtAsc(@Param("doctor") Doctor doctor,
            @Param("status") Waitlist.WaitlistStatus status);

    @Query("SELECT w FROM Waitlist w WHERE w.preferredDate BETWEEN :startDate AND :endDate AND w.status = 'WAITING' ORDER BY w.priority DESC, w.createdAt ASC")
    List<Waitlist> findWaitingByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.doctor = :doctor AND w.status = 'WAITING'")
    Long countWaitingByDoctor(@Param("doctor") Doctor doctor);

    boolean existsByPatientAndDoctorAndStatus(Patient patient, Doctor doctor, Waitlist.WaitlistStatus status);
}