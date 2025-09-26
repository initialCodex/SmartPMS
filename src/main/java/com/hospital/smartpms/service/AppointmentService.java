package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Patient.PatientStatus;
import com.hospital.smartpms.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findByPatient(Patient patient) {
        return appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
    }

    public List<Appointment> findByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorOrderByAppointmentDateDesc(doctor);
    }

    public List<Appointment> findTodaysAppointments() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay);
    }

    public List<Appointment> findTodaysAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        // Debug logging
        System.out.println("🔍 DEBUG - Today's range: " + startOfDay + " to " + endOfDay);
        System.out.println("🔍 DEBUG - Current time: " + LocalDateTime.now());

        List<Appointment> result = appointmentRepository.findByDoctorAndAppointmentDateBetween(doctor, startOfDay,
                endOfDay);

        System.out.println(
                "🔍 DEBUG - Found " + result.size() + " today's appointments for doctor " + doctor.getFirstName());
        for (Appointment app : result) {
            System.out.println("  - " + app.getAppointmentDate() + " | " + app.getReason() + " | " + app.getStatus());
        }

        return result;
    }

    public List<Appointment> findUpcomingAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureLimit = now.plusDays(30); // Next 30 days
        return appointmentRepository.findByDoctorAndAppointmentDateBetween(doctor, now, futureLimit);
    }

    public boolean isTimeSlotAvailable(Doctor doctor, LocalDateTime appointmentDateTime) {
        // Check if the doctor has any appointment within 1 hour of the requested time
        LocalDateTime startWindow = appointmentDateTime.minusMinutes(30);
        LocalDateTime endWindow = appointmentDateTime.plusMinutes(30);

        List<Appointment> conflictingAppointments = appointmentRepository.findByDoctorAndAppointmentDateBetween(
                doctor, startWindow, endWindow);

        return conflictingAppointments.isEmpty();
    }

    public List<LocalDateTime> getAvailableSlots(Doctor doctor, LocalDate startDate, LocalDate endDate) {
        List<LocalDateTime> availableSlots = new ArrayList<>();

        // Define working hours (9 AM to 5 PM)
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        // Get all existing appointments for the doctor in the date range
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(23, 59);
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorAndAppointmentDateBetween(doctor, rangeStart, rangeEnd);

        // Generate all possible slots (1-hour intervals)
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends (assuming doctor doesn't work weekends)
            if (currentDate.getDayOfWeek().getValue() < 6) {
                LocalTime currentTime = startTime;
                while (currentTime.isBefore(endTime)) {
                    LocalDateTime slotDateTime = LocalDateTime.of(currentDate, currentTime);

                    // Only include future slots
                    if (slotDateTime.isAfter(LocalDateTime.now())) {
                        // Check if this slot conflicts with existing appointments
                        boolean isAvailable = existingAppointments.stream()
                                .noneMatch(appointment -> {
                                    LocalDateTime appointmentTime = appointment.getAppointmentDate();
                                    return appointmentTime.isAfter(slotDateTime.minusMinutes(30)) &&
                                            appointmentTime.isBefore(slotDateTime.plusMinutes(30));
                                });

                        if (isAvailable) {
                            availableSlots.add(slotDateTime);
                        }
                    }

                    currentTime = currentTime.plusHours(1);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return availableSlots.stream()
                .sorted()
                .limit(20) // Limit to next 20 available slots
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> searchAppointments(String searchTerm, Appointment.AppointmentStatus status,
            PatientStatus patientStatus) {
        return appointmentRepository.searchAppointments(searchTerm, status, patientStatus);
    }

    public List<Appointment> findByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    public Appointment updateStatus(Long appointmentId, Appointment.AppointmentStatus status) {
        Optional<Appointment> appointmentOpt = findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(status);
            return save(appointment);
        }
        return null;
    }
}