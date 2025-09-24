package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Patient save(Patient patient) {
        // Encrypt password before saving
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        return patientRepository.save(patient);
    }

    public Patient update(Patient patient) {
        // Save without re-encoding password (for updates where password is already
        // encoded)
        return patientRepository.save(patient);
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> findByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public List<Patient> searchPatients(String searchTerm) {
        return patientRepository.searchPatients(searchTerm);
    }

    public boolean existsByEmail(String email) {
        return patientRepository.existsByEmail(email);
    }

    public void deleteById(Long id) {
        patientRepository.deleteById(id);
    }
}