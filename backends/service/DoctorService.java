package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Doctor save(Doctor doctor) {
        // Encrypt password before saving (only if password is provided)
        if (doctor.getPassword() != null && !doctor.getPassword().trim().isEmpty()) {
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        }
        return doctorRepository.save(doctor);
    }

    public Doctor saveWithoutPasswordEncoding(Doctor doctor) {
        // Save without encoding password (used for updates when password is already
        // encoded)
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public Optional<Doctor> findByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public List<Doctor> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> findBySpecialty(String specialty) {
        return doctorRepository.findBySpecialization(specialty);
    }

    public List<Doctor> searchDoctors(String searchTerm) {
        String searchLower = searchTerm.toLowerCase();
        return doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getFirstName().toLowerCase().contains(searchLower) ||
                        doctor.getLastName().toLowerCase().contains(searchLower) ||
                        doctor.getSpecialization().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }

    public List<String> getAllSpecialties() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialization)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean existsByEmail(String email) {
        return doctorRepository.existsByEmail(email);
    }

    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }
}