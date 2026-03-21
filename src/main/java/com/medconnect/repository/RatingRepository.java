package com.medconnect.repository;

import com.medconnect.model.Rating;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByDoctor(Doctor doctor);
    Optional<Rating> findByDoctorAndPatient(Doctor doctor, Patient patient);
    List<Rating> findByPatient(Patient patient);
}