package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Waitlist;
import com.hospital.smartpms.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WaitlistService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    public Waitlist save(Waitlist waitlist) {
        return waitlistRepository.save(waitlist);
    }

    public Optional<Waitlist> findById(Long id) {
        return waitlistRepository.findById(id);
    }

    public List<Waitlist> findAll() {
        return waitlistRepository.findAll();
    }

    public List<Waitlist> findByPatient(Patient patient) {
        return waitlistRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<Waitlist> findByDoctor(Doctor doctor) {
        return waitlistRepository.findByDoctorOrderByPriorityDescCreatedAtAsc(doctor);
    }

    public List<Waitlist> findWaitingByDoctor(Doctor doctor) {
        return waitlistRepository.findByDoctorAndStatusOrderByPriorityDescCreatedAtAsc(doctor,
                Waitlist.WaitlistStatus.WAITING);
    }

    public List<Waitlist> findByStatus(Waitlist.WaitlistStatus status) {
        return waitlistRepository.findByStatusOrderByPriorityDescCreatedAtAsc(status);
    }

    public Long countWaitingByDoctor(Doctor doctor) {
        return waitlistRepository.countWaitingByDoctor(doctor);
    }

    public boolean isPatientInWaitlist(Patient patient, Doctor doctor) {
        return waitlistRepository.existsByPatientAndDoctorAndStatus(patient, doctor, Waitlist.WaitlistStatus.WAITING);
    }

    public Waitlist addToWaitlist(Patient patient, Doctor doctor, LocalDateTime preferredDate, String reason) {
        // Check if patient is already in waitlist for this doctor
        if (isPatientInWaitlist(patient, doctor)) {
            throw new RuntimeException("Patient is already in waitlist for this doctor");
        }

        Waitlist waitlist = new Waitlist(patient, doctor, preferredDate, reason);
        return save(waitlist);
    }

    public void removeFromWaitlist(Long waitlistId) {
        Optional<Waitlist> waitlistOpt = findById(waitlistId);
        if (waitlistOpt.isPresent()) {
            Waitlist waitlist = waitlistOpt.get();
            waitlist.setStatus(Waitlist.WaitlistStatus.CANCELLED);
            save(waitlist);
        }
    }

    public void notifyPatient(Long waitlistId) {
        Optional<Waitlist> waitlistOpt = findById(waitlistId);
        if (waitlistOpt.isPresent()) {
            Waitlist waitlist = waitlistOpt.get();
            waitlist.setStatus(Waitlist.WaitlistStatus.NOTIFIED);
            waitlist.setNotifiedAt(LocalDateTime.now());
            save(waitlist);
        }
    }

    public void markAsScheduled(Long waitlistId) {
        Optional<Waitlist> waitlistOpt = findById(waitlistId);
        if (waitlistOpt.isPresent()) {
            Waitlist waitlist = waitlistOpt.get();
            waitlist.setStatus(Waitlist.WaitlistStatus.SCHEDULED);
            save(waitlist);
        }
    }

    public void updatePriority(Long waitlistId, Integer priority) {
        Optional<Waitlist> waitlistOpt = findById(waitlistId);
        if (waitlistOpt.isPresent()) {
            Waitlist waitlist = waitlistOpt.get();
            waitlist.setPriority(priority);
            save(waitlist);
        }
    }

    public List<Waitlist> findWaitingByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return waitlistRepository.findWaitingByDateRange(startDate, endDate);
    }

    public void processExpiredWaitlists() {
        // Mark waitlists as expired if notified but not scheduled within 24 hours
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Waitlist> notifiedWaitlists = findByStatus(Waitlist.WaitlistStatus.NOTIFIED);

        for (Waitlist waitlist : notifiedWaitlists) {
            if (waitlist.getNotifiedAt() != null && waitlist.getNotifiedAt().isBefore(cutoffTime)) {
                waitlist.setStatus(Waitlist.WaitlistStatus.EXPIRED);
                save(waitlist);
            }
        }
    }
}