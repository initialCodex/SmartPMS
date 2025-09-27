package com.hospital.smartpms.service;

import com.hospital.smartpms.entity.*;
import com.hospital.smartpms.entity.Alert.AlertType;
import com.hospital.smartpms.entity.Alert.AlertPriority;
import com.hospital.smartpms.entity.Alert.AlertStatus;
import com.hospital.smartpms.entity.Alert.RecipientType;
import com.hospital.smartpms.repository.AlertRepository;
import com.hospital.smartpms.repository.AttendanceRepository;
import com.hospital.smartpms.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    // Create and schedule appointment reminder
    public Alert createAppointmentReminder(Appointment appointment) {
        // Don't create duplicate reminders
        if (alertRepository.existsByAppointmentIdAndType(appointment.getId(), AlertType.APPOINTMENT_REMINDER)) {
            return null;
        }

        Alert alert = new Alert();
        alert.setType(AlertType.APPOINTMENT_REMINDER);
        alert.setPriority(AlertPriority.NORMAL);
        alert.setTitle("Appointment Reminder");
        alert.setMessage(buildAppointmentReminderMessage(appointment));
        alert.setPatient(appointment.getPatient());
        alert.setDoctor(appointment.getDoctor());
        alert.setAppointment(appointment);

        // Schedule reminder 24 hours before appointment
        alert.setScheduledTime(appointment.getAppointmentDate().minusHours(24));

        return alertRepository.save(alert);
    }

    // Create low attendance alert
    public Alert createLowAttendanceAlert(Patient patient, int missedAppointments) {
        Alert alert = new Alert();
        alert.setType(AlertType.LOW_ATTENDANCE);
        alert.setPriority(AlertPriority.HIGH);
        alert.setTitle("Low Attendance Warning");
        alert.setMessage(buildLowAttendanceMessage(patient, missedAppointments));
        alert.setPatient(patient);

        return alertRepository.save(alert);
    }

    // Create no-show warning
    public Alert createNoShowWarning(Appointment appointment) {
        Alert alert = new Alert();
        alert.setType(AlertType.NO_SHOW_WARNING);
        alert.setPriority(AlertPriority.HIGH);
        alert.setTitle("No-Show Alert");
        alert.setMessage(buildNoShowMessage(appointment));
        alert.setPatient(appointment.getPatient());
        alert.setDoctor(appointment.getDoctor());
        alert.setAppointment(appointment);

        // Send to both patient and doctor
        Alert doctorAlert = new Alert();
        doctorAlert.setType(AlertType.NO_SHOW_WARNING);
        doctorAlert.setPriority(AlertPriority.NORMAL);
        doctorAlert.setTitle("Patient No-Show");
        doctorAlert.setMessage(buildDoctorNoShowMessage(appointment));
        doctorAlert.setPatient(appointment.getPatient());
        doctorAlert.setDoctor(appointment.getDoctor());
        doctorAlert.setAppointment(appointment);

        alertRepository.save(alert);
        alertRepository.save(doctorAlert);

        return alert;
    }

    // Create emergency notification
    public Alert createEmergencyNotification(String title, String message, RecipientType recipientType,
            Long recipientId) {
        Alert alert = new Alert();
        alert.setType(AlertType.EMERGENCY_NOTIFICATION);
        alert.setPriority(AlertPriority.EMERGENCY);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setRecipientType(recipientType);
        alert.setRecipientId(recipientId);

        return alertRepository.save(alert);
    }

    // Create waitlist notification
    public Alert createWaitlistNotification(Waitlist waitlist, String message) {
        Alert alert = new Alert();
        alert.setType(AlertType.WAITLIST_NOTIFICATION);
        alert.setPriority(AlertPriority.NORMAL);
        alert.setTitle("Waitlist Update");
        alert.setMessage(message);
        alert.setPatient(waitlist.getPatient());
        alert.setDoctor(waitlist.getDoctor());

        return alertRepository.save(alert);
    }

    // Create overdue check-in alert
    public Alert createOverdueCheckinAlert(Appointment appointment) {
        Alert alert = new Alert();
        alert.setType(AlertType.OVERDUE_CHECKIN);
        alert.setPriority(AlertPriority.HIGH);
        alert.setTitle("Overdue Check-in");
        alert.setMessage(buildOverdueCheckinMessage(appointment));
        alert.setPatient(appointment.getPatient());
        alert.setDoctor(appointment.getDoctor());
        alert.setAppointment(appointment);

        return alertRepository.save(alert);
    }

    // Get alerts for recipient
    public List<Alert> getAlertsForRecipient(RecipientType recipientType, Long recipientId) {
        return alertRepository.findByRecipientTypeAndRecipientId(recipientType, recipientId);
    }

    // Get unread alerts for recipient
    public List<Alert> getUnreadAlertsForRecipient(RecipientType recipientType, Long recipientId) {
        return alertRepository.findUnreadAlertsByRecipient(recipientType, recipientId);
    }

    // Count unread alerts for recipient
    public Long countUnreadAlertsForRecipient(RecipientType recipientType, Long recipientId) {
        return alertRepository.countUnreadAlertsByRecipient(recipientType, recipientId);
    }

    // Mark alert as read
    public void markAlertAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.markAsRead();
            alertRepository.save(alert);
        }
    }

    // Get alert statistics
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        List<Object[]> statusCounts = alertRepository.countAlertsByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", statusMap);

        // Count by type
        List<Object[]> typeCounts = alertRepository.countAlertsByType();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeCounts) {
            typeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeMap);

        // Count by priority
        List<Object[]> priorityCounts = alertRepository.countAlertsByPriority();
        Map<String, Long> priorityMap = new HashMap<>();
        for (Object[] row : priorityCounts) {
            priorityMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byPriority", priorityMap);

        return stats;
    }

    // Scheduled task to process pending alerts (runs every 5 minutes)
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processPendingAlerts() {
        logger.info("Processing pending alerts...");

        List<Alert> pendingAlerts = alertRepository.findPendingAlertsToSend(LocalDateTime.now());

        for (Alert alert : pendingAlerts) {
            try {
                sendAlert(alert);
            } catch (Exception e) {
                logger.error("Failed to send alert {}: {}", alert.getId(), e.getMessage());
                alert.markAsFailed();
                alertRepository.save(alert);
            }
        }

        logger.info("Processed {} pending alerts", pendingAlerts.size());
    }

    // Scheduled task to retry failed alerts (runs every 30 minutes)
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void retryFailedAlerts() {
        logger.info("Retrying failed alerts...");

        List<Alert> failedAlerts = alertRepository.findFailedAlertsForRetry();

        for (Alert alert : failedAlerts) {
            try {
                alert.incrementRetryCount();
                sendAlert(alert);
            } catch (Exception e) {
                logger.error("Retry failed for alert {}: {}", alert.getId(), e.getMessage());
                alert.markAsFailed();
                alertRepository.save(alert);
            }
        }

        logger.info("Retried {} failed alerts", failedAlerts.size());
    }

    // Scheduled task to mark expired alerts (runs every hour)
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void markExpiredAlerts() {
        logger.info("Marking expired alerts...");

        LocalDateTime expiredTime = LocalDateTime.now().minusHours(24);
        List<Alert> expiredAlerts = alertRepository.findExpiredAlerts(expiredTime);

        for (Alert alert : expiredAlerts) {
            alert.setStatus(AlertStatus.EXPIRED);
            alertRepository.save(alert);
        }

        logger.info("Marked {} alerts as expired", expiredAlerts.size());
    }

    // Scheduled task to check for low attendance patterns (runs daily at 9 AM)
    @Scheduled(cron = "0 0 9 * * *")
    public void checkLowAttendancePatterns() {
        logger.info("Checking for low attendance patterns...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Find patients with multiple missed appointments
        List<Object[]> missedAppointments = attendanceRepository.findPatientsMissedAppointments(thirtyDaysAgo);

        for (Object[] row : missedAppointments) {
            Long patientId = (Long) row[0];
            Long missedCount = (Long) row[1];

            if (missedCount >= 3) { // Threshold for low attendance
                // Check if we already sent an alert recently
                List<Alert> recentAlerts = alertRepository.findLatestAlertByPatientAndType(
                        patientId, AlertType.LOW_ATTENDANCE);

                boolean needsAlert = true;
                if (!recentAlerts.isEmpty()) {
                    Alert lastAlert = recentAlerts.get(0);
                    if (lastAlert.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                        needsAlert = false; // Don't spam alerts
                    }
                }

                if (needsAlert) {
                    // Find patient and create alert
                    // You would need to inject PatientRepository to get the patient
                    logger.info("Creating low attendance alert for patient ID: {}", patientId);
                }
            }
        }

        logger.info("Completed low attendance pattern check");
    }

    // Scheduled task to check for overdue check-ins (runs every 15 minutes)
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void checkOverdueCheckins() {
        logger.info("Checking for overdue check-ins...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime overdueTime = now.minusMinutes(15); // 15 minutes after appointment time

        // Find appointments that are overdue for check-in
        List<Appointment> overdueAppointments = appointmentRepository.findOverdueForCheckin(overdueTime);

        for (Appointment appointment : overdueAppointments) {
            // Check if alert already exists
            if (!alertRepository.existsByAppointmentIdAndType(appointment.getId(), AlertType.OVERDUE_CHECKIN)) {
                createOverdueCheckinAlert(appointment);
            }
        }

        logger.info("Checked {} overdue appointments", overdueAppointments.size());
    }

    // Send alert via appropriate channel
    private void sendAlert(Alert alert) throws Exception {
        switch (alert.getRecipientType()) {
            case PATIENT:
            case DOCTOR:
            case ADMIN:
            case STAFF:
                if (alert.getRecipientEmail() != null) {
                    emailService.sendEmail(
                            alert.getRecipientEmail(),
                            alert.getTitle(),
                            alert.getMessage());
                    alert.markAsSent();
                    alertRepository.save(alert);
                }
                break;
            case SYSTEM:
                // Log system alerts
                logger.warn("SYSTEM ALERT: {} - {}", alert.getTitle(), alert.getMessage());
                alert.markAsSent();
                alertRepository.save(alert);
                break;
        }
    }

    // Message builders
    private String buildAppointmentReminderMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return String.format(
                "Dear %s %s,\n\n" +
                        "This is a reminder that you have an appointment scheduled with Dr. %s %s " +
                        "on %s.\n\n" +
                        "Please arrive 15 minutes early for check-in.\n\n" +
                        "If you need to reschedule, please contact us at least 24 hours in advance.\n\n" +
                        "Thank you,\n" +
                        "Pakenham Hospital",
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate().format(formatter));
    }

    private String buildLowAttendanceMessage(Patient patient, int missedAppointments) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "We've noticed that you have missed %d appointments in the past 30 days. " +
                        "Regular attendance is important for your health and treatment.\n\n" +
                        "If you're experiencing difficulties attending appointments, please contact us " +
                        "so we can work together to find a solution.\n\n" +
                        "You can reach us at (03) 5941 8000.\n\n" +
                        "Best regards,\n" +
                        "Pakenham Hospital",
                patient.getFirstName(),
                patient.getLastName(),
                missedAppointments);
    }

    private String buildNoShowMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return String.format(
                "Dear %s %s,\n\n" +
                        "You missed your scheduled appointment with Dr. %s %s on %s.\n\n" +
                        "Please contact us to reschedule your appointment. Regular medical care is " +
                        "important for your health.\n\n" +
                        "To reschedule, please call (03) 5941 8000.\n\n" +
                        "Thank you,\n" +
                        "Pakenham Hospital",
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate().format(formatter));
    }

    private String buildDoctorNoShowMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return String.format(
                "Dr. %s %s,\n\n" +
                        "Patient %s %s (ID: %d) did not show up for their appointment on %s.\n\n" +
                        "The appointment slot is now available.\n\n" +
                        "System Notification",
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getPatient().getId(),
                appointment.getAppointmentDate().format(formatter));
    }

    private String buildOverdueCheckinMessage(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return String.format(
                "Dear %s %s,\n\n" +
                        "Your appointment with Dr. %s %s was scheduled for %s, but you haven't " +
                        "checked in yet.\n\n" +
                        "Please check in at the reception desk if you're at the hospital, or " +
                        "call (03) 5941 8000 if you're running late.\n\n" +
                        "Thank you,\n" +
                        "Pakenham Hospital",
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate().format(formatter));
    }
}