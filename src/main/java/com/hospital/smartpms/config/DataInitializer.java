package com.hospital.smartpms.config;

import com.hospital.smartpms.entity.Admin;
import com.hospital.smartpms.entity.Appointment;
import com.hospital.smartpms.entity.Attendance;
import com.hospital.smartpms.entity.Counselor;
import com.hospital.smartpms.entity.Doctor;
import com.hospital.smartpms.entity.Patient;
import com.hospital.smartpms.entity.Patient.PatientStatus;
import com.hospital.smartpms.entity.TreatmentHistory;
import com.hospital.smartpms.entity.Waitlist;
import com.hospital.smartpms.repository.AdminRepository;
import com.hospital.smartpms.repository.AppointmentRepository;
import com.hospital.smartpms.repository.AttendanceRepository;
import com.hospital.smartpms.repository.CounselorRepository;
import com.hospital.smartpms.repository.DoctorRepository;
import com.hospital.smartpms.repository.PatientRepository;
import com.hospital.smartpms.repository.TreatmentHistoryRepository;
import com.hospital.smartpms.repository.WaitlistRepository;
import com.hospital.smartpms.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private AdminRepository adminRepository;

        @Autowired
        private DoctorRepository doctorRepository;

        @Autowired
        private PatientRepository patientRepository;

        @Autowired
        private AppointmentRepository appointmentRepository;

        @Autowired
        private TreatmentHistoryRepository treatmentHistoryRepository;

        @Autowired
        private WaitlistRepository waitlistRepository;

        @Autowired
        private AttendanceRepository attendanceRepository;

        @Autowired
        private CounselorRepository counselorRepository;

        @Autowired
        private AttendanceService attendanceService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                try {
                        // Create default admin if not exists
                        if (adminRepository.findAll().isEmpty()) {
                                Admin admin = new Admin();
                                admin.setUsername("admin");
                                admin.setEmail("admin@pakenhamhospital.com");
                                admin.setPassword(passwordEncoder.encode("admin123"));
                                admin.setFirstName("System");
                                admin.setLastName("Administrator");
                                admin.setCreatedAt(LocalDateTime.now());
                                adminRepository.save(admin);
                                System.out.println("✅ Default admin created: admin / admin123");
                        }

                        // Create test doctors if not exists
                        if (doctorRepository.findAll().isEmpty()) {
                                // Doctor 1 - General Medicine
                                Doctor doctor1 = new Doctor();
                                doctor1.setEmail("dr.smith@pakenhamhospital.com");
                                doctor1.setPassword(passwordEncoder.encode("doctor123"));
                                doctor1.setFirstName("Dr. John");
                                doctor1.setLastName("Smith");
                                doctor1.setSpecialization("General Medicine");
                                doctor1.setLicenseNumber("DOC12345");
                                doctor1.setPhoneNumber("03-9876-5432");
                                doctor1.setExperience("10 years");
                                doctor1.setCreatedAt(LocalDateTime.now());
                                doctorRepository.save(doctor1);

                                // Doctor 2 - Cardiology
                                Doctor doctor2 = new Doctor();
                                doctor2.setEmail("dr.johnson@pakenhamhospital.com");
                                doctor2.setPassword(passwordEncoder.encode("doctor123"));
                                doctor2.setFirstName("Dr. Sarah");
                                doctor2.setLastName("Johnson");
                                doctor2.setSpecialization("Cardiology");
                                doctor2.setLicenseNumber("DOC67890");
                                doctor2.setPhoneNumber("03-9876-5433");
                                doctor2.setExperience("15 years");
                                doctor2.setCreatedAt(LocalDateTime.now());
                                doctorRepository.save(doctor2);

                                // Doctor 3 - Pediatrics
                                Doctor doctor3 = new Doctor();
                                doctor3.setEmail("dr.brown@pakenhamhospital.com");
                                doctor3.setPassword(passwordEncoder.encode("doctor123"));
                                doctor3.setFirstName("Dr. Michael");
                                doctor3.setLastName("Brown");
                                doctor3.setSpecialization("Pediatrics");
                                doctor3.setLicenseNumber("DOC11111");
                                doctor3.setPhoneNumber("03-9876-5434");
                                doctor3.setExperience("8 years");
                                doctor3.setCreatedAt(LocalDateTime.now());
                                doctorRepository.save(doctor3);

                                // Doctor 4 - Orthopedics
                                Doctor doctor4 = new Doctor();
                                doctor4.setEmail("dr.davis@pakenhamhospital.com");
                                doctor4.setPassword(passwordEncoder.encode("doctor123"));
                                doctor4.setFirstName("Dr. Emily");
                                doctor4.setLastName("Davis");
                                doctor4.setSpecialization("Orthopedics");
                                doctor4.setLicenseNumber("DOC22222");
                                doctor4.setPhoneNumber("03-9876-5435");
                                doctor4.setExperience("12 years");
                                doctor4.setCreatedAt(LocalDateTime.now());
                                doctorRepository.save(doctor4);

                                // Doctor 5 - Dermatology
                                Doctor doctor5 = new Doctor();
                                doctor5.setEmail("dr.wilson@pakenhamhospital.com");
                                doctor5.setPassword(passwordEncoder.encode("doctor123"));
                                doctor5.setFirstName("Dr. Robert");
                                doctor5.setLastName("Wilson");
                                doctor5.setSpecialization("Dermatology");
                                doctor5.setLicenseNumber("DOC33333");
                                doctor5.setPhoneNumber("03-9876-5436");
                                doctor5.setExperience("7 years");
                                doctor5.setCreatedAt(LocalDateTime.now());
                                doctorRepository.save(doctor5);

                                System.out.println("✅ Test doctors created: 5 doctors with specializations");
                        }

                        // Create test counselors if not exists
                        if (counselorRepository.findAll().isEmpty()) {
                                // Counselor 1 - Mental Health Specialist
                                Counselor counselor1 = new Counselor();
                                counselor1.setFirstName("Sarah");
                                counselor1.setLastName("Johnson");
                                counselor1.setEmail("sarah.johnson@pakenhamhospital.com");
                                counselor1.setPassword(passwordEncoder.encode("counselor123"));
                                counselor1.setPhoneNumber("03-9876-1001");
                                counselor1.setSpecialization(Counselor.Specialization.MENTAL_HEALTH);
                                counselor1.setCounselorType(Counselor.CounselorType.PSYCHOLOGIST);
                                counselor1.setAvailabilityStatus(Counselor.AvailabilityStatus.AVAILABLE);
                                counselor1.setYearsExperience(8);
                                counselor1.setConsultationFee(150.0);
                                counselor1.setLicenseNumber("PSY001");
                                counselor1.setQualifications("Ph.D. in Clinical Psychology");
                                counselor1.setBio(
                                                "Specializing in mental health and depression/anxiety disorders with 8 years of experience.");
                                counselor1.setLanguagesSpoken("English, Spanish");
                                counselor1.setOfficeLocation("Building A, Floor 2, Room 201");
                                counselor1.setOnlineConsultation(true);
                                counselor1.setInPersonConsultation(true);
                                counselor1.setCreatedAt(LocalDateTime.now());
                                counselorRepository.save(counselor1);

                                // Counselor 2 - Family Therapy
                                Counselor counselor2 = new Counselor();
                                counselor2.setFirstName("Michael");
                                counselor2.setLastName("Chen");
                                counselor2.setEmail("michael.chen@pakenhamhospital.com");
                                counselor2.setPassword(passwordEncoder.encode("counselor123"));
                                counselor2.setPhoneNumber("03-9876-1002");
                                counselor2.setSpecialization(Counselor.Specialization.FAMILY_COUNSELING);
                                counselor2.setCounselorType(Counselor.CounselorType.FAMILY_THERAPIST);
                                counselor2.setAvailabilityStatus(Counselor.AvailabilityStatus.AVAILABLE);
                                counselor2.setYearsExperience(12);
                                counselor2.setConsultationFee(180.0);
                                counselor2.setLicenseNumber("FAM002");
                                counselor2.setQualifications("M.A. in Family Therapy");
                                counselor2.setBio(
                                                "Expert in family dynamics and relationship counseling with over 12 years of experience.");
                                counselor2.setLanguagesSpoken("English, Mandarin");
                                counselor2.setOfficeLocation("Building A, Floor 2, Room 203");
                                counselor2.setOnlineConsultation(true);
                                counselor2.setInPersonConsultation(true);
                                counselor2.setCreatedAt(LocalDateTime.now());
                                counselorRepository.save(counselor2);

                                // Counselor 3 - Career Counseling
                                Counselor counselor3 = new Counselor();
                                counselor3.setFirstName("Emily");
                                counselor3.setLastName("Davis");
                                counselor3.setEmail("emily.davis@pakenhamhospital.com");
                                counselor3.setPassword(passwordEncoder.encode("counselor123"));
                                counselor3.setPhoneNumber("03-9876-1003");
                                counselor3.setSpecialization(Counselor.Specialization.CAREER_COUNSELING);
                                counselor3.setCounselorType(Counselor.CounselorType.CAREER_COUNSELOR);
                                counselor3.setAvailabilityStatus(Counselor.AvailabilityStatus.AVAILABLE);
                                counselor3.setYearsExperience(6);
                                counselor3.setConsultationFee(120.0);
                                counselor3.setLicenseNumber("CAR003");
                                counselor3.setQualifications("M.S. in Career Development");
                                counselor3.setBio(
                                                "Helping individuals navigate career transitions and professional development.");
                                counselor3.setLanguagesSpoken("English");
                                counselor3.setOfficeLocation("Building B, Floor 1, Room 105");
                                counselor3.setOnlineConsultation(true);
                                counselor3.setInPersonConsultation(true);
                                counselor3.setCreatedAt(LocalDateTime.now());
                                counselorRepository.save(counselor3);

                                // Counselor 4 - Trauma Therapy
                                Counselor counselor4 = new Counselor();
                                counselor4.setFirstName("Dr. Robert");
                                counselor4.setLastName("Martinez");
                                counselor4.setEmail("robert.martinez@pakenhamhospital.com");
                                counselor4.setPassword(passwordEncoder.encode("counselor123"));
                                counselor4.setPhoneNumber("03-9876-1004");
                                counselor4.setSpecialization(Counselor.Specialization.TRAUMA_THERAPY);
                                counselor4.setCounselorType(Counselor.CounselorType.LICENSED_COUNSELOR);
                                counselor4.setAvailabilityStatus(Counselor.AvailabilityStatus.AVAILABLE);
                                counselor4.setYearsExperience(15);
                                counselor4.setConsultationFee(200.0);
                                counselor4.setLicenseNumber("TRA004");
                                counselor4.setQualifications("Ph.D. in Trauma Psychology");
                                counselor4.setBio(
                                                "Specialized in trauma recovery and PTSD treatment with extensive experience.");
                                counselor4.setLanguagesSpoken("English, French");
                                counselor4.setOfficeLocation("Building A, Floor 3, Room 301");
                                counselor4.setOnlineConsultation(true);
                                counselor4.setInPersonConsultation(true);
                                counselor4.setCreatedAt(LocalDateTime.now());
                                counselorRepository.save(counselor4);

                                // Counselor 5 - Currently Busy
                                Counselor counselor5 = new Counselor();
                                counselor5.setFirstName("Lisa");
                                counselor5.setLastName("Thompson");
                                counselor5.setEmail("lisa.thompson@pakenhamhospital.com");
                                counselor5.setPassword(passwordEncoder.encode("counselor123"));
                                counselor5.setPhoneNumber("03-9876-1005");
                                counselor5.setSpecialization(Counselor.Specialization.DEPRESSION_ANXIETY);
                                counselor5.setCounselorType(Counselor.CounselorType.THERAPIST);
                                counselor5.setAvailabilityStatus(Counselor.AvailabilityStatus.BUSY);
                                counselor5.setYearsExperience(10);
                                counselor5.setConsultationFee(160.0);
                                counselor5.setLicenseNumber("DEP005");
                                counselor5.setQualifications("M.A. in Clinical Psychology");
                                counselor5.setBio(
                                                "Focused on anxiety and depression treatment with cognitive behavioral therapy approach.");
                                counselor5.setLanguagesSpoken("English");
                                counselor5.setOfficeLocation("Building A, Floor 2, Room 205");
                                counselor5.setOnlineConsultation(true);
                                counselor5.setInPersonConsultation(false);
                                counselor5.setCreatedAt(LocalDateTime.now());
                                counselorRepository.save(counselor5);

                                System.out.println(
                                                "✅ Test counselors created: 5 counselors with various specializations");
                        }

                        // Create test patients if not exists
                        if (patientRepository.findAll().isEmpty()) {
                                // Patient 1 - Jane Doe
                                Patient patient1 = new Patient();
                                patient1.setEmail("jane.doe@example.com");
                                patient1.setPassword(passwordEncoder.encode("patient123"));
                                patient1.setFirstName("Jane");
                                patient1.setLastName("Doe");
                                patient1.setPhoneNumber("04-1234-5678");
                                patient1.setAddress("123 Main St, Pakenham VIC 3810");
                                patient1.setDateOfBirth("1990-05-15");
                                patient1.setStatus(PatientStatus.ACTIVE);
                                patient1.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient1);

                                // Patient 2 - Mark Thompson
                                Patient patient2 = new Patient();
                                patient2.setEmail("mark.thompson@example.com");
                                patient2.setPassword(passwordEncoder.encode("patient123"));
                                patient2.setFirstName("Mark");
                                patient2.setLastName("Thompson");
                                patient2.setPhoneNumber("04-2345-6789");
                                patient2.setAddress("456 Oak Avenue, Pakenham VIC 3810");
                                patient2.setDateOfBirth("1985-03-22");
                                patient2.setStatus(PatientStatus.VIP);
                                patient2.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient2);

                                // Patient 3 - Lisa Chen
                                Patient patient3 = new Patient();
                                patient3.setEmail("lisa.chen@example.com");
                                patient3.setPassword(passwordEncoder.encode("patient123"));
                                patient3.setFirstName("Lisa");
                                patient3.setLastName("Chen");
                                patient3.setPhoneNumber("04-3456-7890");
                                patient3.setAddress("789 Pine Road, Pakenham VIC 3810");
                                patient3.setDateOfBirth("1992-11-08");
                                patient3.setStatus(PatientStatus.INACTIVE);
                                patient3.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient3);

                                // Patient 4 - David Miller
                                Patient patient4 = new Patient();
                                patient4.setEmail("david.miller@example.com");
                                patient4.setPassword(passwordEncoder.encode("patient123"));
                                patient4.setFirstName("David");
                                patient4.setLastName("Miller");
                                patient4.setPhoneNumber("04-4567-8901");
                                patient4.setAddress("321 Elm Street, Pakenham VIC 3810");
                                patient4.setDateOfBirth("1978-07-14");
                                patient4.setStatus(PatientStatus.ACTIVE);
                                patient4.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient4);

                                // Patient 5 - Emma Watson
                                Patient patient5 = new Patient();
                                patient5.setEmail("emma.watson@example.com");
                                patient5.setPassword(passwordEncoder.encode("patient123"));
                                patient5.setFirstName("Emma");
                                patient5.setLastName("Watson");
                                patient5.setPhoneNumber("04-5678-9012");
                                patient5.setAddress("654 Maple Drive, Pakenham VIC 3810");
                                patient5.setDateOfBirth("1995-12-03");
                                patient5.setStatus(PatientStatus.VIP);
                                patient5.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient5);

                                // Patient 6 - Robert Garcia
                                Patient patient6 = new Patient();
                                patient6.setEmail("robert.garcia@example.com");
                                patient6.setPassword(passwordEncoder.encode("patient123"));
                                patient6.setFirstName("Robert");
                                patient6.setLastName("Garcia");
                                patient6.setPhoneNumber("04-6789-0123");
                                patient6.setAddress("987 Cedar Lane, Pakenham VIC 3810");
                                patient6.setDateOfBirth("1983-09-27");
                                patient6.setStatus(PatientStatus.INACTIVE);
                                patient6.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient6);

                                // Patient 7 - Sophie Anderson
                                Patient patient7 = new Patient();
                                patient7.setEmail("sophie.anderson@example.com");
                                patient7.setPassword(passwordEncoder.encode("patient123"));
                                patient7.setFirstName("Sophie");
                                patient7.setLastName("Anderson");
                                patient7.setPhoneNumber("04-7890-1234");
                                patient7.setAddress("147 Birch Court, Pakenham VIC 3810");
                                patient7.setDateOfBirth("1988-02-18");
                                patient7.setStatus(PatientStatus.ACTIVE);
                                patient7.setCreatedAt(LocalDateTime.now());
                                patientRepository.save(patient7);

                                System.out.println("✅ Test patients created: 7 patients with diverse profiles");
                        }

                        // Create test appointments if not exists
                        if (appointmentRepository.findAll().isEmpty()) {
                                List<Doctor> doctors = doctorRepository.findAll();
                                List<Patient> patients = patientRepository.findAll();

                                if (!doctors.isEmpty() && !patients.isEmpty()) {
                                        // Today's appointments
                                        Appointment todayApp1 = new Appointment();
                                        todayApp1.setPatient(patients.get(0)); // Jane Doe
                                        todayApp1.setDoctor(doctors.get(0)); // Dr. Smith
                                        todayApp1.setAppointmentDate(
                                                        LocalDateTime.now().withHour(9).withMinute(0).withSecond(0));
                                        todayApp1.setReason("Annual health checkup");
                                        todayApp1.setNotes("Patient reports feeling well, routine examination");
                                        todayApp1.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                                        appointmentRepository.save(todayApp1);

                                        Appointment todayApp2 = new Appointment();
                                        todayApp2.setPatient(patients.get(1)); // Mark Thompson
                                        todayApp2.setDoctor(doctors.get(1)); // Dr. Johnson (Cardiology)
                                        todayApp2.setAppointmentDate(
                                                        LocalDateTime.now().withHour(11).withMinute(30).withSecond(0));
                                        todayApp2.setReason("Heart palpitation consultation");
                                        todayApp2.setNotes("Patient experiencing irregular heartbeat");
                                        todayApp2.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                                        appointmentRepository.save(todayApp2);

                                        Appointment todayApp3 = new Appointment();
                                        todayApp3.setPatient(patients.get(2)); // Lisa Chen
                                        todayApp3.setDoctor(doctors.get(4)); // Dr. Wilson (Dermatology)
                                        todayApp3.setAppointmentDate(
                                                        LocalDateTime.now().withHour(14).withMinute(0).withSecond(0));
                                        todayApp3.setReason("Skin rash examination");
                                        todayApp3.setNotes("Rash appeared 3 days ago on arms");
                                        todayApp3.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                                        appointmentRepository.save(todayApp3);

                                        // Tomorrow's appointments
                                        Appointment tomorrowApp1 = new Appointment();
                                        tomorrowApp1.setPatient(patients.get(3)); // David Miller
                                        tomorrowApp1.setDoctor(doctors.get(3)); // Dr. Davis (Orthopedics)
                                        tomorrowApp1.setAppointmentDate(
                                                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
                                                                        .withSecond(0));
                                        tomorrowApp1.setReason("Knee pain follow-up");
                                        tomorrowApp1.setNotes("Post-surgery check, 2 weeks after knee surgery");
                                        tomorrowApp1.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                                        appointmentRepository.save(tomorrowApp1);

                                        Appointment tomorrowApp2 = new Appointment();
                                        tomorrowApp2.setPatient(patients.get(4)); // Emma Watson
                                        tomorrowApp2.setDoctor(doctors.get(2)); // Dr. Brown (Pediatrics)
                                        tomorrowApp2.setAppointmentDate(
                                                        LocalDateTime.now().plusDays(1).withHour(15).withMinute(30)
                                                                        .withSecond(0));
                                        tomorrowApp2.setReason("Vaccination consultation");
                                        tomorrowApp2.setNotes("Discussion about travel vaccinations");
                                        tomorrowApp2.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                                        appointmentRepository.save(tomorrowApp2);

                                        // Past completed appointments
                                        Appointment pastApp1 = new Appointment();
                                        pastApp1.setPatient(patients.get(0)); // Jane Doe
                                        pastApp1.setDoctor(doctors.get(0)); // Dr. Smith
                                        pastApp1.setAppointmentDate(
                                                        LocalDateTime.now().minusDays(5).withHour(11).withMinute(0)
                                                                        .withSecond(0));
                                        pastApp1.setReason("Blood pressure monitoring");
                                        pastApp1.setNotes("Routine BP check - results normal");
                                        pastApp1.setStatus(Appointment.AppointmentStatus.COMPLETED);
                                        appointmentRepository.save(pastApp1);

                                        Appointment pastApp2 = new Appointment();
                                        pastApp2.setPatient(patients.get(5)); // Robert Garcia
                                        pastApp2.setDoctor(doctors.get(1)); // Dr. Johnson (Cardiology)
                                        pastApp2.setAppointmentDate(
                                                        LocalDateTime.now().minusDays(3).withHour(14).withMinute(30)
                                                                        .withSecond(0));
                                        pastApp2.setReason("Chest pain investigation");
                                        pastApp2.setNotes("ECG performed, results reviewed");
                                        pastApp2.setStatus(Appointment.AppointmentStatus.COMPLETED);
                                        appointmentRepository.save(pastApp2);

                                        Appointment pastApp3 = new Appointment();
                                        pastApp3.setPatient(patients.get(6)); // Sophie Anderson
                                        pastApp3.setDoctor(doctors.get(0)); // Dr. Smith
                                        pastApp3.setAppointmentDate(
                                                        LocalDateTime.now().minusDays(7).withHour(9).withMinute(30)
                                                                        .withSecond(0));
                                        pastApp3.setReason("Cold and flu symptoms");
                                        pastApp3.setNotes("Prescribed medication, advised rest");
                                        pastApp3.setStatus(Appointment.AppointmentStatus.COMPLETED);
                                        appointmentRepository.save(pastApp3);

                                        // Cancelled appointment
                                        Appointment cancelledApp = new Appointment();
                                        cancelledApp.setPatient(patients.get(1)); // Mark Thompson
                                        cancelledApp.setDoctor(doctors.get(0)); // Dr. Smith
                                        cancelledApp.setAppointmentDate(
                                                        LocalDateTime.now().minusDays(1).withHour(16).withMinute(0)
                                                                        .withSecond(0));
                                        cancelledApp.setReason("General consultation");
                                        cancelledApp.setNotes(
                                                        "Patient requested cancellation due to scheduling conflict");
                                        cancelledApp.setStatus(Appointment.AppointmentStatus.CANCELLED);
                                        appointmentRepository.save(cancelledApp);

                                        System.out.println(
                                                        "✅ Test appointments created: 9 appointments with various statuses and specializations");
                                }
                        }

                        // Create test treatment history if not exists
                        if (treatmentHistoryRepository.findAll().isEmpty()) {
                                List<Doctor> doctors = doctorRepository.findAll();
                                List<Patient> patients = patientRepository.findAll();
                                List<Appointment> appointments = appointmentRepository.findAll();

                                if (!doctors.isEmpty() && !patients.isEmpty()) {
                                        // Treatment 1 - Jane Doe with Dr. Smith
                                        TreatmentHistory treatment1 = new TreatmentHistory();
                                        treatment1.setPatient(patients.get(0)); // Jane Doe
                                        treatment1.setDoctor(doctors.get(0)); // Dr. Smith
                                        if (appointments.size() > 5) {
                                                treatment1.setAppointment(appointments.get(5)); // Link to past
                                                                                                // appointment
                                        }
                                        treatment1.setTreatmentDate(LocalDateTime.now().minusDays(5));
                                        treatment1.setDiagnosis("Hypertension (Stage 1)");
                                        treatment1.setTreatment(
                                                        "Lifestyle counseling, dietary modifications, and antihypertensive medication");
                                        treatment1.setPrescription(
                                                        "Lisinopril 10mg once daily, taken in the morning. Monitor blood pressure daily.");
                                        treatment1.setNotes(
                                                        "Patient advised to reduce sodium intake, increase physical activity, and schedule follow-up in 2 weeks.");
                                        treatment1.setFollowUpDate(LocalDateTime.now().plusDays(9));
                                        treatmentHistoryRepository.save(treatment1);

                                        // Treatment 2 - Robert Garcia with Dr. Johnson (Cardiology)
                                        TreatmentHistory treatment2 = new TreatmentHistory();
                                        treatment2.setPatient(patients.get(5)); // Robert Garcia
                                        treatment2.setDoctor(doctors.get(1)); // Dr. Johnson
                                        if (appointments.size() > 6) {
                                                treatment2.setAppointment(appointments.get(6)); // Link to past
                                                                                                // appointment
                                        }
                                        treatment2.setTreatmentDate(LocalDateTime.now().minusDays(3));
                                        treatment2.setDiagnosis("Atypical Chest Pain - Non-cardiac");
                                        treatment2.setTreatment(
                                                        "ECG performed, stress test recommended, muscle relaxants prescribed");
                                        treatment2
                                                        .setPrescription(
                                                                        "Ibuprofen 400mg twice daily with food. Avoid heavy lifting for 1 week.");
                                        treatment2.setNotes(
                                                        "ECG results normal. Chest pain likely musculoskeletal. Patient reassured.");
                                        treatment2.setFollowUpDate(LocalDateTime.now().plusDays(11));
                                        treatmentHistoryRepository.save(treatment2);

                                        // Treatment 3 - Sophie Anderson with Dr. Smith
                                        TreatmentHistory treatment3 = new TreatmentHistory();
                                        treatment3.setPatient(patients.get(6)); // Sophie Anderson
                                        treatment3.setDoctor(doctors.get(0)); // Dr. Smith
                                        if (appointments.size() > 7) {
                                                treatment3.setAppointment(appointments.get(7)); // Link to past
                                                                                                // appointment
                                        }
                                        treatment3.setTreatmentDate(LocalDateTime.now().minusDays(7));
                                        treatment3.setDiagnosis("Upper Respiratory Tract Infection (Common Cold)");
                                        treatment3.setTreatment(
                                                        "Symptomatic treatment with rest, increased fluid intake, and supportive care");
                                        treatment3
                                                        .setPrescription(
                                                                        "Paracetamol 500mg every 6 hours as needed. Throat lozenges for comfort.");
                                        treatment3.setNotes(
                                                        "Patient presented with runny nose, sore throat, and mild fever. Recovery expected in 7-10 days.");
                                        treatmentHistoryRepository.save(treatment3);

                                        // Treatment 4 - Mark Thompson with Dr. Johnson (Cardiology) - older record
                                        TreatmentHistory treatment4 = new TreatmentHistory();
                                        treatment4.setPatient(patients.get(1)); // Mark Thompson
                                        treatment4.setDoctor(doctors.get(1)); // Dr. Johnson
                                        treatment4.setTreatmentDate(LocalDateTime.now().minusDays(30));
                                        treatment4.setDiagnosis("Palpitations - Anxiety Related");
                                        treatment4.setTreatment(
                                                        "Holter monitor assessment, stress management counseling, lifestyle modifications");
                                        treatment4.setPrescription(
                                                        "Propranolol 10mg as needed for palpitations. Magnesium supplement daily.");
                                        treatment4.setNotes(
                                                        "24-hour Holter monitor showed occasional PVCs, likely stress-related. Patient counseled on stress management.");
                                        treatment4.setFollowUpDate(LocalDateTime.now().plusDays(14)); // Today's
                                                                                                      // appointment is
                                                                                                      // follow-up
                                        treatmentHistoryRepository.save(treatment4);

                                        // Treatment 5 - Lisa Chen with Dr. Wilson (Dermatology) - older record
                                        TreatmentHistory treatment5 = new TreatmentHistory();
                                        treatment5.setPatient(patients.get(2)); // Lisa Chen
                                        treatment5.setDoctor(doctors.get(4)); // Dr. Wilson
                                        treatment5.setTreatmentDate(LocalDateTime.now().minusDays(45));
                                        treatment5.setDiagnosis("Eczema (Atopic Dermatitis)");
                                        treatment5.setTreatment(
                                                        "Topical corticosteroids, moisturizing regimen, trigger avoidance");
                                        treatment5.setPrescription(
                                                        "Hydrocortisone 1% cream twice daily to affected areas. Cetaphil moisturizer daily.");
                                        treatment5.setNotes(
                                                        "Mild eczema on hands and arms. Patient advised on skincare routine and trigger avoidance.");
                                        treatment5.setFollowUpDate(LocalDateTime.now().plusDays(2)); // Today's
                                                                                                     // appointment is
                                                                                                     // follow-up
                                        treatmentHistoryRepository.save(treatment5);

                                        // Treatment 6 - David Miller with Dr. Davis (Orthopedics) - post-surgery
                                        TreatmentHistory treatment6 = new TreatmentHistory();
                                        treatment6.setPatient(patients.get(3)); // David Miller
                                        treatment6.setDoctor(doctors.get(3)); // Dr. Davis
                                        treatment6.setTreatmentDate(LocalDateTime.now().minusDays(14));
                                        treatment6.setDiagnosis("Post-operative Care - Arthroscopic Knee Surgery");
                                        treatment6.setTreatment(
                                                        "Post-surgical wound care, physiotherapy referral, pain management");
                                        treatment6.setPrescription(
                                                        "Paracetamol + Codeine as needed for pain. Physiotherapy 3x weekly.");
                                        treatment6.setNotes(
                                                        "Knee arthroscopy completed successfully. Wound healing well. Physiotherapy commenced.");
                                        treatment6.setFollowUpDate(LocalDateTime.now().plusDays(1)); // Tomorrow's
                                                                                                     // appointment is
                                                                                                     // follow-up
                                        treatmentHistoryRepository.save(treatment6);

                                        // Treatment 7 - Emma Watson with Dr. Brown (Pediatrics) - vaccination record
                                        TreatmentHistory treatment7 = new TreatmentHistory();
                                        treatment7.setPatient(patients.get(4)); // Emma Watson
                                        treatment7.setDoctor(doctors.get(2)); // Dr. Brown
                                        treatment7.setTreatmentDate(LocalDateTime.now().minusDays(90));
                                        treatment7.setDiagnosis("Routine Vaccination - Travel Health");
                                        treatment7.setTreatment(
                                                        "Hepatitis A/B vaccination series, travel health counseling");
                                        treatment7.setPrescription(
                                                        "Complete Hepatitis A/B series. Malaria prophylaxis if traveling to endemic areas.");
                                        treatment7.setNotes(
                                                        "Patient planning travel to Southeast Asia. First dose of Hepatitis A/B completed. Follow-up for booster.");
                                        treatment7.setFollowUpDate(LocalDateTime.now().plusDays(1)); // Tomorrow's
                                                                                                     // appointment is
                                                                                                     // follow-up
                                        treatmentHistoryRepository.save(treatment7);

                                        System.out.println(
                                                        "✅ Test treatment history created: 7 comprehensive treatment records across specializations");
                                }
                        }

                        // Create test waitlist entries if not exists
                        if (waitlistRepository.findAll().isEmpty()) {
                                List<Doctor> doctors = doctorRepository.findAll();
                                List<Patient> patients = patientRepository.findAll();

                                if (!doctors.isEmpty() && !patients.isEmpty()) {
                                        // Waitlist Entry 1 - Jane Doe waiting for Dr. Johnson (Cardiology)
                                        Waitlist waitlist1 = new Waitlist();
                                        waitlist1.setPatient(patients.get(0)); // Jane Doe
                                        waitlist1.setDoctor(doctors.get(1)); // Dr. Johnson (Cardiology)
                                        waitlist1
                                                        .setPreferredDate(LocalDateTime.now().plusDays(3).withHour(10)
                                                                        .withMinute(0).withSecond(0));
                                        waitlist1.setReason("Heart palpitation consultation - urgent");
                                        waitlist1.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist1.setPriority(2); // High priority
                                        waitlist1.setCreatedAt(LocalDateTime.now().minusDays(2));
                                        waitlistRepository.save(waitlist1);

                                        // Waitlist Entry 2 - Mark Thompson waiting for Dr. Brown (Pediatrics)
                                        Waitlist waitlist2 = new Waitlist();
                                        waitlist2.setPatient(patients.get(1)); // Mark Thompson
                                        waitlist2.setDoctor(doctors.get(2)); // Dr. Brown (Pediatrics)
                                        waitlist2.setPreferredDate(
                                                        LocalDateTime.now().plusDays(5).withHour(14).withMinute(30)
                                                                        .withSecond(0));
                                        waitlist2.setReason("Family vaccination consultation");
                                        waitlist2.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist2.setPriority(1); // Medium priority
                                        waitlist2.setCreatedAt(LocalDateTime.now().minusDays(1));
                                        waitlistRepository.save(waitlist2);

                                        // Waitlist Entry 3 - Lisa Chen waiting for Dr. Davis (Orthopedics)
                                        Waitlist waitlist3 = new Waitlist();
                                        waitlist3.setPatient(patients.get(2)); // Lisa Chen
                                        waitlist3.setDoctor(doctors.get(3)); // Dr. Davis (Orthopedics)
                                        waitlist3.setPreferredDate(LocalDateTime.now().plusDays(7).withHour(9)
                                                        .withMinute(0).withSecond(0));
                                        waitlist3.setReason("Knee pain assessment");
                                        waitlist3.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist3.setPriority(1); // Medium priority
                                        waitlist3.setCreatedAt(LocalDateTime.now().minusHours(8));
                                        waitlistRepository.save(waitlist3);

                                        // Waitlist Entry 4 - David Miller waiting for Dr. Smith (General Medicine)
                                        Waitlist waitlist4 = new Waitlist();
                                        waitlist4.setPatient(patients.get(3)); // David Miller
                                        waitlist4.setDoctor(doctors.get(0)); // Dr. Smith (General Medicine)
                                        waitlist4
                                                        .setPreferredDate(LocalDateTime.now().plusDays(4).withHour(11)
                                                                        .withMinute(0).withSecond(0));
                                        waitlist4.setReason("Annual health checkup");
                                        waitlist4.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist4.setPriority(0); // Normal priority
                                        waitlist4.setCreatedAt(LocalDateTime.now().minusHours(4));
                                        waitlistRepository.save(waitlist4);

                                        // Waitlist Entry 5 - Emma Watson waiting for Dr. Wilson (Dermatology)
                                        Waitlist waitlist5 = new Waitlist();
                                        waitlist5.setPatient(patients.get(4)); // Emma Watson
                                        waitlist5.setDoctor(doctors.get(4)); // Dr. Wilson (Dermatology)
                                        waitlist5.setPreferredDate(
                                                        LocalDateTime.now().plusDays(6).withHour(15).withMinute(30)
                                                                        .withSecond(0));
                                        waitlist5.setReason("Skin condition follow-up");
                                        waitlist5.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist5.setPriority(1); // Medium priority
                                        waitlist5.setCreatedAt(LocalDateTime.now().minusHours(2));
                                        waitlistRepository.save(waitlist5);

                                        // Waitlist Entry 6 - Robert Garcia waiting for Dr. Johnson (Cardiology) -
                                        // NOTIFIED
                                        Waitlist waitlist6 = new Waitlist();
                                        waitlist6.setPatient(patients.get(5)); // Robert Garcia
                                        waitlist6.setDoctor(doctors.get(1)); // Dr. Johnson (Cardiology)
                                        waitlist6
                                                        .setPreferredDate(LocalDateTime.now().plusDays(2).withHour(13)
                                                                        .withMinute(0).withSecond(0));
                                        waitlist6.setReason("Chest pain follow-up");
                                        waitlist6.setStatus(Waitlist.WaitlistStatus.NOTIFIED);
                                        waitlist6.setPriority(2); // High priority
                                        waitlist6.setCreatedAt(LocalDateTime.now().minusDays(3));
                                        waitlist6.setNotifiedAt(LocalDateTime.now().minusHours(6));
                                        waitlistRepository.save(waitlist6);

                                        // Waitlist Entry 7 - Sophie Anderson waiting for Dr. Smith (General Medicine)
                                        Waitlist waitlist7 = new Waitlist();
                                        waitlist7.setPatient(patients.get(6)); // Sophie Anderson
                                        waitlist7.setDoctor(doctors.get(0)); // Dr. Smith (General Medicine)
                                        waitlist7
                                                        .setPreferredDate(LocalDateTime.now().plusDays(8).withHour(12)
                                                                        .withMinute(0).withSecond(0));
                                        waitlist7.setReason("Cold symptoms follow-up");
                                        waitlist7.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist7.setPriority(0); // Normal priority
                                        waitlist7.setCreatedAt(LocalDateTime.now().minusHours(1));
                                        waitlistRepository.save(waitlist7);

                                        // Waitlist Entry 8 - Jane Doe waiting for Dr. Wilson (Dermatology) - Different
                                        // specialty
                                        Waitlist waitlist8 = new Waitlist();
                                        waitlist8.setPatient(patients.get(0)); // Jane Doe
                                        waitlist8.setDoctor(doctors.get(4)); // Dr. Wilson (Dermatology)
                                        waitlist8.setPreferredDate(
                                                        LocalDateTime.now().plusDays(10).withHour(16).withMinute(0)
                                                                        .withSecond(0));
                                        waitlist8.setReason("Mole examination");
                                        waitlist8.setStatus(Waitlist.WaitlistStatus.WAITING);
                                        waitlist8.setPriority(0); // Normal priority
                                        waitlist8.setCreatedAt(LocalDateTime.now().minusMinutes(30));
                                        waitlistRepository.save(waitlist8);

                                        System.out.println(
                                                        "✅ Test waitlist entries created: 8 waitlist entries with various statuses and priorities");
                                }
                        }

                        // Create demo attendance data
                        if (attendanceRepository.findAll().isEmpty()) {
                                System.out.println("🏥 Creating attendance demo data...");

                                List<Appointment> todaysAppointments = appointmentRepository
                                                .findByAppointmentDateBetween(
                                                                LocalDateTime.now().withHour(0).withMinute(0)
                                                                                .withSecond(0),
                                                                LocalDateTime.now().withHour(23).withMinute(59)
                                                                                .withSecond(59));

                                if (!todaysAppointments.isEmpty()) {
                                        // Create attendance records for today's appointments
                                        for (int i = 0; i < todaysAppointments.size(); i++) {
                                                Appointment appointment = todaysAppointments.get(i);
                                                Attendance attendance = new Attendance(appointment);

                                                // Set different statuses and scenarios for demonstration
                                                switch (i % 5) {
                                                        case 0: // CHECKED_IN - Patient has checked in but not yet seen
                                                                attendance.setCheckInTime(
                                                                                LocalDateTime.now().minusMinutes(45));
                                                                attendance.setCheckInMethod(
                                                                                Attendance.CheckInMethod.QR_CODE);
                                                                attendance.setStatus(
                                                                                Attendance.AttendanceStatus.CHECKED_IN);
                                                                attendance.setLocation("Reception Desk");
                                                                attendance.setNotes("Checked in via QR code scan");
                                                                break;

                                                        case 1: // PRESENT - Patient is currently in appointment
                                                                attendance.setCheckInTime(
                                                                                LocalDateTime.now().minusMinutes(90));
                                                                attendance.setCheckInMethod(
                                                                                Attendance.CheckInMethod.MANUAL);
                                                                attendance.setStatus(
                                                                                Attendance.AttendanceStatus.PRESENT);
                                                                attendance.setLocation("Reception Desk");
                                                                attendance.setNotes("Manual check-in by staff");
                                                                break;

                                                        case 2: // NO_SHOW - Patient didn't show up
                                                                attendance.setStatus(
                                                                                Attendance.AttendanceStatus.NO_SHOW);
                                                                attendance.setNotes(
                                                                                "Patient did not show up for appointment");
                                                                break;

                                                        case 3: // LATE - Patient arrived late
                                                                LocalDateTime lateCheckIn = appointment
                                                                                .getAppointmentDate().plusMinutes(20);
                                                                attendance.setCheckInTime(lateCheckIn);
                                                                attendance.setCheckInMethod(
                                                                                Attendance.CheckInMethod.ONLINE);
                                                                attendance.setStatus(Attendance.AttendanceStatus.LATE);
                                                                attendance.setLocation("Online Portal");
                                                                attendance.setNotes(
                                                                                "Patient checked in 20 minutes late");
                                                                break;

                                                        case 4: // SCHEDULED - Still waiting for check-in
                                                                attendance.setStatus(
                                                                                Attendance.AttendanceStatus.SCHEDULED);
                                                                attendance.setNotes("Awaiting patient check-in");
                                                                break;
                                                }

                                                attendanceRepository.save(attendance);
                                        }
                                }

                                // Create additional attendance records for recent check-ins demonstration
                                List<Appointment> allAppointments = appointmentRepository.findAll();
                                if (allAppointments.size() >= 8) {
                                        // Recent check-ins from last hour
                                        for (int i = 4; i < 8 && i < allAppointments.size(); i++) {
                                                Appointment appointment = allAppointments.get(i);

                                                // Skip if attendance already exists for this appointment
                                                if (attendanceRepository.findByAppointmentId(appointment.getId())
                                                                .isPresent()) {
                                                        continue;
                                                }

                                                Attendance recentAttendance = new Attendance(appointment);

                                                // Set recent check-in times (within last hour)
                                                int minutesAgo = (i - 3) * 15; // 15, 30, 45 minutes ago
                                                recentAttendance.setCheckInTime(
                                                                LocalDateTime.now().minusMinutes(minutesAgo));
                                                recentAttendance.setCheckInMethod(
                                                                i % 2 == 0 ? Attendance.CheckInMethod.QR_CODE
                                                                                : Attendance.CheckInMethod.KIOSK);
                                                recentAttendance.setStatus(Attendance.AttendanceStatus.CHECKED_IN);
                                                recentAttendance.setLocation(
                                                                i % 2 == 0 ? "Reception Desk" : "Self-Service Kiosk");
                                                recentAttendance.setNotes("Recent check-in for demonstration");

                                                attendanceRepository.save(recentAttendance);
                                        }
                                }

                                // Create overdue check-ins (appointments that started but patient hasn't
                                // checked in)
                                if (allAppointments.size() >= 12) {
                                        for (int i = 8; i < 12 && i < allAppointments.size(); i++) {
                                                Appointment appointment = allAppointments.get(i);

                                                // Skip if attendance already exists
                                                if (attendanceRepository.findByAppointmentId(appointment.getId())
                                                                .isPresent()) {
                                                        continue;
                                                }

                                                // Create appointments that are overdue (appointment time passed but no
                                                // check-in)
                                                LocalDateTime overdueTime = LocalDateTime.now().minusHours(2);
                                                appointment.setAppointmentDate(overdueTime);
                                                appointmentRepository.save(appointment);

                                                Attendance overdueAttendance = new Attendance(appointment);
                                                overdueAttendance.setStatus(Attendance.AttendanceStatus.SCHEDULED);
                                                overdueAttendance.setNotes(
                                                                "Overdue for check-in - appointment time passed");

                                                attendanceRepository.save(overdueAttendance);
                                        }
                                }

                                // Create some completed appointments with attendance history
                                if (allAppointments.size() >= 16) {
                                        for (int i = 12; i < 16 && i < allAppointments.size(); i++) {
                                                Appointment appointment = allAppointments.get(i);

                                                // Skip if attendance already exists
                                                if (attendanceRepository.findByAppointmentId(appointment.getId())
                                                                .isPresent()) {
                                                        continue;
                                                }

                                                // Set appointment to past time
                                                LocalDateTime pastTime = LocalDateTime.now().minusDays(1)
                                                                .minusHours(i - 12);
                                                appointment.setAppointmentDate(pastTime);
                                                appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                                                appointmentRepository.save(appointment);

                                                Attendance completedAttendance = new Attendance(appointment);
                                                completedAttendance.setCheckInTime(pastTime.plusMinutes(5)); // Checked
                                                                                                             // in 5 min
                                                                                                             // after
                                                                                                             // appointment
                                                                                                             // time
                                                completedAttendance.setCheckInMethod(
                                                                Attendance.CheckInMethod.values()[i
                                                                                % Attendance.CheckInMethod
                                                                                                .values().length]);
                                                completedAttendance.setStatus(Attendance.AttendanceStatus.PRESENT);
                                                completedAttendance.setLocation("Reception Desk");
                                                completedAttendance.setNotes(
                                                                "Completed appointment with attendance record");

                                                attendanceRepository.save(completedAttendance);
                                        }
                                }

                                System.out.println(
                                                "✅ Attendance demo data created with various statuses and check-in scenarios");
                                System.out.println("   - Today's appointments with mixed attendance statuses");
                                System.out.println("   - Recent check-ins for real-time monitoring");
                                System.out.println("   - Overdue check-ins for alerts demonstration");
                                System.out.println("   - Historical attendance records");
                                System.out.println("   - QR codes generated for all attendance records");
                        }
                } catch (Exception e) {
                        System.err.println("❌ Error initializing data: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}