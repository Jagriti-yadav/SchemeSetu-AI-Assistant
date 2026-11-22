
package com.mahi.demo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mahi.demo.service.EligibilityService;
import com.mahi.demo.service.ProfileExtractionService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationWorkflowController {

    private final ProfileExtractionService profileExtractionService;
    private final EligibilityService eligibilityService;

    public RecommendationWorkflowController(
            ProfileExtractionService profileExtractionService,
            EligibilityService eligibilityService) {
        this.profileExtractionService = profileExtractionService;
        this.eligibilityService = eligibilityService;
    }

    @PostMapping("/from-document")
    public ResponseEntity<?> recommendFromDocument(
            @RequestParam String fileName) {

        try {
            // 1. Extract user profile from the uploaded PDF
            String profileJson =
                    profileExtractionService.extractProfile(fileName);

            // 2. Find matching government schemes
            List<Map<String, String>> recommendations =
                    eligibilityService.findEligibleSchemes(profileJson);

            // 3. Return profile and recommendations
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("fileName", fileName);
            response.put("profile", profileJson);
            response.put("totalRecommendations", recommendations.size());
            response.put("recommendations", recommendations);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error",
                            "Recommendation failed. Check the application console."
                    ));
        }
    }
}

