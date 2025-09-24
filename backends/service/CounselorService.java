package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Counselor;
import com.hospital.smartpms.repository.CounselorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CounselorService {

    @Autowired
    private CounselorRepository counselorRepository;

    public Counselor save(Counselor counselor) {
        return counselorRepository.save(counselor);
    }

    public Optional<Counselor> findById(Long id) {
        return counselorRepository.findById(id);
    }

    public List<Counselor> findAll() {
        return counselorRepository.findAll();
    }

    public List<Counselor> findByCounselorType(Counselor.CounselorType type) {
        return counselorRepository.findByCounselorType(type);
    }

    public List<Counselor> findMentalHealthCounselors() {
        return counselorRepository.findBySpecialization(Counselor.Specialization.MENTAL_HEALTH);
    }

    public List<Counselor> findAvailableCounselors() {
        return counselorRepository.findByAvailabilityStatus(Counselor.AvailabilityStatus.AVAILABLE);
    }

    public void deleteById(Long id) {
        counselorRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return counselorRepository.existsById(id);
    }

    public long count() {
        return counselorRepository.count();
    }

    public List<String> getAllSpecializations() {
        return counselorRepository.findAll().stream()
                .map(counselor -> counselor.getSpecialization().getDisplayName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Counselor> searchCounselors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return counselorRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public List<Counselor> findBySpecialization(String specializationName) {
        if (specializationName == null || specializationName.trim().isEmpty()) {
            return findAll();
        }

        // Find the enum value that matches the display name
        for (Counselor.Specialization spec : Counselor.Specialization.values()) {
            if (spec.getDisplayName().equalsIgnoreCase(specializationName)) {
                return counselorRepository.findBySpecialization(spec);
            }
        }

        // If no exact match, return empty list
        return List.of();
    }

    public List<Counselor> findBySpecializationAndSearch(String specializationName, String searchTerm) {
        if (specializationName == null || specializationName.trim().isEmpty()) {
            return searchCounselors(searchTerm);
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findBySpecialization(specializationName);
        }

        // Find the enum value that matches the display name
        for (Counselor.Specialization spec : Counselor.Specialization.values()) {
            if (spec.getDisplayName().equalsIgnoreCase(specializationName)) {
                // Filter by specialization first, then by search term
                return counselorRepository.findBySpecialization(spec).stream()
                        .filter(counselor -> counselor.getFirstName().toLowerCase().contains(searchTerm.toLowerCase())
                                ||
                                counselor.getLastName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                (counselor.getFirstName() + " " + counselor.getLastName()).toLowerCase()
                                        .contains(searchTerm.toLowerCase())
                                ||
                                (counselor.getQualifications() != null && counselor.getQualifications().toLowerCase()
                                        .contains(searchTerm.toLowerCase())))
                        .collect(Collectors.toList());
            }
        }

        // If no specialization match, return empty list
        return List.of();
    }

    public List<Counselor> findBySpecializationEnum(Counselor.Specialization specialization) {
        return counselorRepository.findBySpecialization(specialization);
    }
}