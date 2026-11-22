package com.mahi.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.ObjectMapper;

import com.mahi.demo.service.EligibilityService;
import com.mahi.demo.service.PdfService;
import com.mahi.demo.service.TextChunkService;
import com.mahi.demo.service.DocumentChunkService;
import com.mahi.demo.service.ProfileExtractionService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final EligibilityService eligibilityService;
    private final PdfService pdfService;
    private final TextChunkService textChunkService;
    private final DocumentChunkService documentChunkService;
    private final ProfileExtractionService profileExtractionService;
    private final ObjectMapper objectMapper;

    public RecommendationController(
            EligibilityService eligibilityService,
            PdfService pdfService,
            TextChunkService textChunkService,
            DocumentChunkService documentChunkService,
            ProfileExtractionService profileExtractionService,
            ObjectMapper objectMapper) {

        this.eligibilityService = eligibilityService;
        this.pdfService = pdfService;
        this.textChunkService = textChunkService;
        this.documentChunkService = documentChunkService;
        this.profileExtractionService = profileExtractionService;
        this.objectMapper = objectMapper;
    }

    // PDF UPLOAD ANALYSIS

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzePdf(
            @RequestParam("file") MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Please upload a PDF file."));
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null ||
                    !originalFileName.toLowerCase().endsWith(".pdf")) {

                return ResponseEntity.badRequest().body(
                        Map.of("error", "Only PDF files are supported."));
            }

            String extractedText = pdfService.extractText(file);

            if (extractedText == null || extractedText.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No readable text found in PDF."));
            }

            List<String> chunks =
                    textChunkService.splitText(extractedText);

            if (chunks == null || chunks.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Unable to extract PDF text."));
            }

            String documentId = UUID.randomUUID().toString();

            String storedFileName =
                    documentId + "_" + originalFileName;

            documentChunkService.saveChunks(storedFileName, chunks);

            String profileJson =
                    profileExtractionService.extractProfile(storedFileName);

            if (profileJson == null || profileJson.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Profile extraction failed."));
            }

            List<Map<String, String>> recommendations =
                    eligibilityService.findEligibleSchemes(profileJson);

            Object extractedProfile =
                    objectMapper.readValue(profileJson, Object.class);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "documentId", documentId,
                            "fileName", originalFileName,
                            "profile", extractedProfile,
                            "totalRecommendations", recommendations.size(),
                            "recommendations", recommendations
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "error", "Unable to analyze PDF: " + e.getMessage()
                    ));
        }
    }

    // MANUAL FORM ANALYSIS

    @PostMapping(
            value = "/form",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analyzeForm(
            @RequestBody Map<String, Object> profile) {

        try {

            if (profile == null || profile.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "error", "Please fill your personal information."
                        ));
            }

            if (isBlank(profile.get("age"))
                    || isBlank(profile.get("state"))
                    || isBlank(profile.get("occupation"))
                    || isBlank(profile.get("annualFamilyIncome"))) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "error", "Age, state, occupation and annual family income are required."
                        ));
            }

            String profileJson =
                    objectMapper.writeValueAsString(profile);

            List<Map<String, String>> recommendations =
                    eligibilityService.findEligibleSchemes(profileJson);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "profile", profile,
                            "totalRecommendations", recommendations.size(),
                            "recommendations", recommendations
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "error", "Unable to analyze profile: " + e.getMessage()
                    ));
        }
    }

    private boolean isBlank(Object value) {
        return value == null || value.toString().isBlank();
    }
}