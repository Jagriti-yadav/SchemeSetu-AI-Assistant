
package com.mahi.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAnswer(String question, String context) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String prompt = """
                You are an assistant for Indian government schemes.

                Follow the instructions in the question carefully.
                Use the supplied context as supporting information.
                Do not invent government scheme rules or facts.

                Question and instructions:
                %s

                Context and relevant information:
                %s
                """.formatted(question, context);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(message),
                "temperature", 0.0
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        Map<?, ?> response = restTemplate.postForObject(
                apiUrl,
                request,
                Map.class
        );

        if (response == null || response.get("choices") == null) {
            throw new IllegalStateException(
                    "Groq returned an empty response.");
        }

        List<?> choices = (List<?>) response.get("choices");

        if (choices.isEmpty()) {
            throw new IllegalStateException(
                    "Groq returned no answer.");
        }

        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
        Map<?, ?> responseMessage = (Map<?, ?>) choice.get("message");

        Object content = responseMessage.get("content");

        if (content == null) {
            throw new IllegalStateException(
                    "Groq returned empty answer content.");
        }

        return content.toString().trim();
    }
}

