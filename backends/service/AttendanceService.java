package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Attendance;
import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.repository.AttendanceRepository;
import com.hospital.smartpms.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Create attendance record for appointment
    public Attendance createAttendanceRecord(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        // Check if attendance record already exists
        Optional<Attendance> existingAttendance = attendanceRepository.findByAppointment(appointment);
        if (existingAttendance.isPresent()) {
            return existingAttendance.get();
        }

        Attendance attendance = new Attendance(appointment);
        return attendanceRepository.save(attendance);
    }

    // Check in patient using QR code
    public Attendance checkInByQrCode(String qrCode, String location) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findByQrCode(qrCode);
        if (!attendanceOpt.isPresent()) {
            throw new RuntimeException("Invalid QR code");
        }

        Attendance attendance = attendanceOpt.get();

        // Validate appointment date (today only)
        LocalDate appointmentDate = attendance.getAppointment().getAppointmentDate().toLocalDate();
        if (!appointmentDate.equals(LocalDate.now())) {
            throw new RuntimeException("QR code is only valid on appointment date");
        }

        // Check if already checked in
        if (attendance.getStatus() != Attendance.AttendanceStatus.SCHEDULED) {
            throw new RuntimeException("Patient has already checked in");
        }

        attendance.checkIn(Attendance.CheckInMethod.QR_CODE);
        attendance.setLocation(location);

        // Check if late
        if (attendance.isLate()) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        }

        return attendanceRepository.save(attendance);
    }

    // Manual check-in by staff
    public Attendance manualCheckIn(Long appointmentId, String location, String notes) {
        Attendance attendance = getOrCreateAttendance(appointmentId);

        if (attendance.getStatus() != Attendance.AttendanceStatus.SCHEDULED) {
            throw new RuntimeException("Patient has already checked in");
        }

        attendance.checkIn(Attendance.CheckInMethod.MANUAL);
        attendance.setLocation(location);
        attendance.setNotes(notes);

        if (attendance.isLate()) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        }

        return attendanceRepository.save(attendance);
    }

    // Online check-in
    public Attendance onlineCheckIn(Long appointmentId, Patient patient) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        // Verify patient owns this appointment
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }

        // Check if appointment is today
        if (!appointment.getAppointmentDate().toLocalDate().equals(LocalDate.now())) {
            throw new RuntimeException("Online check-in only available on appointment date");
        }

        // Check if appointment time is within check-in window (1 hour before to 30
        // minutes after)
        LocalDateTime appointmentDateTime = appointment.getAppointmentDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInStart = appointmentDateTime.minusHours(1);
        LocalDateTime checkInEnd = appointmentDateTime.plusMinutes(30);

        if (now.isBefore(checkInStart)) {
            throw new RuntimeException("Check-in not yet available. You can check in 1 hour before your appointment.");
        }

        if (now.isAfter(checkInEnd)) {
            throw new RuntimeException("Check-in window has closed. Please contact reception.");
        }

        Attendance attendance = getOrCreateAttendance(appointmentId);

        if (attendance.getStatus() != Attendance.AttendanceStatus.SCHEDULED) {
            throw new RuntimeException("Already checked in");
        }

        attendance.checkIn(Attendance.CheckInMethod.ONLINE);

        if (attendance.isLate()) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        }

        return attendanceRepository.save(attendance);
    }

    // Mark patient as present
    public Attendance markPresent(Long attendanceId) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
        if (!attendanceOpt.isPresent()) {
            throw new RuntimeException("Attendance record not found");
        }

        Attendance attendance = attendanceOpt.get();
        attendance.markPresent();
        return attendanceRepository.save(attendance);
    }

    // Mark patient as no-show
    public Attendance markNoShow(Long attendanceId, String notes) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
        if (!attendanceOpt.isPresent()) {
            throw new RuntimeException("Attendance record not found");
        }

        Attendance attendance = attendanceOpt.get();
        attendance.markNoShow();
        attendance.setNotes(notes);
        return attendanceRepository.save(attendance);
    }

    // Get or create attendance record
    private Attendance getOrCreateAttendance(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        Optional<Attendance> existingAttendance = attendanceRepository.findByAppointment(appointment);

        if (existingAttendance.isPresent()) {
            return existingAttendance.get();
        }

        return new Attendance(appointment);
    }

    // Get attendance by appointment ID
    public Optional<Attendance> getAttendanceByAppointmentId(Long appointmentId) {
        return attendanceRepository.findByAppointmentId(appointmentId);
    }

    // Get all attendances
    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    // Get today's attendances
    public List<Attendance> getTodaysAttendances() {
        return attendanceRepository.findTodaysAttendances();
    }

    // Get attendances by patient
    public List<Attendance> getAttendancesByPatient(Patient patient) {
        return attendanceRepository.findByPatient(patient);
    }

    // Get attendances by doctor
    public List<Attendance> getAttendancesByDoctor(Doctor doctor) {
        return attendanceRepository.findByDoctor(doctor);
    }

    // Get attendances by status
    public List<Attendance> getAttendancesByStatus(Attendance.AttendanceStatus status) {
        return attendanceRepository.findByStatus(status);
    }

    // Get attendance statistics
    public AttendanceStatistics getAttendanceStatistics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> stats = attendanceRepository.getAttendanceStatistics(startDate, endDate);

        AttendanceStatistics statistics = new AttendanceStatistics();
        for (Object[] stat : stats) {
            Attendance.AttendanceStatus status = (Attendance.AttendanceStatus) stat[0];
            Long count = (Long) stat[1];

            switch (status) {
                case PRESENT:
                    statistics.setPresentCount(count);
                    break;
                case NO_SHOW:
                    statistics.setNoShowCount(count);
                    break;
                case CHECKED_IN:
                    statistics.setCheckedInCount(count);
                    break;
                case LATE:
                    statistics.setLateCount(count);
                    break;
                case SCHEDULED:
                    statistics.setScheduledCount(count);
                    break;
                case CANCELLED:
                    statistics.setCancelledCount(count);
                    break;
            }
        }

        statistics.calculateTotals();
        return statistics;
    }

    // Get overdue check-ins
    public List<Attendance> getOverdueCheckIns() {
        return attendanceRepository.findOverdueCheckIns();
    }

    // Get pending attendances for today
    public List<Attendance> getTodaysPendingAttendances() {
        return attendanceRepository.findTodaysPendingAttendances();
    }

    // Get recent check-ins
    public List<Attendance> getRecentCheckIns(int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return attendanceRepository.findRecentCheckIns(since);
    }

    // Get patient no-show count
    public long getPatientNoShowCount(Patient patient) {
        return attendanceRepository.countNoShowsByPatient(patient);
    }

    // Get today's attendances by status
    public List<Attendance> getTodaysAttendancesByStatus(Attendance.AttendanceStatus status) {
        return attendanceRepository.findTodaysAttendancesByStatus(status);
    }

    // Get patients with multiple no-shows
    public List<Object[]> getPatientsWithMultipleNoShows(int threshold) {
        return attendanceRepository.findPatientsWithMultipleNoShows(threshold);
    }

    // Process overdue appointments (mark as no-show)
    public void processOverdueAppointments() {
        List<Attendance> overdueAttendances = getOverdueCheckIns();

        for (Attendance attendance : overdueAttendances) {
            LocalDateTime appointmentDateTime = attendance.getAppointment().getAppointmentDate();

            // If appointment was more than 30 minutes ago, mark as no-show
            if (LocalDateTime.now().isAfter(appointmentDateTime.plusMinutes(30))) {
                attendance.markNoShow();
                attendance.setNotes("Automatically marked as no-show - patient did not check in");
                attendanceRepository.save(attendance);
            }
        }
    }

    // Inner class for statistics
    public static class AttendanceStatistics {
        private long totalCount;
        private long presentCount;
        private long noShowCount;
        private long checkedInCount;
        private long lateCount;
        private long scheduledCount;
        private long cancelledCount;
        private double attendanceRate;
        private double noShowRate;
        private double lateRate;

        public void calculateTotals() {
            this.totalCount = presentCount + noShowCount + checkedInCount + lateCount + scheduledCount + cancelledCount;

            if (totalCount > 0) {
                this.attendanceRate = (double) (presentCount + checkedInCount + lateCount) / totalCount * 100;
                this.noShowRate = (double) noShowCount / totalCount * 100;
                this.lateRate = (double) lateCount / totalCount * 100;
            }
        }

        // Getters and setters
        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public long getPresentCount() {
            return presentCount;
        }

        public void setPresentCount(long presentCount) {
            this.presentCount = presentCount;
        }

        public long getNoShowCount() {
            return noShowCount;
        }

        public void setNoShowCount(long noShowCount) {
            this.noShowCount = noShowCount;
        }

        public long getCheckedInCount() {
            return checkedInCount;
        }

        public void setCheckedInCount(long checkedInCount) {
            this.checkedInCount = checkedInCount;
        }

        public long getLateCount() {
            return lateCount;
        }

        public void setLateCount(long lateCount) {
            this.lateCount = lateCount;
        }

        public long getScheduledCount() {
            return scheduledCount;
        }

        public void setScheduledCount(long scheduledCount) {
            this.scheduledCount = scheduledCount;
        }

        public long getCancelledCount() {
            return cancelledCount;
        }

        public void setCancelledCount(long cancelledCount) {
            this.cancelledCount = cancelledCount;
        }

        public double getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(double attendanceRate) {
            this.attendanceRate = attendanceRate;
        }

        public double getNoShowRate() {
            return noShowRate;
        }

        public void setNoShowRate(double noShowRate) {
            this.noShowRate = noShowRate;
        }

        public double getLateRate() {
            return lateRate;
        }

        public void setLateRate(double lateRate) {
            this.lateRate = lateRate;
        }
    }
}