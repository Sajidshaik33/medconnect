package com.medconnect.service;

import com.medconnect.model.Patient;
import com.medconnect.model.User;
import com.medconnect.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public Patient savePatient(Patient patient) { return patientRepository.save(patient); }
    public Optional<Patient> findById(Long id) { return patientRepository.findById(id); }
    public Optional<Patient> findByUser(User user) { return patientRepository.findByUser(user); }
    public List<Patient> getAllPatients() { return patientRepository.findAll(); }
    public void deletePatient(Long id) { patientRepository.deleteById(id); }
}
