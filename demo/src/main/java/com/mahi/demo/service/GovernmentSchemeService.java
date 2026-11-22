
package com.mahi.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mahi.demo.entity.GovernmentScheme;
import com.mahi.demo.repository.GovernmentSchemeRepository;

@Service
@Transactional
public class GovernmentSchemeService {

    private final GovernmentSchemeRepository schemeRepository;

    public GovernmentSchemeService(GovernmentSchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    // Get all schemes
    public List<GovernmentScheme> getAllSchemes() {
        return schemeRepository.findAll();
    }

    // Get scheme by ID
    public Optional<GovernmentScheme> getSchemeById(Long id) {
        return schemeRepository.findById(id);
    }

    // Search schemes by name, description, or eligibility
    public List<GovernmentScheme> searchSchemes(String keyword) {
        List<GovernmentScheme> results =
                schemeRepository.findBySchemeNameContainingIgnoreCase(keyword);

        for (GovernmentScheme scheme :
                schemeRepository.findByDescriptionContainingIgnoreCase(keyword)) {
            if (results.stream().noneMatch(s -> s.getId().equals(scheme.getId()))) {
                results.add(scheme);
            }
        }

        for (GovernmentScheme scheme :
                schemeRepository.findByEligibilityContainingIgnoreCase(keyword)) {
            if (results.stream().noneMatch(s -> s.getId().equals(scheme.getId()))) {
                results.add(scheme);
            }
        }

        return results;
    }

    // Add a new scheme
    public GovernmentScheme addScheme(GovernmentScheme scheme) {
        return schemeRepository.save(scheme);
    }

    // Update an existing scheme
    public Optional<GovernmentScheme> updateScheme(
            Long id, GovernmentScheme updatedScheme) {

        return schemeRepository.findById(id).map(existingScheme -> {
            existingScheme.setSchemeName(updatedScheme.getSchemeName());
            existingScheme.setDescription(updatedScheme.getDescription());
            existingScheme.setEligibility(updatedScheme.getEligibility());
            existingScheme.setBenefits(updatedScheme.getBenefits());
            existingScheme.setOfficialWebsite(updatedScheme.getOfficialWebsite());

            return schemeRepository.save(existingScheme);
        });
    }

    // Delete a scheme
    public boolean deleteScheme(Long id) {
        if (!schemeRepository.existsById(id)) {
            return false;
        }

        schemeRepository.deleteById(id);
        return true;
    }
}

