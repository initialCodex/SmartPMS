package com.hospital.smartpms.controller;

import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Counselor;
import com.hospital.smartpms.entity.CounselingSession;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.TreatmentHistory;
import com.hospital.smartpms.entity.Waitlist;
import com.hospital.smartpms.security.CustomUserDetails;
import com.hospital.smartpms.service.AppointmentService;
import com.hospital.smartpms.service.CounselorService;
import com.hospital.smartpms.service.CounselingSessionService;
import com.hospital.smartpms.service.DoctorService;
import com.hospital.smartpms.service.PatientService;
import com.hospital.smartpms.service.TreatmentHistoryService;
import com.hospital.smartpms.service.WaitlistService;
import com.hospital.smartpms.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TreatmentHistoryService treatmentHistoryService;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private CounselingSessionService counselingSessionService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/dashboard")
    public String patientDashboard(Model model) {
        model.addAttribute("title", "Patient Dashboard");
        return "patient/dashboard";
    }

    @GetMapping("/doctors")
    public String viewDoctors(@RequestParam(required = false) String specialty,
            @RequestParam(required = false) String search,
            Model model) {
        List<Doctor> doctors;

        if (specialty != null && !specialty.isEmpty()) {
            doctors = doctorService.findBySpecialty(specialty);
        } else if (search != null && !search.isEmpty()) {
            doctors = doctorService.searchDoctors(search);
        } else {
            doctors = doctorService.findAll();
        }

        // Get all unique specialties for filter dropdown
        List<String> specialties = doctorService.getAllSpecialties();

        model.addAttribute("doctors", doctors);
        model.addAttribute("specialties", specialties);
        model.addAttribute("selectedSpecialty", specialty);
        model.addAttribute("searchQuery", search);
        model.addAttribute("title", "Find Doctors");
        return "patient/doctors";
    }

    @GetMapping("/doctors/{id}")
    public String viewDoctorProfile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Doctor> doctorOpt = doctorService.findById(id);

        if (!doctorOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Doctor not found");
            return "redirect:/patient/doctors";
        }

        Doctor doctor = doctorOpt.get();

        // Get doctor's upcoming availability (next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<LocalDateTime> availableSlots;
        try {
            availableSlots = appointmentService.getAvailableSlots(doctor, today, nextWeek);
            if (availableSlots == null) {
                availableSlots = new ArrayList<>();
            }
        } catch (Exception e) {
            // If there's an error getting available slots, provide an empty list
            availableSlots = new ArrayList<>();
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("appointment", new AppointmentForm());
        model.addAttribute("title", "Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
        return "patient/doctor-profile-simple";
    }

    @GetMapping("/book-appointment")
    public String showBookAppointmentForm(@RequestParam(required = false) Long doctorId, Model model) {
        List<Doctor> doctors = doctorService.findAll();
        AppointmentForm appointmentForm = new AppointmentForm();

        // Pre-select doctor if doctorId is provided
        if (doctorId != null) {
            Optional<Doctor> doctorOpt = doctorService.findById(doctorId);
            if (doctorOpt.isPresent()) {
                appointmentForm.setDoctor(doctorOpt.get());
            }
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("appointment", appointmentForm);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("title", "Book Appointment");
        return "patient/book-appointment";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@Valid @ModelAttribute("appointment") AppointmentForm appointmentForm,
            BindingResult result,
            @RequestParam(required = false) Long doctorId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            List<Doctor> doctors = doctorService.findAll();
            model.addAttribute("doctors", doctors);
            return "patient/book-appointment";
        }

        try {
            // Get current patient
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/patient/book-appointment";
            }

            // Get selected doctor - use doctorId parameter or from form
            Long selectedDoctorId = doctorId;
            if (selectedDoctorId == null && appointmentForm.getDoctor() != null) {
                selectedDoctorId = appointmentForm.getDoctor().getId();
            }

            if (selectedDoctorId == null) {
                redirectAttributes.addFlashAttribute("error", "Please select a doctor");
                return "redirect:/patient/book-appointment";
            }

            Optional<Doctor> doctorOpt = doctorService.findById(selectedDoctorId);
            if (!doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/patient/book-appointment";
            }

            // Parse date and time
            LocalDate date = LocalDate.parse(appointmentForm.getAppointmentDate());
            LocalTime time = LocalTime.parse(appointmentForm.getAppointmentTime());
            LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

            // Check if the appointment is in the future
            if (appointmentDateTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error", "Cannot book appointment in the past");
                return "redirect:/patient/book-appointment";
            }

            // Check if time slot is available
            Doctor doctor = doctorOpt.get();
            if (!appointmentService.isTimeSlotAvailable(doctor, appointmentDateTime)) {
                redirectAttributes.addFlashAttribute("error",
                        "Selected time slot is not available. Please choose another time.");
                return "redirect:/patient/book-appointment";
            }

            // Create and save appointment
            Appointment appointment = new Appointment();
            appointment.setPatient(patientOpt.get());
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDateTime);
            appointment.setReason(appointmentForm.getReason());
            appointment.setNotes(appointmentForm.getNotes());
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

            appointmentService.save(appointment);

            redirectAttributes.addFlashAttribute("success",
                    "Appointment booked successfully! You will receive a confirmation shortly.");
            return "redirect:/patient/appointments";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to book appointment. Please try again.");
            return "redirect:/patient/book-appointment";
        }
    }

    @GetMapping("/appointments")
    public String viewAppointments(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                model.addAttribute("error", "Patient not found");
                return "patient/appointments";
            }

            Patient patient = patientOpt.get();
            List<Appointment> appointments = appointmentService.findByPatient(patient);

            // Calculate statistics
            long totalAppointments = appointments.size();
            long completedAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long scheduledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .count();
            long cancelledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();

            model.addAttribute("appointments", appointments);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("scheduledAppointments", scheduledAppointments);
            model.addAttribute("cancelledAppointments", cancelledAppointments);
            model.addAttribute("title", "My Appointments");

            return "patient/appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading appointments");
            return "patient/appointments";
        }
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointmentDetails(@PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);

            if (!patientOpt.isPresent() || !appointmentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/patient/appointments";
            }

            Appointment appointment = appointmentOpt.get();
            // Verify the appointment belongs to the current patient
            if (!appointment.getPatient().getId().equals(patientOpt.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/patient/appointments";
            }

            model.addAttribute("appointment", appointment);
            model.addAttribute("title", "Appointment Details");
            return "patient/appointment-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading appointment details");
            return "redirect:/patient/appointments";
        }
    }

    @GetMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);

            if (!patientOpt.isPresent() || !appointmentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/patient/appointments";
            }

            Appointment appointment = appointmentOpt.get();
            // Verify the appointment belongs to the current patient
            if (!appointment.getPatient().getId().equals(patientOpt.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/patient/appointments";
            }

            // Check if appointment can be cancelled (only scheduled appointments)
            if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
                redirectAttributes.addFlashAttribute("error", "Only scheduled appointments can be cancelled");
                return "redirect:/patient/appointments";
            }

            // Check if appointment is not in the past
            if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error", "Cannot cancel past appointments");
                return "redirect:/patient/appointments";
            }

            appointmentService.updateStatus(id, Appointment.AppointmentStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");

            return "redirect:/patient/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling appointment");
            return "redirect:/patient/appointments";
        }
    }

    @GetMapping("/treatment-history")
    public String viewTreatmentHistory(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                model.addAttribute("error", "Patient not found");
                return "patient/treatment-history";
            }

            Patient patient = patientOpt.get();
            List<TreatmentHistory> treatmentHistory = treatmentHistoryService.findByPatient(patient);

            // Calculate statistics
            long totalTreatments = treatmentHistory.size();
            long treatmentsThisYear = treatmentHistory.stream()
                    .filter(t -> t.getTreatmentDate().getYear() == LocalDateTime.now().getYear())
                    .count();
            long treatmentsWithFollowUp = treatmentHistory.stream()
                    .filter(t -> t.getFollowUpDate() != null && t.getFollowUpDate().isAfter(LocalDateTime.now()))
                    .count();

            model.addAttribute("treatmentHistory", treatmentHistory);
            model.addAttribute("totalTreatments", totalTreatments);
            model.addAttribute("treatmentsThisYear", treatmentsThisYear);
            model.addAttribute("treatmentsWithFollowUp", treatmentsWithFollowUp);
            model.addAttribute("title", "Treatment History");

            return "patient/treatment-history";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading treatment history");
            return "patient/treatment-history";
        }
    }

    @GetMapping("/treatment-history/{id}")
    public String viewTreatmentDetails(@PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());
            Optional<TreatmentHistory> treatmentOpt = treatmentHistoryService.findById(id);

            if (!patientOpt.isPresent() || !treatmentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Treatment record not found");
                return "redirect:/patient/treatment-history";
            }

            TreatmentHistory treatment = treatmentOpt.get();
            // Verify the treatment belongs to the current patient
            if (!treatment.getPatient().getId().equals(patientOpt.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/patient/treatment-history";
            }

            model.addAttribute("treatment", treatment);
            model.addAttribute("title", "Treatment Details");
            return "patient/treatment-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading treatment details");
            return "redirect:/patient/treatment-history";
        }
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                model.addAttribute("error", "Patient not found");
                return "patient/profile";
            }

            Patient patient = patientOpt.get();

            // Calculate patient's statistics for profile summary
            List<Appointment> allAppointments = appointmentService.findByPatient(patient);
            List<TreatmentHistory> treatmentHistories = treatmentHistoryService.findByPatient(patient);

            long totalAppointments = allAppointments.size();
            long completedAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long totalTreatments = treatmentHistories.size();
            long upcomingAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED &&
                            a.getAppointmentDate().isAfter(LocalDateTime.now()))
                    .count();

            model.addAttribute("patient", patient);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("totalTreatments", totalTreatments);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("title", "My Profile");

            return "patient/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading profile");
            return "patient/profile";
        }
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                model.addAttribute("error", "Patient not found");
                return "patient/profile-edit";
            }

            Patient patient = patientOpt.get();
            model.addAttribute("patient", patient);
            model.addAttribute("title", "Edit Profile");

            return "patient/profile-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading profile for editing");
            return "patient/profile-edit";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam String dateOfBirth,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/patient/profile";
            }

            Patient patient = patientOpt.get();

            // Update patient information
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            patient.setPhoneNumber(phoneNumber);
            patient.setAddress(address);
            patient.setDateOfBirth(dateOfBirth);

            // Save updated patient
            patientService.save(patient);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/patient/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/patient/profile/edit";
        }
    }

    public static class AppointmentForm {
        private Doctor doctor;
        private String appointmentDate;
        private String appointmentTime;
        private String reason;
        private String notes;

        // Constructors
        public AppointmentForm() {
            this.doctor = new Doctor();
        }

        // Getters and Setters
        public Doctor getDoctor() {
            return doctor;
        }

        public void setDoctor(Doctor doctor) {
            this.doctor = doctor;
        }

        public String getAppointmentDate() {
            return appointmentDate;
        }

        public void setAppointmentDate(String appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public String getAppointmentTime() {
            return appointmentTime;
        }

        public void setAppointmentTime(String appointmentTime) {
            this.appointmentTime = appointmentTime;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    // Mental Health Support Endpoints
    @GetMapping("/mental-health")
    public String viewMentalHealthSupport(@RequestParam(required = false) String specialization,
            @RequestParam(required = false) String search,
            Model model) {
        List<Counselor> counselors;

        // Handle combined search and specialization filtering
        boolean hasSpecialization = specialization != null && !specialization.isEmpty();
        boolean hasSearch = search != null && !search.isEmpty();

        if (hasSpecialization && hasSearch) {
            // Both specialization and search term provided
            counselors = counselorService.findBySpecializationAndSearch(specialization, search);
        } else if (hasSpecialization) {
            // Only specialization filter
            counselors = counselorService.findBySpecialization(specialization);
        } else if (hasSearch) {
            // Only search term
            counselors = counselorService.searchCounselors(search);
        } else {
            // No filters - show all counselors (not just mental health ones)
            counselors = counselorService.findAll();
        }

        // Get all unique specializations for filter dropdown
        List<String> specializations = counselorService.getAllSpecializations();

        model.addAttribute("counselors", counselors);
        model.addAttribute("specializations", specializations);
        model.addAttribute("selectedSpecialization", specialization);
        model.addAttribute("searchQuery", search);
        model.addAttribute("title", "Mental Health Support");
        return "patient/mental-health";
    }

    @GetMapping("/counselors/{id}")
    public String viewCounselorProfile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Counselor> counselorOpt = counselorService.findById(id);

        if (!counselorOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Counselor not found");
            return "redirect:/patient/mental-health";
        }

        Counselor counselor = counselorOpt.get();
        model.addAttribute("counselor", counselor);
        model.addAttribute("title", counselor.getFirstName() + " " + counselor.getLastName());
        return "patient/counselor-profile";
    }

    // AI Chat Endpoints for Mental Health Support
    @PostMapping("/chat/mental-health")
    @ResponseBody
    public ResponseEntity<ChatResponse> handleMentalHealthChat(@RequestBody ChatRequest request) {
        try {
            String response = chatService.getMentalHealthResponse(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(true, response));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse(false,
                    "I'm currently unavailable. Please speak with one of our professional counselors who are always here to help."));
        }
    }

    @GetMapping("/chat/test")
    @ResponseBody
    public ResponseEntity<ChatResponse> testChatConnection() {
        try {
            boolean isConnected = chatService.testConnection();
            String message = isConnected ? "AI Chat is working properly!"
                    : "AI Chat is currently unavailable. Please try again later.";
            return ResponseEntity.ok(new ChatResponse(isConnected, message));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse(false,
                    "Unable to test connection. Please try again later."));
        }
    }

    // Simple DTO classes for chat endpoints
    public static class ChatRequest {
        private String message;

        public ChatRequest() {
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {
        private boolean success;
        private String message;

        public ChatResponse() {
        }

        public ChatResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Waitlist Management Endpoints
    @GetMapping("/waitlist")
    public String viewWaitlist(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

        if (patientOpt.isPresent()) {
            List<Waitlist> waitlists = waitlistService.findByPatient(patientOpt.get());
            model.addAttribute("waitlists", waitlists);
        }

        return "patient/waitlist-simple";
    }

    @PostMapping("/waitlist/add")
    public String addToWaitlist(@RequestParam Long doctorId,
            @RequestParam(required = false) String preferredDate,
            @RequestParam String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());
            Optional<Doctor> doctorOpt = doctorService.findById(doctorId);

            if (!patientOpt.isPresent() || !doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient or Doctor not found");
                return "redirect:/patient/doctors";
            }

            Patient patient = patientOpt.get();
            Doctor doctor = doctorOpt.get();

            if (waitlistService.isPatientInWaitlist(patient, doctor)) {
                redirectAttributes.addFlashAttribute("error", "You are already in the waitlist for this doctor");
                return "redirect:/patient/doctors";
            }

            LocalDateTime preferredDateTime = null;
            if (preferredDate != null && !preferredDate.trim().isEmpty()) {
                preferredDateTime = LocalDate.parse(preferredDate).atTime(9, 0);
            }
            waitlistService.addToWaitlist(patient, doctor, preferredDateTime, reason);

            redirectAttributes.addFlashAttribute("success",
                    "Successfully added to waitlist for Dr. " + doctor.getFirstName() + " " + doctor.getLastName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add to waitlist: " + e.getMessage());
        }

        return "redirect:/patient/waitlist";
    }

    @PostMapping("/waitlist/{id}/cancel")
    public String cancelWaitlist(@PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Waitlist> waitlistOpt = waitlistService.findById(id);
            if (waitlistOpt.isPresent()) {
                Waitlist waitlist = waitlistOpt.get();

                // Verify the waitlist belongs to the current patient
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (!waitlist.getPatient().getId().equals(userDetails.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                    return "redirect:/patient/waitlist";
                }

                waitlistService.removeFromWaitlist(id);
                redirectAttributes.addFlashAttribute("success", "Waitlist entry cancelled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Waitlist entry not found");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel waitlist entry: " + e.getMessage());
        }

        return "redirect:/patient/waitlist";
    }

    // Counseling Session Booking Endpoints
    @PostMapping("/book-session")
    @ResponseBody
    public ResponseEntity<?> bookCounselingSession(@RequestBody BookingRequest request, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long patientId = userDetails.getId();

            CounselingSession session = counselingSessionService.bookSession(
                    patientId,
                    request.getCounselorId(),
                    request.getSessionDateTime(),
                    request.getSessionType(),
                    request.getSessionMode(),
                    request.getReason());

            return ResponseEntity.ok(new BookingResponse(true, "Session booked successfully!", session.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new BookingResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/sessions")
    public String viewSessions(Model model, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Patient> patientOpt = patientService.findById(userDetails.getId());

            if (patientOpt.isPresent()) {
                List<CounselingSession> sessions = counselingSessionService.getPatientSessions(patientOpt.get());
                model.addAttribute("sessions", sessions);
            }

            model.addAttribute("title", "My Counseling Sessions");
            return "patient/sessions";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load sessions: " + e.getMessage());
            return "patient/sessions";
        }
    }

    @PostMapping("/sessions/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelSession(@PathVariable Long id, @RequestBody CancelRequest request,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Verify session belongs to current patient
            Optional<CounselingSession> sessionOpt = counselingSessionService.findById(id);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new BookingResponse(false, "Session not found", null));
            }

            CounselingSession session = sessionOpt.get();
            if (!session.getPatient().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest().body(new BookingResponse(false, "Unauthorized access", null));
            }

            counselingSessionService.cancelSession(id, request.getReason());
            return ResponseEntity.ok(new BookingResponse(true, "Session cancelled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new BookingResponse(false, e.getMessage(), null));
        }
    }

    // DTO Classes for Booking
    public static class BookingRequest {
        private Long counselorId;
        private LocalDateTime sessionDateTime;
        private CounselingSession.SessionType sessionType;
        private CounselingSession.SessionMode sessionMode;
        private String reason;

        // Getters and setters
        public Long getCounselorId() {
            return counselorId;
        }

        public void setCounselorId(Long counselorId) {
            this.counselorId = counselorId;
        }

        public LocalDateTime getSessionDateTime() {
            return sessionDateTime;
        }

        public void setSessionDateTime(LocalDateTime sessionDateTime) {
            this.sessionDateTime = sessionDateTime;
        }

        public CounselingSession.SessionType getSessionType() {
            return sessionType;
        }

        public void setSessionType(CounselingSession.SessionType sessionType) {
            this.sessionType = sessionType;
        }

        public CounselingSession.SessionMode getSessionMode() {
            return sessionMode;
        }

        public void setSessionMode(CounselingSession.SessionMode sessionMode) {
            this.sessionMode = sessionMode;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class BookingResponse {
        private boolean success;
        private String message;
        private Long sessionId;

        public BookingResponse(boolean success, String message, Long sessionId) {
            this.success = success;
            this.message = message;
            this.sessionId = sessionId;
        }

        // Getters and setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }
    }

    public static class CancelRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}