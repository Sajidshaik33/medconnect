package com.medconnect.repository;

import com.medconnect.model.Prescription;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient(Patient patient);
    List<Prescription> findByDoctor(Doctor doctor);
}
