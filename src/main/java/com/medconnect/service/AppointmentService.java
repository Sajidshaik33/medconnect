package com.medconnect.service;

import com.medconnect.model.Appointment;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import com.medconnect.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    public Appointment saveAppointment(Appointment appointment) {
        Appointment saved = appointmentRepository.save(appointment);
        // Notify patient
        if (saved.getPatient() != null) {
            notificationService.createNotification(
                saved.getPatient().getUser(),
                "Your appointment has been booked successfully with Dr. " +
                (saved.getDoctor() != null ? saved.getDoctor().getUser().getName() : ""),
                "APPOINTMENT_BOOKED"
            );
        }
        // Notify doctor
        if (saved.getDoctor() != null) {
            notificationService.createNotification(
                saved.getDoctor().getUser(),
                "New appointment request from " +
                (saved.getPatient() != null ? saved.getPatient().getUser().getName() : ""),
                "NEW_APPOINTMENT"
            );
        }
        return saved;
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getAppointmentsByStatus(Appointment.Status status) {
        return appointmentRepository.findByStatus(status);
    }

    public Appointment updateStatus(Long id, Appointment.Status status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        // Notifications based on status
        if (status == Appointment.Status.CONFIRMED) {
            if (updated.getPatient() != null) {
                notificationService.createNotification(
                    updated.getPatient().getUser(),
                    "Your appointment with Dr. " +
                    (updated.getDoctor() != null ? updated.getDoctor().getUser().getName() : "") +
                    " has been CONFIRMED!",
                    "APPOINTMENT_CONFIRMED"
                );
            }
        } else if (status == Appointment.Status.CANCELLED) {
            if (updated.getPatient() != null) {
                notificationService.createNotification(
                    updated.getPatient().getUser(),
                    "Your appointment has been cancelled.",
                    "APPOINTMENT_CANCELLED"
                );
            }
        } else if (status == Appointment.Status.COMPLETED) {
            if (updated.getPatient() != null) {
                notificationService.createNotification(
                    updated.getPatient().getUser(),
                    "Your appointment has been completed. Please rate your doctor!",
                    "APPOINTMENT_COMPLETED"
                );
            }
        }
        return updated;
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}