package com.medconnect.service;

import com.medconnect.model.Prescription;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import com.medconnect.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public Prescription savePrescription(Prescription prescription) { return prescriptionRepository.save(prescription); }
    public Optional<Prescription> findById(Long id) { return prescriptionRepository.findById(id); }
    public List<Prescription> getPrescriptionsByPatient(Patient patient) { return prescriptionRepository.findByPatient(patient); }
    public List<Prescription> getPrescriptionsByDoctor(Doctor doctor) { return prescriptionRepository.findByDoctor(doctor); }
    public void deletePrescription(Long id) { prescriptionRepository.deleteById(id); }
}
