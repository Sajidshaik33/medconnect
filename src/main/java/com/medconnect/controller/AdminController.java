package com.medconnect.controller;

import com.medconnect.model.Appointment;
import com.medconnect.model.Patient;
import com.medconnect.service.AppointmentService;
import com.medconnect.service.DoctorService;
import com.medconnect.service.PatientService;
import com.medconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private PatientService patientService;
    @Autowired private AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Appointment> allAppointments =
            appointmentService.getAllAppointments();

        long pending = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.Status.PENDING)
            .count();
        long confirmed = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.Status.CONFIRMED)
            .count();
        long completed = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
            .count();
        long cancelled = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.Status.CANCELLED)
            .count();

        model.addAttribute("totalDoctors",
            doctorService.getAllDoctors().size());
        model.addAttribute("totalPatients",
            patientService.getAllPatients().size());
        model.addAttribute("totalAppointments",
            allAppointments.size());
        model.addAttribute("totalUsers",
            userService.getAllUsers().size());
        model.addAttribute("recentAppointments", allAppointments);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("confirmedCount", confirmed);
        model.addAttribute("completedCount", completed);
        model.addAttribute("cancelledCount", cancelled);

        return "admin/dashboard";
    }

    @GetMapping("/doctors")
    public String viewDoctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/doctors";
    }

    @GetMapping("/patients")
    public String viewPatients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "admin/patients";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model) {
        model.addAttribute("appointments",
            appointmentService.getAllAppointments());
        return "admin/appointments";
    }

    @GetMapping("/delete-doctor/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
        } catch (Exception e) {
            System.out.println("Error deleting doctor: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    @GetMapping("/delete-patient/{id}")
    public String deletePatient(@PathVariable Long id) {
        try {
            Patient patient = patientService.findById(id).orElse(null);
            if (patient != null) {
                // Pehle appointments delete karo
                List<Appointment> appointments =
                    appointmentService.getAppointmentsByPatient(patient);
                for (Appointment a : appointments) {
                    appointmentService.deleteAppointment(a.getId());
                }
                // Phir patient delete karo
                patientService.deletePatient(id);
            }
        } catch (Exception e) {
            System.out.println("Error deleting patient: " + e.getMessage());
        }
        return "redirect:/admin/patients";
    }
    @Autowired
    private com.medconnect.repository.DepartmentRepository departmentRepository;

    // View Departments
    @GetMapping("/departments")
    public String viewDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("department", new com.medconnect.model.Department());
        return "admin/departments";
    }

    // Add Department
    @PostMapping("/departments")
    public String addDepartment(
            @ModelAttribute com.medconnect.model.Department department) {
        departmentRepository.save(department);
        return "redirect:/admin/departments?added";
    }

    // Delete Department
    @GetMapping("/delete-department/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return "redirect:/admin/departments";
    }
}