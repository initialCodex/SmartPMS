package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.*;
import com.hospital.smartpms.entity.CounselingSession.SessionStatus;
import com.hospital.smartpms.entity.CounselingSession.SessionType;
import com.hospital.smartpms.entity.CounselingSession.SessionMode;
import com.hospital.smartpms.repository.CounselingSessionRepository;
import com.hospital.smartpms.repository.CounselorRepository;
import com.hospital.smartpms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CounselingSessionService {

    @Autowired
    private CounselingSessionRepository counselingSessionRepository;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public CounselingSession bookSession(Long patientId, Long counselorId, LocalDateTime sessionDate,
            SessionType sessionType, SessionMode sessionMode, String reason) {

        // Validate patient
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found");
        }

        // Validate counselor
        Optional<Counselor> counselorOpt = counselorRepository.findById(counselorId);
        if (!counselorOpt.isPresent()) {
            throw new RuntimeException("Counselor not found");
        }

        Patient patient = patientOpt.get();
        Counselor counselor = counselorOpt.get();

        // Check if counselor is available
        if (counselor.getAvailabilityStatus() != Counselor.AvailabilityStatus.AVAILABLE) {
            throw new RuntimeException("Counselor is not currently available");
        }

        // Check for scheduling conflicts
        if (hasSchedulingConflict(counselor, sessionDate)) {
            throw new RuntimeException("Counselor has another session scheduled at this time");
        }

        // Validate session date (must be in the future)
        if (sessionDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot schedule sessions in the past");
        }

        // Create the counseling session
        CounselingSession session = new CounselingSession();
        session.setPatient(patient);
        session.setCounselor(counselor);
        session.setSessionDate(sessionDate);
        session.setSessionType(sessionType);
        session.setSessionMode(sessionMode);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setDurationMinutes(counselor.getSessionDuration());
        session.setSessionFee(counselor.getConsultationFee());
        session.setReason(reason);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        return counselingSessionRepository.save(session);
    }

    public boolean hasSchedulingConflict(Counselor counselor, LocalDateTime sessionDate) {
        // Check for existing sessions within the counselor's session duration
        LocalDateTime startTime = sessionDate.minusMinutes(counselor.getSessionDuration());
        LocalDateTime endTime = sessionDate.plusMinutes(counselor.getSessionDuration());

        List<CounselingSession> conflictingSessions = counselingSessionRepository
                .findByCounselorAndSessionDateBetween(counselor, startTime, endTime);

        return !conflictingSessions.isEmpty();
    }

    public List<CounselingSession> getPatientSessions(Patient patient) {
        return counselingSessionRepository.findByPatientOrderBySessionDateDesc(patient);
    }

    public List<CounselingSession> getCounselorSessions(Counselor counselor) {
        return counselingSessionRepository.findByCounselorOrderBySessionDateDesc(counselor);
    }

    public List<CounselingSession> getUpcomingSessions() {
        return counselingSessionRepository.findUpcomingSessions();
    }

    public List<CounselingSession> getTodaysSessions() {
        return counselingSessionRepository.findTodaysSessions();
    }

    public Optional<CounselingSession> findById(Long id) {
        return counselingSessionRepository.findById(id);
    }

    public CounselingSession cancelSession(Long sessionId, String reason) {
        Optional<CounselingSession> sessionOpt = counselingSessionRepository.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("Session not found");
        }

        CounselingSession session = sessionOpt.get();

        // Check if session can be cancelled
        if (session.getStatus() == SessionStatus.COMPLETED ||
                session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel a completed or already cancelled session");
        }

        session.cancelSession(reason);
        return counselingSessionRepository.save(session);
    }

    public CounselingSession rescheduleSession(Long sessionId, LocalDateTime newSessionDate) {
        Optional<CounselingSession> sessionOpt = counselingSessionRepository.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("Session not found");
        }

        CounselingSession session = sessionOpt.get();

        // Check if session can be rescheduled
        if (session.getStatus() == SessionStatus.COMPLETED ||
                session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Cannot reschedule a completed or cancelled session");
        }

        // Check for scheduling conflicts at new time
        if (hasSchedulingConflict(session.getCounselor(), newSessionDate)) {
            throw new RuntimeException("Counselor has another session scheduled at the new time");
        }

        session.setSessionDate(newSessionDate);
        session.setStatus(SessionStatus.RESCHEDULED);
        session.setUpdatedAt(LocalDateTime.now());

        return counselingSessionRepository.save(session);
    }

    public List<CounselingSession> findAll() {
        return counselingSessionRepository.findAll();
    }

    public long count() {
        return counselingSessionRepository.count();
    }
}