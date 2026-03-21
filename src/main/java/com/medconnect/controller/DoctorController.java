package com.medconnect.controller;

import com.medconnect.model.*;
import com.medconnect.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired private DoctorService doctorService;
    @Autowired private UserService userService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private PrescriptionService prescriptionService;
    @Autowired private RatingService ratingService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                Doctor doctor = doctorService.findByUser(user).orElse(null);
                if (doctor == null) {
                    doctor = new Doctor();
                    doctor.setUser(user);
                    doctor.setSpecialization("General");
                    doctor.setQualification("MBBS");
                    doctor.setExperience(1);
                    doctor.setFees(500);
                    doctorService.saveDoctor(doctor);
                }
                List<Appointment> appointments =
                    appointmentService.getAppointmentsByDoctor(doctor);

                long pending = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.Status.PENDING)
                    .count();
                long confirmed = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.Status.CONFIRMED)
                    .count();
                long completed = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
                    .count();

                model.addAttribute("appointments", appointments);
                model.addAttribute("pendingCount", pending);
                model.addAttribute("confirmedCount", confirmed);
                model.addAttribute("completedCount", completed);
                model.addAttribute("doctor", doctor);
            }
            model.addAttribute("user", user);
            return "doctor/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "doctor/dashboard";
        }
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Doctor doctor = doctorService.findByUser(user).orElse(null);
            if (doctor != null)
                model.addAttribute("appointments",
                    appointmentService.getAppointmentsByDoctor(doctor));
        }
        model.addAttribute("user", user);
        return "doctor/appointments";
    }

    @GetMapping("/confirm-appointment/{id}")
    public String confirmAppointment(@PathVariable Long id) {
        appointmentService.updateStatus(id, Appointment.Status.CONFIRMED);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/complete-appointment/{id}")
    public String completeAppointment(@PathVariable Long id) {
        appointmentService.updateStatus(id, Appointment.Status.COMPLETED);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/prescription/{appointmentId}")
    public String writePrescription(
            @PathVariable Long appointmentId, Model model) {
        model.addAttribute("appointment",
            appointmentService.findById(appointmentId).orElse(null));
        model.addAttribute("prescription", new Prescription());
        return "doctor/prescription";
    }

    @PostMapping("/prescription/{appointmentId}")
    public String savePrescription(
            @PathVariable Long appointmentId,
            @ModelAttribute Prescription prescription,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        Appointment appointment =
            appointmentService.findById(appointmentId).orElse(null);
        if (appointment != null && user != null) {
            Doctor doctor = doctorService.findByUser(user).orElse(null);
            prescription.setDoctor(doctor);
            prescription.setPatient(appointment.getPatient());
            prescription.setAppointment(appointment);
            prescriptionService.savePrescription(prescription);
            appointmentService.updateStatus(
                appointmentId, Appointment.Status.COMPLETED);
        }
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/schedule")
    public String viewSchedule(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Doctor doctor = doctorService.findByUser(user).orElse(null);
            model.addAttribute("doctor", doctor);
        }
        model.addAttribute("user", user);
        return "doctor/schedule";
    }

    @PostMapping("/schedule")
    public String updateSchedule(
            @RequestParam String availableDays,
            @RequestParam String fees,
            @RequestParam String specialization,
            @RequestParam String qualification,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Doctor doctor = doctorService.findByUser(user).orElse(null);
            if (doctor != null) {
                doctor.setAvailableDays(availableDays);
                doctor.setFees(Double.parseDouble(fees));
                doctor.setSpecialization(specialization);
                doctor.setQualification(qualification);
                doctorService.saveDoctor(doctor);
            }
        }
        return "redirect:/doctor/schedule?updated";
    }

    @GetMapping("/reviews")
    public String viewReviews(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Doctor doctor = doctorService.findByUser(user).orElse(null);
            if (doctor != null) {
                model.addAttribute("ratings",
                    ratingService.getRatingsByDoctor(doctor));
                model.addAttribute("avgRating",
                    ratingService.getAverageRating(doctor));
                model.addAttribute("doctor", doctor);
            }
        }
        model.addAttribute("user", user);
        return "doctor/reviews";
    }
}