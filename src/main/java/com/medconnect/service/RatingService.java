package com.medconnect.service;

import com.medconnect.model.Doctor;
import com.medconnect.model.Patient;
import com.medconnect.model.Rating;
import com.medconnect.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    public Rating saveRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    public List<Rating> getRatingsByDoctor(Doctor doctor) {
        return ratingRepository.findByDoctor(doctor);
    }

    public List<Rating> getRatingsByPatient(Patient patient) {
        return ratingRepository.findByPatient(patient);
    }

    public Optional<Rating> getRatingByDoctorAndPatient(
            Doctor doctor, Patient patient) {
        return ratingRepository.findByDoctorAndPatient(doctor, patient);
    }

    public double getAverageRating(Doctor doctor) {
        List<Rating> ratings = ratingRepository.findByDoctor(doctor);
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);
    }

    public void deleteRating(Long id) {
        ratingRepository.deleteById(id);
    }
}