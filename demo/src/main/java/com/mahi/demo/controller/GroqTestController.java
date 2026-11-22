
package com.mahi.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mahi.demo.service.GroqService;

@RestController
public class GroqTestController {

    private final GroqService groqService;

    public GroqTestController(GroqService groqService) {
        this.groqService = groqService;
    }

    @GetMapping("/api/ai/test")
    public Map<String, String> testGroq(
            @RequestParam(defaultValue = "What is PM Kisan Samman Nidhi?") String question) {

        String answer = groqService.generateAnswer(
                question,
                "PM Kisan Samman Nidhi is a government scheme that provides financial assistance to eligible farmer families in India."
        );

        return Map.of(
                "question", question,
                "answer", answer
        );
    }
}

