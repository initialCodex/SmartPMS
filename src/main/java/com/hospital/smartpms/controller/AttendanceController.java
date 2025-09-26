package com.hospital.smartpms.controller;

import com.hospital.smartpms.entity.Attendance;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.service.AttendanceService;
import com.hospital.smartpms.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private PatientService patientService;

    // QR Code check-in page
    @GetMapping("/checkin")
    public String checkInPage() {
        return "attendance/checkin";
    }

    // Process QR code check-in
    @PostMapping("/checkin/qr")
    @ResponseBody
    public ResponseEntity<?> checkInByQr(@RequestParam String qrCode,
            @RequestParam(required = false) String location) {
        try {
            Attendance attendance = attendanceService.checkInByQrCode(qrCode, location);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully checked in!");
            response.put("patientName", attendance.getAppointment().getPatient().getFirstName() +
                    " " + attendance.getAppointment().getPatient().getLastName());
            response.put("doctorName", "Dr. " + attendance.getAppointment().getDoctor().getFirstName() +
                    " " + attendance.getAppointment().getDoctor().getLastName());
            response.put("appointmentTime", attendance.getAppointment().getAppointmentDate().toString());
            response.put("status", attendance.getStatus().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Online check-in for patients
    @PostMapping("/checkin/online/{appointmentId}")
    public String onlineCheckIn(@PathVariable Long appointmentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> patientOpt = patientService.findByEmail(authentication.getName());
            if (!patientOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient not found");
                return "redirect:/patient/appointments";
            }

            Patient patient = patientOpt.get();
            Attendance attendance = attendanceService.onlineCheckIn(appointmentId, patient);

            String message = "Successfully checked in! ";
            if (attendance.getStatus() == Attendance.AttendanceStatus.LATE) {
                message += "Please note that you are checked in late.";
            }

            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/patient/appointments";
    }

    // Manual check-in by staff
    @PostMapping("/checkin/manual/{appointmentId}")
    public String manualCheckIn(@PathVariable Long appointmentId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.manualCheckIn(appointmentId, location, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Patient checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/attendance/today";
    }

    // Mark patient as present
    @PostMapping("/{attendanceId}/present")
    public String markPresent(@PathVariable Long attendanceId,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markPresent(attendanceId);
            redirectAttributes.addFlashAttribute("successMessage", "Patient marked as present!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/attendance/today";
    }

    // Mark patient as no-show
    @PostMapping("/{attendanceId}/noshow")
    public String markNoShow(@PathVariable Long attendanceId,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.markNoShow(attendanceId, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Patient marked as no-show!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/attendance/today";
    }

    // Get QR code for appointment (AJAX)
    @GetMapping("/qr/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> getQrCode(@PathVariable Long appointmentId) {
        try {
            Optional<Attendance> attendanceOpt = attendanceService.getAttendanceByAppointmentId(appointmentId);

            if (!attendanceOpt.isPresent()) {
                // Create attendance record if it doesn't exist
                Attendance attendance = attendanceService.createAttendanceRecord(appointmentId);
                Map<String, String> response = new HashMap<>();
                response.put("qrCode", attendance.getQrCode());
                return ResponseEntity.ok(response);
            }

            Map<String, String> response = new HashMap<>();
            response.put("qrCode", attendanceOpt.get().getQrCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get attendance status for appointment (AJAX)
    @GetMapping("/status/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> getAttendanceStatus(@PathVariable Long appointmentId) {
        try {
            Optional<Attendance> attendanceOpt = attendanceService.getAttendanceByAppointmentId(appointmentId);

            Map<String, Object> response = new HashMap<>();
            if (attendanceOpt.isPresent()) {
                Attendance attendance = attendanceOpt.get();
                response.put("status", attendance.getStatus().toString());
                response.put("checkInTime", attendance.getCheckInTime());
                response.put("checkInMethod", attendance.getCheckInMethod());
                response.put("notes", attendance.getNotes());
            } else {
                response.put("status", "SCHEDULED");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Real-time attendance monitoring (for staff dashboard)
    @GetMapping("/realtime")
    @ResponseBody
    public ResponseEntity<?> getRealtimeAttendance() {
        try {
            List<Attendance> recentCheckIns = attendanceService.getRecentCheckIns(30); // Last 30 minutes
            List<Attendance> pendingAttendances = attendanceService.getTodaysPendingAttendances();
            List<Attendance> overdueCheckIns = attendanceService.getOverdueCheckIns();

            Map<String, Object> response = new HashMap<>();
            response.put("recentCheckIns", recentCheckIns);
            response.put("pendingAttendances", pendingAttendances);
            response.put("overdueCheckIns", overdueCheckIns);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}