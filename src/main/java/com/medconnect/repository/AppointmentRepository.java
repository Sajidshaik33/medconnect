package com.medconnect.repository;

import com.medconnect.model.Appointment;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByStatus(Appointment.Status status);
    List<Appointment> findByDoctorAndStatus(Doctor doctor, Appointment.Status status);
}
