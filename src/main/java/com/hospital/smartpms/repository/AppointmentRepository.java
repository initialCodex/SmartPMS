package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Patient.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientOrderByAppointmentDateDesc(Patient patient);

    List<Appointment> findByDoctorOrderByAppointmentDateDesc(Doctor doctor);

    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorAndAppointmentDateBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    // Find appointments that are overdue for check-in (for alert system)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate < :overdueTime AND a.id NOT IN " +
            "(SELECT att.appointment.id FROM Attendance att WHERE att.appointment.id = a.id AND att.status != 'SCHEDULED')")
    List<Appointment> findOverdueForCheckin(@Param("overdueTime") LocalDateTime overdueTime);

    // Search appointments by various criteria including patient status
    @Query("SELECT a FROM Appointment a JOIN a.patient p JOIN a.doctor d WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:patientStatus IS NULL OR p.status = :patientStatus) " +
            "ORDER BY a.appointmentDate DESC")
    List<Appointment> searchAppointments(@Param("searchTerm") String searchTerm,
            @Param("status") Appointment.AppointmentStatus status,
            @Param("patientStatus") PatientStatus patientStatus);
}