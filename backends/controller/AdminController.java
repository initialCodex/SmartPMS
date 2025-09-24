package com.hospital.smartpms.controller;

import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Attendance;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Patient.PatientStatus;
import com.hospital.smartpms.entity.Waitlist;
import com.hospital.smartpms.service.AppointmentService;
import com.hospital.smartpms.service.AttendanceService;
import com.hospital.smartpms.service.DoctorService;
import com.hospital.smartpms.service.PatientService;
import com.hospital.smartpms.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Get quick statistics for dashboard
        List<Appointment> allAppointments = appointmentService.findAll();
        List<Doctor> allDoctors = doctorService.findAll();
        List<Patient> allPatients = patientService.findAll();
        List<Appointment> todaysAppointments = appointmentService.findTodaysAppointments();

        // Calculate statistics
        long totalAppointments = allAppointments.size();
        long totalDoctors = allDoctors.size();
        long totalPatients = allPatients.size();
        long todaysAppointmentCount = todaysAppointments.size();

        // Calculate patient counts by status
        long activePatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.ACTIVE)
                .count();

        long vipPatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.VIP)
                .count();

        long inactivePatients = allPatients.stream()
                .filter(p -> p.getStatus() == Patient.PatientStatus.INACTIVE)
                .count();

        long pendingAppointments = allAppointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .count();

        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("activePatients", activePatients);
        model.addAttribute("vipPatients", vipPatients);
        model.addAttribute("inactivePatients", inactivePatients);
        model.addAttribute("todaysAppointments", todaysAppointmentCount);
        model.addAttribute("pendingAppointments", pendingAppointments);
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/appointments")
    public String viewAllAppointments(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String patientStatus) {
        try {
            List<Appointment> appointments;
            Appointment.AppointmentStatus statusEnum = null;
            PatientStatus patientStatusEnum = null;

            // Parse status parameter if provided
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                try {
                    statusEnum = Appointment.AppointmentStatus.valueOf(status.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore
                }
            }

            // Parse patient status parameter if provided
            if (patientStatus != null && !patientStatus.trim().isEmpty() && !patientStatus.equals("ALL")) {
                try {
                    patientStatusEnum = PatientStatus.valueOf(patientStatus.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid patient status, ignore
                }
            }

            // Perform search based on criteria
            if ((search != null && !search.trim().isEmpty()) || statusEnum != null || patientStatusEnum != null) {
                appointments = appointmentService.searchAppointments(
                        (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                        statusEnum,
                        patientStatusEnum);
            } else {
                appointments = appointmentService.findAll();
            }

            // Calculate statistics based on filtered results
            long totalAppointments = appointments.size();
            long todaysAppointments = appointmentService.findTodaysAppointments().size();
            long completedAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long scheduledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .count();
            long cancelledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();
            long noShowAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.NO_SHOW)
                    .count();

            model.addAttribute("appointments", appointments);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("todaysAppointments", todaysAppointments);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("scheduledAppointments", scheduledAppointments);
            model.addAttribute("cancelledAppointments", cancelledAppointments);
            model.addAttribute("noShowAppointments", noShowAppointments);
            model.addAttribute("searchTerm", search);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedPatientStatus", patientStatus);
            model.addAttribute("title", "All Appointments");

            return "admin/appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading appointments: " + e.getMessage());
            return "admin/appointments";
        }
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointmentDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);

            if (!appointmentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/admin/appointments";
            }

            model.addAttribute("appointment", appointmentOpt.get());
            model.addAttribute("title", "Appointment Details");
            return "admin/appointment-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading appointment details");
            return "redirect:/admin/appointments";
        }
    }

    @PostMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);

            if (!appointmentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/admin/appointments";
            }

            Appointment appointment = appointmentOpt.get();
            // Check if appointment can be cancelled (only scheduled appointments)
            if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
                redirectAttributes.addFlashAttribute("error", "Only scheduled appointments can be cancelled");
                return "redirect:/admin/appointments";
            }

            appointmentService.updateStatus(id, Appointment.AppointmentStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");

            return "redirect:/admin/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling appointment");
            return "redirect:/admin/appointments";
        }
    }

    @GetMapping("/doctors")
    public String viewAllDoctors(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization) {
        try {
            List<Doctor> doctors;

            if (search != null && !search.trim().isEmpty()) {
                doctors = doctorService.searchDoctors(search.trim());
            } else if (specialization != null && !specialization.trim().isEmpty()) {
                doctors = doctorService.findBySpecialization(specialization.trim());
            } else {
                doctors = doctorService.findAll();
            }

            // Get unique specializations for filter dropdown
            Set<String> specializations = doctorService.findAll().stream()
                    .map(Doctor::getSpecialization)
                    .collect(Collectors.toSet());

            model.addAttribute("doctors", doctors);
            model.addAttribute("specializations", specializations);
            model.addAttribute("searchTerm", search);
            model.addAttribute("selectedSpecialization", specialization);
            model.addAttribute("title", "Manage Doctors");
            return "admin/doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading doctors: " + e.getMessage());
            return "admin/doctors";
        }
    }

    @GetMapping("/doctors/add")
    public String showAddDoctorForm(Model model) {
        model.addAttribute("title", "Add New Doctor");
        return "admin/add-doctor";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@ModelAttribute Doctor doctor,
            RedirectAttributes redirectAttributes) {
        try {
            if (doctor.getId() != null) {
                // Editing existing doctor
                Optional<Doctor> existingDoctorOpt = doctorService.findById(doctor.getId());
                if (!existingDoctorOpt.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Doctor not found");
                    return "redirect:/admin/doctors";
                }

                Doctor existingDoctor = existingDoctorOpt.get();

                // Update fields
                existingDoctor.setFirstName(doctor.getFirstName());
                existingDoctor.setLastName(doctor.getLastName());
                existingDoctor.setEmail(doctor.getEmail());
                existingDoctor.setPhoneNumber(doctor.getPhoneNumber());
                existingDoctor.setSpecialization(doctor.getSpecialization());
                existingDoctor.setExperience(doctor.getExperience());
                existingDoctor.setLicenseNumber(doctor.getLicenseNumber());

                // Only update password if provided
                if (doctor.getPassword() != null && !doctor.getPassword().trim().isEmpty()) {
                    existingDoctor.setPassword(doctor.getPassword());
                    doctorService.save(existingDoctor); // This will encode the new password
                } else {
                    doctorService.saveWithoutPasswordEncoding(existingDoctor); // Keep existing password
                }

                redirectAttributes.addFlashAttribute("success", "Doctor updated successfully");
            } else {
                // Adding new doctor
                if (doctor.getPassword() == null || doctor.getPassword().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Password is required for new doctors");
                    return "redirect:/admin/doctors";
                }

                // Check if email already exists
                if (doctorService.findByEmail(doctor.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email already exists");
                    return "redirect:/admin/doctors";
                }

                doctorService.save(doctor); // This will encode the password
                redirectAttributes.addFlashAttribute("success", "Doctor added successfully");
            }

            return "redirect:/admin/doctors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving doctor: " + e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @GetMapping("/doctors/{id}/edit")
    public String showEditDoctorForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Doctor> doctorOpt = doctorService.findById(id);
            if (!doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            model.addAttribute("doctor", doctorOpt.get());
            model.addAttribute("title", "Edit Doctor");
            return "admin/edit-doctor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading doctor details");
            return "redirect:/admin/doctors";
        }
    }

    @GetMapping("/doctors/{id}/edit-json")
    @ResponseBody
    public ResponseEntity<Doctor> getDoctorForEdit(@PathVariable Long id) {
        try {
            Optional<Doctor> doctorOpt = doctorService.findById(id);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                // Don't send password in response
                doctor.setPassword(null);
                return ResponseEntity.ok(doctor);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctors/{id}/view")
    public String viewDoctorDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Doctor> doctorOpt = doctorService.findById(id);
            if (!doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            Doctor doctor = doctorOpt.get();
            List<Appointment> doctorAppointments = appointmentService.findByDoctor(doctor);

            model.addAttribute("doctor", doctor);
            model.addAttribute("appointments", doctorAppointments);
            model.addAttribute("title", "Doctor Details");
            return "admin/doctor-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading doctor details");
            return "redirect:/admin/doctors";
        }
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Doctor> doctorOpt = doctorService.findById(id);
            if (!doctorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            Doctor doctor = doctorOpt.get();

            // Check if doctor has any scheduled appointments
            List<Appointment> scheduledAppointments = appointmentService.findByDoctor(doctor)
                    .stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .collect(Collectors.toList());

            if (!scheduledAppointments.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete doctor with scheduled appointments. Please cancel or reassign appointments first.");
                return "redirect:/admin/doctors";
            }

            doctorService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Doctor deleted successfully");
            return "redirect:/admin/doctors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting doctor: " + e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @GetMapping("/patients")
    public String viewAllPatients(Model model,
            @RequestParam(required = false) String search) {
        try {
            List<Patient> patients;

            if (search != null && !search.trim().isEmpty()) {
                patients = patientService.searchPatients(search.trim());
            } else {
                patients = patientService.findAll();
            }

            model.addAttribute("patients", patients);
            model.addAttribute("searchTerm", search);
            model.addAttribute("title", "All Patients");
            return "admin/patients";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading patients: " + e.getMessage());
            return "admin/patients";
        }
    }

    @PostMapping("/patients/save")
    public String savePatient(@ModelAttribute Patient patient,
            @RequestParam(value = "status", required = false) String statusParam,
            RedirectAttributes redirectAttributes) {
        try {
            // Convert status string to enum if provided
            if (statusParam != null && !statusParam.isEmpty()) {
                try {
                    patient.setStatus(Patient.PatientStatus.valueOf(statusParam));
                } catch (IllegalArgumentException e) {
                    patient.setStatus(Patient.PatientStatus.ACTIVE); // Default fallback
                }
            }

            if (patient.getId() != null) {
                // Editing existing patient
                Optional<Patient> existingPatientOpt = patientService.findById(patient.getId());
                if (!existingPatientOpt.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Patient not found");
                    return "redirect:/admin/patients";
                }

                Patient existingPatient = existingPatientOpt.get();

                // Update fields
                existingPatient.setFirstName(patient.getFirstName());
                existingPatient.setLastName(patient.getLastName());
                existingPatient.setEmail(patient.getEmail());
                existingPatient.setPhoneNumber(patient.getPhoneNumber());
                existingPatient.setAddress(patient.getAddress());
                existingPatient.setDateOfBirth(patient.getDateOfBirth());
                existingPatient.setStatus(patient.getStatus()); // Add status update

                // Don't update password if it's empty
                if (patient.getPassword() != null && !patient.getPassword().trim().isEmpty()) {
                    existingPatient.setPassword(patient.getPassword());
                    patientService.save(existingPatient); // This will encode the new password
                } else {
                    patientService.update(existingPatient); // This won't re-encode existing password
                }
                redirectAttributes.addFlashAttribute("success", "Patient updated successfully");
            } else {
                // Creating new patient
                // Check if email already exists
                if (patientService.findByEmail(patient.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email already exists");
                    return "redirect:/admin/patients";
                }

                patientService.save(patient);
                redirectAttributes.addFlashAttribute("success", "Patient created successfully");
            }
            return "redirect:/admin/patients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving patient: " + e.getMessage());
            return "redirect:/admin/patients";
        }
    }

    @GetMapping("/patients/add")
    public String addPatient(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("title", "Add New Patient");
        return "admin/patient-add";
    }

    @GetMapping("/patients/{id}/edit")
    public String editPatient(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> patientOpt = patientService.findById(id);
            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/admin/patients";
            }

            model.addAttribute("patient", patientOpt.get());
            model.addAttribute("title", "Edit Patient");
            return "admin/patient-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading patient: " + e.getMessage());
            return "redirect:/admin/patients";
        }
    }

    @GetMapping("/patients/{id}/view")
    public String viewPatient(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> patientOpt = patientService.findById(id);
            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/admin/patients";
            }

            Patient patient = patientOpt.get();
            List<Appointment> patientAppointments = appointmentService.findByPatient(patient);

            model.addAttribute("patient", patient);
            model.addAttribute("appointments", patientAppointments);
            model.addAttribute("title", "Patient Details");
            return "admin/patient-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading patient details: " + e.getMessage());
            return "redirect:/admin/patients";
        }
    }

    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> patientOpt = patientService.findById(id);
            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/admin/patients";
            }

            Patient patient = patientOpt.get();

            // Debug: Log patient information
            System.out.println("Attempting to delete patient: " + patient.getFirstName() + " " + patient.getLastName()
                    + " (ID: " + id + ")");

            // Check if patient has any scheduled appointments
            List<Appointment> allAppointments = appointmentService.findByPatient(patient);
            System.out.println("Found " + allAppointments.size() + " total appointments for patient");

            List<Appointment> scheduledAppointments = allAppointments
                    .stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .collect(Collectors.toList());

            System.out.println("Found " + scheduledAppointments.size() + " SCHEDULED appointments for patient");

            // For debugging: show all appointment statuses
            for (Appointment apt : allAppointments) {
                System.out.println("  - Appointment ID " + apt.getId() + ": " + apt.getStatus() +
                        " on " + apt.getAppointmentDate() + " (" + apt.getReason() + ")");
            }

            if (!scheduledAppointments.isEmpty()) {
                System.out.println(
                        "Blocking deletion - patient has " + scheduledAppointments.size() + " scheduled appointments");

                // Create detailed error message with appointment information
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("Cannot delete patient with ").append(scheduledAppointments.size())
                        .append(" scheduled appointment").append(scheduledAppointments.size() > 1 ? "s" : "")
                        .append(". Scheduled appointments: ");

                for (int i = 0; i < scheduledAppointments.size(); i++) {
                    Appointment apt = scheduledAppointments.get(i);
                    if (i > 0)
                        errorMsg.append(", ");
                    errorMsg.append(apt.getAppointmentDate().toLocalDate())
                            .append(" at ").append(apt.getAppointmentDate().toLocalTime())
                            .append(" (").append(apt.getReason()).append(")");
                }
                errorMsg.append(". Please cancel these appointments first.");

                redirectAttributes.addFlashAttribute("error", errorMsg.toString());
                return "redirect:/admin/patients";
            }

            System.out.println("No scheduled appointments found - proceeding with deletion");
            patientService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Patient deleted successfully");
            return "redirect:/admin/patients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting patient: " + e.getMessage());
            return "redirect:/admin/patients";
        }
    }

    @GetMapping("/debug/patient-appointments")
    @ResponseBody
    public String debugPatientAppointments() {
        StringBuilder debug = new StringBuilder();
        debug.append("<h2>PATIENT APPOINTMENTS DEBUG</h2><pre>");

        List<Patient> allPatients = patientService.findAll();
        debug.append("Total patients: ").append(allPatients.size()).append("\n\n");

        for (Patient patient : allPatients) {
            debug.append("Patient ID: ").append(patient.getId())
                    .append(", Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName())
                    .append(", Status: ").append(patient.getStatus()).append("\n");

            List<Appointment> appointments = appointmentService.findByPatient(patient);
            debug.append("  Total appointments: ").append(appointments.size()).append("\n");

            long scheduledCount = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .count();
            debug.append("  SCHEDULED appointments: ").append(scheduledCount).append("\n");

            if (scheduledCount > 0) {
                debug.append("  ❌ CANNOT DELETE - Has scheduled appointments\n");
            } else {
                debug.append("  ✅ CAN DELETE - No scheduled appointments\n");
            }

            for (Appointment appointment : appointments) {
                debug.append("    - Appointment ID: ").append(appointment.getId())
                        .append(", Status: ").append(appointment.getStatus())
                        .append(", Date: ").append(appointment.getAppointmentDate()).append("\n");
            }
            debug.append("\n");
        }

        debug.append("</pre>");
        return debug.toString();
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        try {
            // Get statistics for reports
            List<Appointment> allAppointments = appointmentService.findAll();
            List<Doctor> allDoctors = doctorService.findAll();
            List<Patient> allPatients = patientService.findAll();

            // Calculate report statistics
            long totalAppointments = allAppointments.size();
            long completedAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long scheduledAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .count();
            long cancelledAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();

            // Calculate monthly appointment trends for the last 6 months
            LocalDate now = LocalDate.now();
            Map<String, Long> monthlyTrends = new LinkedHashMap<>();

            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("MMM"));

                long monthlyCount = allAppointments.stream()
                        .filter(a -> !a.getAppointmentDate().toLocalDate().isBefore(monthStart) &&
                                !a.getAppointmentDate().toLocalDate().isAfter(monthEnd))
                        .count();

                monthlyTrends.put(monthLabel, monthlyCount);
            }

            // Recent appointments for reports
            List<Appointment> recentAppointments = allAppointments.stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("completedAppointments", completedAppointments);
            model.addAttribute("scheduledAppointments", scheduledAppointments);
            model.addAttribute("cancelledAppointments", cancelledAppointments);
            model.addAttribute("totalDoctors", allDoctors.size());
            model.addAttribute("totalPatients", allPatients.size());
            model.addAttribute("recentAppointments", recentAppointments);
            model.addAttribute("monthlyTrends", monthlyTrends);
            model.addAttribute("title", "Reports & Analytics");

            return "admin/reports";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reports");
            return "admin/reports";
        }
    }

    @GetMapping("/settings")
    public String viewSettings(Model model) {
        try {
            // Add any settings data if needed
            model.addAttribute("title", "System Settings");
            return "admin/settings";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading settings");
            return "admin/settings";
        }
    }

    // Waitlist Management Endpoints
    @GetMapping("/waitlist")
    public String viewWaitlist(Model model) {
        try {
            List<Waitlist> allWaitlists = waitlistService.findAll();
            List<Waitlist> waitingList = waitlistService.findByStatus(Waitlist.WaitlistStatus.WAITING);
            List<Waitlist> notifiedList = waitlistService.findByStatus(Waitlist.WaitlistStatus.NOTIFIED);
            List<Waitlist> scheduledList = waitlistService.findByStatus(Waitlist.WaitlistStatus.SCHEDULED);

            model.addAttribute("allWaitlists", allWaitlists);
            model.addAttribute("waitingList", waitingList);
            model.addAttribute("notifiedList", notifiedList);
            model.addAttribute("scheduledCount", scheduledList.size());
            model.addAttribute("title", "Waitlist Management");

            return "admin/waitlist";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading waitlist data");
            return "admin/waitlist";
        }
    }

    @PostMapping("/waitlist/{id}/notify")
    public String notifyWaitlistPatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            waitlistService.notifyPatient(id);
            redirectAttributes.addFlashAttribute("success", "Patient notified successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to notify patient: " + e.getMessage());
        }
        return "redirect:/admin/waitlist";
    }

    @PostMapping("/waitlist/{id}/schedule")
    public String scheduleWaitlistPatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            waitlistService.markAsScheduled(id);
            redirectAttributes.addFlashAttribute("success", "Waitlist entry marked as scheduled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update waitlist entry: " + e.getMessage());
        }
        return "redirect:/admin/waitlist";
    }

    @PostMapping("/waitlist/{id}/priority")
    public String updateWaitlistPriority(@PathVariable Long id,
            @RequestParam Integer priority,
            RedirectAttributes redirectAttributes) {
        try {
            waitlistService.updatePriority(id, priority);
            redirectAttributes.addFlashAttribute("success", "Priority updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update priority: " + e.getMessage());
        }
        return "redirect:/admin/waitlist";
    }

    @PostMapping("/waitlist/{id}/cancel")
    public String cancelWaitlistEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            waitlistService.removeFromWaitlist(id);
            redirectAttributes.addFlashAttribute("success", "Waitlist entry cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel waitlist entry: " + e.getMessage());
        }
        return "redirect:/admin/waitlist";
    }

    // Attendance Management Endpoints
    @GetMapping("/attendance")
    public String viewAttendance(Model model) {
        try {
            List<Attendance> todaysAttendances = attendanceService.getTodaysAttendances();
            List<Attendance> pendingAttendances = attendanceService.getTodaysPendingAttendances();
            List<Attendance> overdueCheckIns = attendanceService.getOverdueCheckIns();
            List<Attendance> recentCheckIns = attendanceService.getRecentCheckIns(60); // Last hour

            // Get statistics
            AttendanceService.AttendanceStatistics stats = attendanceService.getAttendanceStatistics(
                    java.time.LocalDate.now(), java.time.LocalDate.now());

            model.addAttribute("todaysAttendances", todaysAttendances);
            model.addAttribute("pendingAttendances", pendingAttendances);
            model.addAttribute("overdueCheckIns", overdueCheckIns);
            model.addAttribute("recentCheckIns", recentCheckIns);
            model.addAttribute("stats", stats);
            model.addAttribute("title", "Attendance Management");

            return "admin/attendance";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading attendance data");
            return "admin/attendance";
        }
    }

    @GetMapping("/attendance/today")
    public String viewTodaysAttendance(Model model) {
        try {
            List<Attendance> todaysAttendances = attendanceService.getTodaysAttendances();
            List<Attendance> pendingCheckIns = attendanceService
                    .getTodaysAttendancesByStatus(Attendance.AttendanceStatus.SCHEDULED);
            List<Attendance> checkedIn = attendanceService
                    .getTodaysAttendancesByStatus(Attendance.AttendanceStatus.CHECKED_IN);
            List<Attendance> present = attendanceService
                    .getTodaysAttendancesByStatus(Attendance.AttendanceStatus.PRESENT);
            List<Attendance> noShows = attendanceService
                    .getTodaysAttendancesByStatus(Attendance.AttendanceStatus.NO_SHOW);
            List<Attendance> late = attendanceService.getTodaysAttendancesByStatus(Attendance.AttendanceStatus.LATE);

            model.addAttribute("todaysAttendances", todaysAttendances);
            model.addAttribute("pendingCheckIns", pendingCheckIns);
            model.addAttribute("checkedIn", checkedIn);
            model.addAttribute("present", present);
            model.addAttribute("noShows", noShows);
            model.addAttribute("late", late);
            model.addAttribute("title", "Today's Attendance");

            return "admin/attendance-today";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading today's attendance data");
            return "admin/attendance-today";
        }
    }

    @PostMapping("/attendance/{id}/checkin")
    public String checkInAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // For check-in, we need the appointment ID, not attendance ID
            // Let's use manual check-in with default values
            attendanceService.manualCheckIn(id, "Reception", "Manual check-in by admin");
            redirectAttributes.addFlashAttribute("success", "Patient checked in successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to check in patient: " + e.getMessage());
        }
        return "redirect:/admin/attendance";
    }

    @PostMapping("/attendance/{id}/present")
    public String markAttendancePresent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markPresent(id);
            redirectAttributes.addFlashAttribute("success", "Patient marked as present");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark patient as present: " + e.getMessage());
        }
        return "redirect:/admin/attendance";
    }

    @PostMapping("/attendance/{id}/noshow")
    public String markAttendanceNoShow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markNoShow(id, "Marked as no-show by admin");
            redirectAttributes.addFlashAttribute("success", "Patient marked as no-show");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark patient as no-show: " + e.getMessage());
        }
        return "redirect:/admin/attendance";
    }
}