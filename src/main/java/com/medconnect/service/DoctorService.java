package com.medconnect.service;

import com.medconnect.model.Doctor;
import com.medconnect.model.User;
import com.medconnect.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor saveDoctor(Doctor doctor) { return doctorRepository.save(doctor); }
    public Optional<Doctor> findById(Long id) { return doctorRepository.findById(id); }
    public Optional<Doctor> findByUser(User user) { return doctorRepository.findByUser(user); }
    public List<Doctor> getAllDoctors() { return doctorRepository.findAll(); }
    public List<Doctor> getDoctorsByDepartment(Long deptId) { return doctorRepository.findByDepartmentId(deptId); }
    public void deleteDoctor(Long id) { doctorRepository.deleteById(id); }
    public List<Doctor> searchDoctors(String keyword) {
        return doctorRepository.findAll().stream()
            .filter(d -> d.getUser() != null &&
                (d.getUser().getName().toLowerCase()
                    .contains(keyword.toLowerCase()) ||
                 d.getSpecialization().toLowerCase()
                    .contains(keyword.toLowerCase())))
            .collect(java.util.stream.Collectors.toList());
    }
}
