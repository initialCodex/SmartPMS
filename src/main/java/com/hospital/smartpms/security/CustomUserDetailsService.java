package com.hospital.smartpms.security;

import com.hospital.smartpms.entity.Admin;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.service.AdminService;
import com.hospital.smartpms.service.DoctorService;
import com.hospital.smartpms.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AdminService adminService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);

        // Try to find patient first
        Optional<Patient> patient = patientService.findByEmail(email);
        if (patient.isPresent()) {
            Patient p = patient.get();
            logger.info("Found patient: {} with ID: {}", p.getFullName(), p.getId());
            return new CustomUserDetails(p.getId(), p.getEmail(), p.getPassword(), p.getFullName(), "PATIENT");
        } else {
            logger.debug("No patient found with email: {}", email);
        }

        // Try to find doctor
        Optional<Doctor> doctor = doctorService.findByEmail(email);
        if (doctor.isPresent()) {
            Doctor d = doctor.get();
            logger.info("Found doctor: {} with ID: {}", d.getFullName(), d.getId());
            return new CustomUserDetails(d.getId(), d.getEmail(), d.getPassword(), d.getFullName(), "DOCTOR");
        } else {
            logger.debug("No doctor found with email: {}", email);
        }

        // Try to find admin
        Optional<Admin> admin = adminService.findByEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            logger.info("Found admin: {} with ID: {}", a.getFullName(), a.getId());
            return new CustomUserDetails(a.getId(), a.getEmail(), a.getPassword(), a.getFullName(), "ADMIN");
        } else {
            logger.debug("No admin found with email: {}", email);
        }

        logger.error("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}