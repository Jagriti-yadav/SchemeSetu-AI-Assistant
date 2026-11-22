package com.mahi.demo;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mahi.demo.entity.GovernmentScheme;
import com.mahi.demo.repository.GovernmentSchemeRepository;

@Component
public class SchemeDataInitializer implements CommandLineRunner {

    private final GovernmentSchemeRepository schemeRepository;

    public SchemeDataInitializer(
            GovernmentSchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    @Override
    public void run(String... args) {

        List<GovernmentScheme> schemes = List.of(

            createScheme(
                "Pradhan Mantri Awas Yojana - Gramin",
                "A government housing scheme supporting eligible rural households.",
                "Eligibility depends on applicable rural housing criteria, "
                    + "official beneficiary selection and exclusion rules. "
                    + "Verify the current official rules.",
                "Housing assistance subject to applicable scheme rules.",
                "https://pmayg.nic.in/"
            ),

            createScheme(
                "Pradhan Mantri Ujjwala Yojana",
                "A scheme supporting eligible women from poor households "
                    + "with access to LPG connections.",
                "Eligibility depends on the current scheme requirements, "
                    + "household circumstances and applicable exclusions. "
                    + "Verify eligibility through the official portal.",
                "LPG connection benefits subject to current scheme rules.",
                "https://www.pmuy.gov.in/"
            ),

            createScheme(
                "Ayushman Bharat PM-JAY",
                "A government health assurance scheme for eligible beneficiaries.",
                "Eligibility depends on the applicable beneficiary database "
                    + "and current scheme rules. Verify beneficiary status "
                    + "through the official portal.",
                "Cashless hospitalisation benefits subject to applicable rules.",
                "https://pmjay.gov.in/"
            )
        );

        for (GovernmentScheme scheme : schemes) {

            boolean exists = schemeRepository.findAll()
                    .stream()
                    .anyMatch(existing ->
                        existing.getSchemeName()
                                .equalsIgnoreCase(scheme.getSchemeName())
                    );

            if (!exists) {
                schemeRepository.save(scheme);
                System.out.println(
                    "Scheme added: " + scheme.getSchemeName()
                );
            }
        }
    }

    private GovernmentScheme createScheme(
            String name,
            String description,
            String eligibility,
            String benefits,
            String website) {

        GovernmentScheme scheme = new GovernmentScheme();

        scheme.setSchemeName(name);
        scheme.setDescription(description);
        scheme.setEligibility(eligibility);
        scheme.setBenefits(benefits);
        scheme.setOfficialWebsite(website);

        return scheme;
    }
}