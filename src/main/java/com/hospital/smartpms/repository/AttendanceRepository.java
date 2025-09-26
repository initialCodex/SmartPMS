package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.Attendance;
import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find attendance by appointment
    Optional<Attendance> findByAppointment(Appointment appointment);

    // Find attendance by appointment ID
    Optional<Attendance> findByAppointmentId(Long appointmentId);

    // Find attendance by QR code
    Optional<Attendance> findByQrCode(String qrCode);

    // Find attendances by status
    List<Attendance> findByStatus(Attendance.AttendanceStatus status);

    // Find attendances by patient
    @Query("SELECT a FROM Attendance a WHERE a.appointment.patient = :patient ORDER BY a.createdAt DESC")
    List<Attendance> findByPatient(@Param("patient") Patient patient);

    // Find attendances by doctor
    @Query("SELECT a FROM Attendance a WHERE a.appointment.doctor = :doctor ORDER BY a.createdAt DESC")
    List<Attendance> findByDoctor(@Param("doctor") Doctor doctor);

    // Find attendances by date
    @Query("SELECT a FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) = :date ORDER BY a.appointment.appointmentDate")
    List<Attendance> findByDate(@Param("date") LocalDate date);

    // Find today's attendances
    @Query("SELECT a FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) = CURRENT_DATE ORDER BY a.appointment.appointmentDate")
    List<Attendance> findTodaysAttendances();

    // Find attendances between dates
    @Query("SELECT a FROM Attendance a WHERE a.appointment.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointment.appointmentDate")
    List<Attendance> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find no-show attendances
    List<Attendance> findByStatusOrderByCreatedAtDesc(Attendance.AttendanceStatus status);

    // Count attendances by status
    long countByStatus(Attendance.AttendanceStatus status);

    // Count no-shows for a patient
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.appointment.patient = :patient AND a.status = 'NO_SHOW'")
    long countNoShowsByPatient(@Param("patient") Patient patient);

    // Count attendances for a doctor on a specific date
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.appointment.doctor = :doctor AND CAST(a.appointment.appointmentDate AS DATE) = :date")
    long countByDoctorAndDate(@Param("doctor") Doctor doctor, @Param("date") LocalDate date);

    // Find late check-ins
    @Query("SELECT a FROM Attendance a WHERE a.status = 'LATE' ORDER BY a.checkInTime DESC")
    List<Attendance> findLateCheckIns();

    // Find patients with multiple no-shows (attendance tracking)
    @Query("SELECT a.appointment.patient, COUNT(a) FROM Attendance a WHERE a.status = 'NO_SHOW' GROUP BY a.appointment.patient HAVING COUNT(a) >= :threshold")
    List<Object[]> findPatientsWithMultipleNoShows(@Param("threshold") int threshold);

    // Find attendance statistics for a date range
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) BETWEEN :startDate AND :endDate GROUP BY a.status")
    List<Object[]> getAttendanceStatistics(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find recent check-ins (for real-time monitoring)
    @Query("SELECT a FROM Attendance a WHERE a.checkInTime >= :since ORDER BY a.checkInTime DESC")
    List<Attendance> findRecentCheckIns(@Param("since") LocalDateTime since);

    // Find pending attendances (scheduled but not checked in) for today
    @Query("SELECT a FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) = CURRENT_DATE AND a.status = 'SCHEDULED' ORDER BY a.appointment.appointmentDate")
    List<Attendance> findTodaysPendingAttendances();

    // Find overdue check-ins (appointments that should have started but patient
    // hasn't checked in)
    @Query("SELECT a FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) = CURRENT_DATE AND a.status = 'SCHEDULED' AND a.appointment.appointmentDate < CURRENT_TIMESTAMP")
    List<Attendance> findOverdueCheckIns();

    // Check if attendance exists for appointment
    boolean existsByAppointment(Appointment appointment);

    // Find by multiple statuses
    List<Attendance> findByStatusIn(List<Attendance.AttendanceStatus> statuses);

    // Find attendances with check-in method
    List<Attendance> findByCheckInMethod(Attendance.CheckInMethod checkInMethod);

    // Find today's attendances by status
    @Query("SELECT a FROM Attendance a WHERE CAST(a.appointment.appointmentDate AS DATE) = CURRENT_DATE AND a.status = :status ORDER BY a.appointment.appointmentDate")
    List<Attendance> findTodaysAttendancesByStatus(@Param("status") Attendance.AttendanceStatus status);

    // Find patients with missed appointments (for alert system)
    @Query("SELECT a.appointment.patient.id, COUNT(a) FROM Attendance a WHERE a.status = 'NO_SHOW' AND a.createdAt >= :since GROUP BY a.appointment.patient.id")
    List<Object[]> findPatientsMissedAppointments(@Param("since") LocalDateTime since);
}