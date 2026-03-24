package com.medconnect.controller;

import com.medconnect.model.*;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired private PatientService patientService;
    @Autowired private UserService userService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private DoctorService doctorService;
    @Autowired private PrescriptionService prescriptionService;
    @Autowired private RatingService ratingService;
    @Autowired private AppointmentRepository appointmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                Patient patient = patientService.findByUser(user).orElse(null);
                if (patient == null) {
                    patient = new Patient();
                    patient.setUser(user);
                    patientService.savePatient(patient);
                }
                List<Appointment> appointments =
                    appointmentService.getAppointmentsByPatient(patient);

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
                model.addAttribute("prescriptions",
                    prescriptionService.getPrescriptionsByPatient(patient));
                model.addAttribute("patient", patient);
            }
            model.addAttribute("user", user);
            return "patient/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "patient/dashboard";
        }
    }

    @GetMapping("/book-appointment")
    public String bookAppointment(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointment", new Appointment());
        return "patient/book-appointment";
    }

    @PostMapping("/book-appointment")
    public String bookAppointmentSubmit(
            @ModelAttribute Appointment appointment,
            Authentication auth,
            Model model) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            if (patient == null) {
                patient = new Patient();
                patient.setUser(user);
                patientService.savePatient(patient);
            }

            // Check duplicate booking
            Doctor doctor = doctorService.findById(
                appointment.getDoctor().getId()).orElse(null);

            boolean slotTaken = appointmentRepository
                .existsByDoctorAndApptDateAndTimeSlotAndStatusNot(
                    doctor,
                    appointment.getApptDate(),
                    appointment.getTimeSlot(),
                    Appointment.Status.CANCELLED
                );

            if (slotTaken) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("appointment", appointment);
                model.addAttribute("error",
                    "This time slot is already booked! Please select another slot.");
                return "patient/book-appointment";
            }

            appointment.setPatient(patient);
            appointment.setStatus(Appointment.Status.PENDING);
            appointmentService.saveAppointment(appointment);
        }
        return "redirect:/patient/appointments";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            if (patient != null)
                model.addAttribute("appointments",
                    appointmentService.getAppointmentsByPatient(patient));
        }
        model.addAttribute("user", user);
        return "patient/appointments";
    }

    @GetMapping("/cancel-appointment/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        appointmentService.updateStatus(id, Appointment.Status.CANCELLED);
        return "redirect:/patient/appointments";
    }

    @GetMapping("/prescriptions")
    public String viewPrescriptions(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            if (patient != null)
                model.addAttribute("prescriptions",
                    prescriptionService.getPrescriptionsByPatient(patient));
        }
        model.addAttribute("user", user);
        return "patient/prescriptions";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            if (patient == null) {
                patient = new Patient();
                patient.setUser(user);
                patientService.savePatient(patient);
            }
            model.addAttribute("patient", patient);
            model.addAttribute("user", user);
        }
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @ModelAttribute Patient patient,
            @RequestParam String name,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            user.setName(name);
            userService.updateUser(user);
            Patient existing = patientService.findByUser(user).orElse(null);
            if (existing != null) {
                existing.setPhone(patient.getPhone());
                existing.setAddress(patient.getAddress());
                existing.setGender(patient.getGender());
                existing.setBloodGroup(patient.getBloodGroup());
                existing.setDob(patient.getDob());
                patientService.savePatient(existing);
            }
        }
        return "redirect:/patient/profile?updated";
    }

    @GetMapping("/search-doctors")
    public String searchDoctors(
            @RequestParam(required = false) String keyword,
            Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();

        if (keyword != null && !keyword.isEmpty()) {
            doctors = doctorService.searchDoctors(keyword);
            model.addAttribute("keyword", keyword);
        }

        model.addAttribute("doctors", doctors);

        Map<Long, Double> ratings = new HashMap<>();
        Map<Long, Integer> reviewCounts = new HashMap<>();

        for (Doctor doctor : doctors) {
            try {
                ratings.put(doctor.getId(),
                    ratingService.getAverageRating(doctor));
                reviewCounts.put(doctor.getId(),
                    ratingService.getRatingsByDoctor(doctor).size());
            } catch (Exception e) {
                ratings.put(doctor.getId(), 0.0);
                reviewCounts.put(doctor.getId(), 0);
            }
        }

        model.addAttribute("ratings", ratings);
        model.addAttribute("reviewCounts", reviewCounts);

        return "patient/search-doctors";
    }

    @GetMapping("/rate-doctor")
    public String rateDoctorPage(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patient", patient);
        }
        return "patient/rate-doctor";
    }

    @PostMapping("/rate-doctor")
    public String submitRating(
            @RequestParam Long doctorId,
            @RequestParam int stars,
            @RequestParam String review,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            Patient patient = patientService.findByUser(user).orElse(null);
            Doctor doctor = doctorService.findById(doctorId).orElse(null);
            if (patient != null && doctor != null) {
                Rating rating = new Rating();
                rating.setDoctor(doctor);
                rating.setPatient(patient);
                rating.setStars(stars);
                rating.setReview(review);
                ratingService.saveRating(rating);
            }
        }
        return "redirect:/patient/rate-doctor?success";
    }
}