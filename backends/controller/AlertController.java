package com.hospital.smartpms.controller;

import com.hospital.smartpms.entity.Alert;
import com.hospital.smartpms.entity.Alert.RecipientType;
import com.hospital.smartpms.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // Admin - View all alerts dashboard
    @GetMapping("/admin")
    public String adminAlertsDashboard(Model model, HttpSession session) {
        // Check if user is admin
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/";
        }

        // Get alert statistics
        Map<String, Object> stats = alertService.getAlertStatistics();
        model.addAttribute("alertStats", stats);

        return "admin/alerts-dashboard";
    }

    // Patient - View my alerts
    @GetMapping("/patient")
    public String patientAlerts(Model model, HttpSession session) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/login";
        }

        List<Alert> alerts = alertService.getAlertsForRecipient(RecipientType.PATIENT, patientId);
        Long unreadCount = alertService.countUnreadAlertsForRecipient(RecipientType.PATIENT, patientId);

        model.addAttribute("alerts", alerts);
        model.addAttribute("unreadCount", unreadCount);

        return "patient/alerts";
    }

    // Doctor - View my alerts
    @GetMapping("/doctor")
    public String doctorAlerts(Model model, HttpSession session) {
        Long doctorId = (Long) session.getAttribute("doctorId");
        if (doctorId == null) {
            return "redirect:/login";
        }

        List<Alert> alerts = alertService.getAlertsForRecipient(RecipientType.DOCTOR, doctorId);
        Long unreadCount = alertService.countUnreadAlertsForRecipient(RecipientType.DOCTOR, doctorId);

        model.addAttribute("alerts", alerts);
        model.addAttribute("unreadCount", unreadCount);

        return "doctor/alerts";
    }

    // API endpoints

    // Get unread alerts count for current user
    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        Long userId = null;
        RecipientType recipientType = null;

        if ("PATIENT".equals(userRole)) {
            userId = (Long) session.getAttribute("patientId");
            recipientType = RecipientType.PATIENT;
        } else if ("DOCTOR".equals(userRole)) {
            userId = (Long) session.getAttribute("doctorId");
            recipientType = RecipientType.DOCTOR;
        } else if ("ADMIN".equals(userRole)) {
            userId = (Long) session.getAttribute("adminId");
            recipientType = RecipientType.ADMIN;
        }

        if (userId == null || recipientType == null) {
            return ResponseEntity.badRequest().build();
        }

        Long unreadCount = alertService.countUnreadAlertsForRecipient(recipientType, userId);

        Map<String, Object> response = Map.of(
                "unreadCount", unreadCount,
                "hasUnread", unreadCount > 0);

        return ResponseEntity.ok(response);
    }

    // Get recent alerts for current user
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<Alert>> getRecentAlerts(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        Long userId = null;
        RecipientType recipientType = null;

        if ("PATIENT".equals(userRole)) {
            userId = (Long) session.getAttribute("patientId");
            recipientType = RecipientType.PATIENT;
        } else if ("DOCTOR".equals(userRole)) {
            userId = (Long) session.getAttribute("doctorId");
            recipientType = RecipientType.DOCTOR;
        } else if ("ADMIN".equals(userRole)) {
            userId = (Long) session.getAttribute("adminId");
            recipientType = RecipientType.ADMIN;
        }

        if (userId == null || recipientType == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Alert> alerts = alertService.getUnreadAlertsForRecipient(recipientType, userId);

        return ResponseEntity.ok(alerts);
    }

    // Mark alert as read
    @PostMapping("/api/{alertId}/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAlertAsRead(@PathVariable Long alertId, HttpSession session) {
        try {
            alertService.markAlertAsRead(alertId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Alert marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // Admin - Create emergency notification
    @PostMapping("/admin/emergency")
    public String createEmergencyNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String recipientType,
            @RequestParam(required = false) Long recipientId,
            HttpSession session) {

        // Check if user is admin
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/";
        }

        try {
            RecipientType type = RecipientType.valueOf(recipientType.toUpperCase());
            alertService.createEmergencyNotification(title, message, type, recipientId);
        } catch (Exception e) {
            // Handle error
        }

        return "redirect:/alerts/admin";
    }

    // Admin - Get alert statistics API
    @GetMapping("/admin/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAlertStatistics(HttpSession session) {
        // Check if user is admin
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> stats = alertService.getAlertStatistics();
        return ResponseEntity.ok(stats);
    }
}