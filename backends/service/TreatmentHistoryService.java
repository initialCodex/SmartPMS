package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.TreatmentHistory;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.repository.TreatmentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TreatmentHistoryService {

    @Autowired
    private TreatmentHistoryRepository treatmentHistoryRepository;

    public TreatmentHistory save(TreatmentHistory treatmentHistory) {
        return treatmentHistoryRepository.save(treatmentHistory);
    }

    public Optional<TreatmentHistory> findById(Long id) {
        return treatmentHistoryRepository.findByIdWithDoctor(id);
    }

    public List<TreatmentHistory> findAll() {
        return treatmentHistoryRepository.findAll();
    }

    public List<TreatmentHistory> findByPatient(Patient patient) {
        return treatmentHistoryRepository.findByPatientOrderByTreatmentDateDesc(patient);
    }

    public List<TreatmentHistory> findByDoctor(Doctor doctor) {
        return treatmentHistoryRepository.findByDoctorOrderByTreatmentDateDesc(doctor);
    }

    public TreatmentHistory createTreatmentRecord(Patient patient, Doctor doctor, Appointment appointment,
            String diagnosis, String treatment, String prescription,
            String notes, LocalDateTime followUpDate) {
        TreatmentHistory treatmentHistory = new TreatmentHistory();
        treatmentHistory.setPatient(patient);
        treatmentHistory.setDoctor(doctor);
        treatmentHistory.setAppointment(appointment);
        treatmentHistory.setDiagnosis(diagnosis);
        treatmentHistory.setTreatment(treatment);
        treatmentHistory.setPrescription(prescription);
        treatmentHistory.setNotes(notes);
        treatmentHistory.setFollowUpDate(followUpDate);
        treatmentHistory.setTreatmentDate(LocalDateTime.now());

        return save(treatmentHistory);
    }

    public void deleteById(Long id) {
        treatmentHistoryRepository.deleteById(id);
    }
}