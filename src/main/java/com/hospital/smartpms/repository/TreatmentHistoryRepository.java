package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.TreatmentHistory;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentHistoryRepository extends JpaRepository<TreatmentHistory, Long> {
    @Query("SELECT t FROM TreatmentHistory t JOIN FETCH t.doctor WHERE t.patient = :patient ORDER BY t.treatmentDate DESC")
    List<TreatmentHistory> findByPatientOrderByTreatmentDateDesc(@Param("patient") Patient patient);

    @Query("SELECT t FROM TreatmentHistory t JOIN FETCH t.patient WHERE t.doctor = :doctor ORDER BY t.treatmentDate DESC")
    List<TreatmentHistory> findByDoctorOrderByTreatmentDateDesc(@Param("doctor") Doctor doctor);

    @Query("SELECT t FROM TreatmentHistory t JOIN FETCH t.doctor JOIN FETCH t.patient WHERE t.id = :id")
    Optional<TreatmentHistory> findByIdWithDoctor(@Param("id") Long id);
}