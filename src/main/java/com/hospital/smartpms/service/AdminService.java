package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Admin;
import com.hospital.smartpms.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin save(Admin admin) {
        // Encrypt password before saving
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    public void deleteById(Long id) {
        adminRepository.deleteById(id);
    }
}