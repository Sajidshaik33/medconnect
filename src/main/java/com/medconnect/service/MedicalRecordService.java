package com.medconnect.service;

import com.medconnect.model.MedicalRecord;
import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import com.medconnect.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    public MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord) { return medicalRecordRepository.save(medicalRecord); }
    public Optional<MedicalRecord> findById(Long id) { return medicalRecordRepository.findById(id); }
    public List<MedicalRecord> getMedicalRecordsByPatient(Patient patient) { return medicalRecordRepository.findByPatient(patient); }
    public List<MedicalRecord> getMedicalRecordsByDoctor(Doctor doctor) { return medicalRecordRepository.findByDoctor(doctor); }
    public void deleteMedicalRecord(Long id) { medicalRecordRepository.deleteById(id); }
}
