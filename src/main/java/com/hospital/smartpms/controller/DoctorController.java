package com.hospital.smartpms.controller;

import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.TreatmentHistory;
import com.hospital.smartpms.security.CustomUserDetails;
import com.hospital.smartpms.service.AppointmentService;
import com.hospital.smartpms.service.DoctorService;
import com.hospital.smartpms.service.PatientService;
import com.hospital.smartpms.service.TreatmentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TreatmentHistoryService treatmentHistoryService;

    @Autowired
    private PatientService patientService;

    @GetMapping("/dashboard")
    public String doctorDashboard(Model model) {
        model.addAttribute("title", "Doctor Dashboard");
        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String viewAllAppointments(Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        // Appointments view is disabled for doctors
        redirectAttributes.addFlashAttribute("info", "Appointments view is not available");
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/appointments/today")
    public String viewTodaysAppointments(Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        // Today's appointments view is disabled for doctors
        redirectAttributes.addFlashAttribute("info", "Today's appointments view is not available");
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointmentDetails(@PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Appointment details view is disabled for doctors
        redirectAttributes.addFlashAttribute("info", "Appointment details view is not available");
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/appointment/{id}/complete")
    public String markAppointmentComplete(@PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        // Appointment actions are disabled for doctors
        redirectAttributes.addFlashAttribute("info", "Appointment actions are not available");
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/appointment/{id}/no-show")
    public String markAppointmentNoShow(@PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        // Appointment actions are disabled for doctors
        redirectAttributes.addFlashAttribute("info", "Appointment actions are not available");
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/patients")
    public String viewPatientRecords(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                model.addAttribute("error", "Doctor not found");
                return "doctor/patients";
            }

            Doctor doctor = doctorOpt.get();
            List<TreatmentHistory> treatmentHistories = treatmentHistoryService.findByDoctor(doctor);

            // Calculate statistics
            long totalTreatments = treatmentHistories.size();
            long uniquePatients = treatmentHistories.stream()
                    .map(th -> th.getPatient().getId())
                    .distinct()
                    .count();

            model.addAttribute("treatmentHistories", treatmentHistories);
            model.addAttribute("totalTreatments", totalTreatments);
            model.addAttribute("uniquePatients", uniquePatients);
            model.addAttribute("doctor", doctor);
            model.addAttribute("title", "Patient Records");

            return "doctor/patients";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading patient records");
            return "doctor/patients";
        }
    }

    @GetMapping("/treatment/add")
    public String showAddTreatmentForm(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                model.addAttribute("error", "Doctor not found");
                return "doctor/add-treatment";
            }

            Doctor doctor = doctorOpt.get();
            List<Patient> patients = patientService.findAll();

            model.addAttribute("patients", patients);
            model.addAttribute("doctor", doctor);
            model.addAttribute("title", "Add Treatment");

            return "doctor/add-treatment";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading treatment form");
            return "doctor/add-treatment";
        }
    }

    @PostMapping("/treatment/add")
    public String addTreatment(
            @RequestParam Long patientId,
            @RequestParam String diagnosis,
            @RequestParam String treatment,
            @RequestParam String prescription,
            @RequestParam String notes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime followUpDate,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());
            Optional<Patient> patientOpt = patientService.findById(patientId);

            if (!doctorOpt.isPresent() || !patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor or Patient not found");
                return "redirect:/doctor/treatment/add";
            }

            Doctor doctor = doctorOpt.get();
            Patient patient = patientOpt.get();

            // Create treatment record without appointment
            treatmentHistoryService.createTreatmentRecord(
                    patient, doctor, null, diagnosis, treatment, prescription, notes, followUpDate);

            redirectAttributes.addFlashAttribute("success", "Treatment record added successfully");
            return "redirect:/doctor/patients";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding treatment record: " + e.getMessage());
            return "redirect:/doctor/treatment/add";
        }
    }

    @GetMapping("/schedule")
    public String viewSchedule(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                model.addAttribute("error", "Doctor not found");
                return "doctor/schedule";
            }

            Doctor doctor = doctorOpt.get();
            List<Appointment> allAppointments = appointmentService.findByDoctor(doctor);
            List<Appointment> todaysAppointments = appointmentService.findTodaysAppointmentsByDoctor(doctor);
            List<Appointment> upcomingAppointments = appointmentService.findUpcomingAppointmentsByDoctor(doctor);

            // Calculate schedule statistics
            long totalAppointments = allAppointments.size();
            long todaysCount = todaysAppointments.size();
            long upcomingCount = upcomingAppointments.size();
            long completedAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();

            // Debug logging
            System.out.println("🔍 SCHEDULE DEBUG - Doctor: " + doctor.getFirstName() + " " + doctor.getLastName());
            System.out.println("🔍 Total appointments: " + totalAppointments);
            System.out.println("🔍 Today's appointments: " + todaysCount);
            System.out.println("🔍 Upcoming appointments: " + upcomingCount);
            System.out.println("🔍 Completed appointments: " + completedAppointments);

            // Debug today's appointments details
            System.out.println("🔍 Today's appointments details:");
            for (Appointment app : todaysAppointments) {
                System.out.println("  - " + app.getAppointmentDate() + " | " + app.getPatient().getFirstName() + " "
                        + app.getPatient().getLastName() + " | " + app.getStatus());
            }

            model.addAttribute("doctor", doctor);
            model.addAttribute("allAppointments", allAppointments);
            model.addAttribute("todaysAppointments", todaysAppointments);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("todaysCount", todaysCount);
            model.addAttribute("upcomingCount", upcomingCount);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("title", "My Schedule");

            return "doctor/schedule";
        } catch (Exception e) {
            System.out.println("❌ SCHEDULE ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading schedule");
            return "doctor/schedule";
        }
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                model.addAttribute("error", "Doctor not found");
                return "doctor/profile";
            }

            Doctor doctor = doctorOpt.get();

            // Calculate doctor's statistics for profile summary
            List<Appointment> allAppointments = appointmentService.findByDoctor(doctor);
            List<TreatmentHistory> treatmentHistories = treatmentHistoryService.findByDoctor(doctor);

            long totalAppointments = allAppointments.size();
            long completedAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long totalTreatments = treatmentHistories.size();
            long uniquePatients = treatmentHistories.stream()
                    .map(th -> th.getPatient().getId())
                    .distinct()
                    .count();

            model.addAttribute("doctor", doctor);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("totalTreatments", totalTreatments);
            model.addAttribute("uniquePatients", uniquePatients);
            model.addAttribute("title", "My Profile");

            return "doctor/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading profile");
            return "doctor/profile";
        }
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                model.addAttribute("error", "Doctor not found");
                return "doctor/profile-edit";
            }

            Doctor doctor = doctorOpt.get();
            model.addAttribute("doctor", doctor);
            model.addAttribute("title", "Edit Profile");

            return "doctor/profile-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading profile for editing");
            return "doctor/profile-edit";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String specialization,
            @RequestParam String experience,
            @RequestParam String licenseNumber,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Optional<Doctor> doctorOpt = doctorService.findById(userDetails.getId());

            if (!doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/doctor/profile";
            }

            Doctor doctor = doctorOpt.get();

            // Update doctor information
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setEmail(email);
            doctor.setPhoneNumber(phoneNumber);
            doctor.setSpecialization(specialization);
            doctor.setExperience(experience);
            doctor.setLicenseNumber(licenseNumber);

            // Save updated doctor
            doctorService.save(doctor);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/doctor/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/doctor/profile/edit";
        }
    }
}