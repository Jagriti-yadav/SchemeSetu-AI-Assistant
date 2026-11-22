
package com.mahi.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mahi.demo.service.RagService;

@RestController
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/api/ai/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @RequestParam String question) {

        String answer = ragService.askQuestion(question);

        return ResponseEntity.ok(Map.of(
                "question", question,
                "answer", answer
        ));
    }
}

