package com.mahi.demo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.mahi.demo.entity.GovernmentScheme;
import com.mahi.demo.repository.GovernmentSchemeRepository;

@Service
public class EligibilityService {

    private final GovernmentSchemeRepository schemeRepository;
    private final GroqService groqService;

    public EligibilityService(
            GovernmentSchemeRepository schemeRepository,
            GroqService groqService) {

        this.schemeRepository = schemeRepository;
        this.groqService = groqService;
    }

    public List<Map<String, String>> findEligibleSchemes(String profileJson) {

        if (profileJson == null || profileJson.isBlank()) {
            throw new IllegalArgumentException(
                    "User profile information is required.");
        }

        List<GovernmentScheme> schemes = schemeRepository.findAll();

        List<Map<String, String>> recommendations = new ArrayList<>();

        for (GovernmentScheme scheme : schemes) {

            String schemeName = safe(scheme.getSchemeName());

            String schemeInformation = """
                    Scheme Name: %s

                    Description:
                    %s

                    Eligibility Rules:
                    %s

                    Benefits:
                    %s
                    """.formatted(
                    schemeName,
                    safe(scheme.getDescription()),
                    safe(scheme.getEligibility()),
                    safe(scheme.getBenefits()));

            String prompt = """
                    You are an expert Indian government welfare scheme
                    eligibility assessment assistant.

                    USER PROFILE:
                    %s

                    IMPORTANT USER DETAILS:

                    Agricultural land ownership:
                    Read the field hasAgriculturalLand.

                    Farmer status:
                    Read the field isFarmer.

                    BPL status:
                    Read the field isBpl.

                    Existing LPG connection:
                    Read the field hasExistingLpgConnection.

                    Ayushman beneficiary status:
                    Read the field ayushmanBeneficiaryStatus.

                    Gender:
                    Read the field gender.

                    Rural or urban area:
                    Read the field area.

                    Annual family income:
                    Read the field annualFamilyIncome.

                    GOVERNMENT SCHEME:
                    %s

                    YOUR TASK:

                    Evaluate this particular scheme independently.

                    SCHEME-SPECIFIC CHECKS:

                    1. PM-KISAN:
                    - Check agricultural land ownership.
                    - If hasAgriculturalLand is false, do not mark eligible.
                    - If land ownership is unknown, request more information.
                    - Do not assume that every farmer qualifies.
                    - Consider applicable exclusion conditions mentioned
                      in the supplied scheme eligibility rules.

                    2. PRADHAN MANTRI UJJWALA YOJANA:
                    - Check whether the applicant is a woman.
                    - Check the eligible poor household/BPL information
                      using isBpl.
                    - Check existing LPG connection using
                      hasExistingLpgConnection.
                    - If an existing LPG connection is present,
                      do not mark eligible.
                    - If an important condition is unknown,
                      request more information.
                    - Follow the supplied scheme rules.

                    3. AYUSHMAN BHARAT PM-JAY:
                    - Check ayushmanBeneficiaryStatus.
                    - If the applicant says they have verified their
                      beneficiary status, this can support a preliminary
                      assessment.
                    - If status is unknown, request beneficiary verification.
                    - Do not claim that government database verification
                      has been performed.
                    - Do not guarantee approval.

                    4. PRADHAN MANTRI AWAS YOJANA - GRAMIN:
                    - Check rural household status.
                    - Check housing condition if information is available.
                    - Consider applicable exclusion conditions from
                      the supplied eligibility rules.
                    - Do not assume eligibility from low income alone.

                    GENERAL RULES:

                    1. Evaluate each scheme independently.

                    2. Use the supplied scheme eligibility rules.

                    3. Use the additional profile fields provided above.

                    4. If all known mandatory conditions are satisfied,
                       return ELIGIBLE as a preliminary assessment.

                    5. If a known fact contradicts a mandatory condition,
                       return NOT_ELIGIBLE.

                    6. If an important mandatory condition is unknown,
                       return NEEDS_MORE_INFORMATION.

                    7. Do not invent missing information.

                    8. Do not introduce conditions that are absent from
                       the supplied scheme rules.

                    9. Correct obvious OCR spelling errors in state names.

                    10. Give a short, understandable reason.

                    RETURN ONLY VALID JSON:

                    {
                      "status": "NEEDS_MORE_INFORMATION",
                      "reason": "Explain the assessment",
                      "missingInformation": "Mention missing details or None"
                    }

                    Allowed status values:

                    ELIGIBLE
                    NOT_ELIGIBLE
                    NEEDS_MORE_INFORMATION

                    Do not use Markdown code fences.
                    Do not return any text outside JSON.
                    """.formatted(profileJson, schemeInformation);

            String result = groqService.generateAnswer(
                    prompt,
                    "Return only valid JSON with status, reason and missingInformation.");

            String status = normalizeStatus(result);
            String reason = extractJsonField(result, "reason");

            String missingInformation =
                    extractJsonField(result, "missingInformation");

            System.out.println(
                    "Scheme: " + schemeName
                    + " | Status: " + status
                    + " | Reason: " + reason
                    + " | Missing: " + missingInformation);

            // Do not show explicitly ineligible schemes
            if ("NOT_ELIGIBLE".equals(status)) {
                continue;
            }

            Map<String, String> recommendation = new LinkedHashMap<>();

            recommendation.put("schemeName", schemeName);
            recommendation.put("status", status);
            recommendation.put("reason", reason);
            recommendation.put("missingInformation", missingInformation);

            recommendation.put(
                    "description",
                    safe(scheme.getDescription()));

            recommendation.put(
                    "benefits",
                    safe(scheme.getBenefits()));

            recommendation.put(
                    "eligibility",
                    safe(scheme.getEligibility()));

            recommendation.put(
                    "officialWebsite",
                    safe(scheme.getOfficialWebsite()));

            recommendation.put(
                    "notice",
                    "This is a preliminary assessment, not official approval. "
                    + "Please verify eligibility on the official scheme portal.");

            recommendation.put(
                    "nextStep",
                    "Check the official scheme eligibility requirements "
                    + "before applying.");

            recommendations.add(recommendation);
        }

        return recommendations;
    }

    // NORMALIZE AI STATUS

    private String normalizeStatus(String result) {

        if (result == null || result.isBlank()) {
            return "NEEDS_MORE_INFORMATION";
        }

        String cleaned = result.toUpperCase();

        Matcher matcher = Pattern.compile(
                "\"status\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE
        ).matcher(cleaned);

        if (matcher.find()) {

            String status = matcher.group(1).trim();

            if (status.equals("NOT_ELIGIBLE")) {
                return "NOT_ELIGIBLE";
            }

            if (status.equals("ELIGIBLE")) {
                return "ELIGIBLE";
            }

            if (status.equals("NEEDS_MORE_INFORMATION")) {
                return "NEEDS_MORE_INFORMATION";
            }
        }

        return "NEEDS_MORE_INFORMATION";
    }

    // EXTRACT JSON FIELDS SAFELY

    private String extractJsonField(String result, String fieldName) {

        if (result == null || result.isBlank()) {
            return "More information is required.";
        }

        try {

            String cleaned = result.trim();

            if (cleaned.startsWith("```")) {
                cleaned = cleaned
                        .replaceFirst("^```(?:json)?\\s*", "")
                        .replaceFirst("\\s*```$", "")
                        .trim();
            }

            String regex = "\"" + Pattern.quote(fieldName)
                    + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"";

            Matcher matcher = Pattern.compile(
                    regex,
                    Pattern.CASE_INSENSITIVE
            ).matcher(cleaned);

            if (matcher.find()) {

                return matcher.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\n", " ")
                        .replace("\\\\", "\\")
                        .trim();
            }

        } catch (Exception e) {

            System.err.println(
                    "Could not extract field: " + fieldName);
        }

        return "Not provided.";
    }

    // SAFE NULL HANDLING

    private String safe(String value) {
        return value == null ? "" : value;
    }
}