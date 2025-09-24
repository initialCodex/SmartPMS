package com.hospital.smartpms.repository;

import com.hospital.smartpms.entity.Counselor;
import com.hospital.smartpms.entity.Counselor.Specialization;
import com.hospital.smartpms.entity.Counselor.CounselorType;
import com.hospital.smartpms.entity.Counselor.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Long> {

    // Find by email
    Optional<Counselor> findByEmail(String email);

    // Find by specialization
    List<Counselor> findBySpecialization(Specialization specialization);

    // Find by counselor type
    List<Counselor> findByCounselorType(CounselorType counselorType);

    // Find by availability status
    List<Counselor> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);

    // Find available counselors
    List<Counselor> findByAvailabilityStatusOrderByFirstNameAsc(AvailabilityStatus availabilityStatus);

    // Find counselors by specialization and availability
    List<Counselor> findBySpecializationAndAvailabilityStatus(
            Specialization specialization, AvailabilityStatus availabilityStatus);

    // Find counselors who accept online consultations
    List<Counselor> findByOnlineConsultationTrueAndAvailabilityStatus(AvailabilityStatus availabilityStatus);

    // Find counselors who accept in-person consultations
    List<Counselor> findByInPersonConsultationTrueAndAvailabilityStatus(AvailabilityStatus availabilityStatus);

    // Search counselors by name
    @Query("SELECT c FROM Counselor c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Counselor> findByNameContainingIgnoreCase(@Param("name") String name);

    // Find counselors with specific years of experience or more
    List<Counselor> findByYearsExperienceGreaterThanEqualAndAvailabilityStatus(
            Integer years, AvailabilityStatus availabilityStatus);

    // Find counselors with fee range
    @Query("SELECT c FROM Counselor c WHERE c.consultationFee BETWEEN :minFee AND :maxFee " +
            "AND c.availabilityStatus = :status")
    List<Counselor> findByFeeRangeAndAvailabilityStatus(
            @Param("minFee") Double minFee,
            @Param("maxFee") Double maxFee,
            @Param("status") AvailabilityStatus status);

    // Find counselors by languages
    @Query("SELECT c FROM Counselor c WHERE LOWER(c.languagesSpoken) LIKE LOWER(CONCAT('%', :language, '%')) " +
            "AND c.availabilityStatus = :status")
    List<Counselor> findByLanguagesContainingAndAvailabilityStatus(
            @Param("language") String language,
            @Param("status") AvailabilityStatus status);

    // Get counselor statistics
    @Query("SELECT c.specialization, COUNT(c) FROM Counselor c GROUP BY c.specialization")
    List<Object[]> countCounselorsBySpecialization();

    @Query("SELECT c.counselorType, COUNT(c) FROM Counselor c GROUP BY c.counselorType")
    List<Object[]> countCounselorsByType();

    @Query("SELECT c.availabilityStatus, COUNT(c) FROM Counselor c GROUP BY c.availabilityStatus")
    List<Object[]> countCounselorsByAvailability();

    // Find top-rated counselors (would need session rating data)
    @Query("SELECT c, AVG(cs.sessionRating) as avgRating FROM Counselor c " +
            "LEFT JOIN c.counselingSessions cs " +
            "WHERE cs.sessionRating IS NOT NULL " +
            "GROUP BY c " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedCounselors();

    // Find counselors with most sessions
    @Query("SELECT c, COUNT(cs) as sessionCount FROM Counselor c " +
            "LEFT JOIN c.counselingSessions cs " +
            "GROUP BY c " +
            "ORDER BY sessionCount DESC")
    List<Object[]> findCounselorsWithMostSessions();

    // Check if email exists
    boolean existsByEmail(String email);

    // Find counselors by license number
    Optional<Counselor> findByLicenseNumber(String licenseNumber);

    // Find counselors by office location
    List<Counselor> findByOfficeLocationContainingIgnoreCase(String location);

    // Advanced search
    @Query("SELECT c FROM Counselor c WHERE " +
            "(:specialization IS NULL OR c.specialization = :specialization) AND " +
            "(:counselorType IS NULL OR c.counselorType = :counselorType) AND " +
            "(:minExperience IS NULL OR c.yearsExperience >= :minExperience) AND " +
            "(:maxFee IS NULL OR c.consultationFee <= :maxFee) AND " +
            "(:language IS NULL OR LOWER(c.languagesSpoken) LIKE LOWER(CONCAT('%', :language, '%'))) AND " +
            "(:onlineOnly IS NULL OR :onlineOnly = FALSE OR c.onlineConsultation = TRUE) AND " +
            "c.availabilityStatus = 'AVAILABLE' " +
            "ORDER BY c.firstName ASC")
    List<Counselor> findCounselorsWithFilters(
            @Param("specialization") Specialization specialization,
            @Param("counselorType") CounselorType counselorType,
            @Param("minExperience") Integer minExperience,
            @Param("maxFee") Double maxFee,
            @Param("language") String language,
            @Param("onlineOnly") Boolean onlineOnly);
}