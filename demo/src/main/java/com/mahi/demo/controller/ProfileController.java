
package com.mahi.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mahi.demo.service.ProfileExtractionService;

@RestController
public class ProfileController {

    private final ProfileExtractionService profileExtractionService;

    public ProfileController(
            ProfileExtractionService profileExtractionService) {
        this.profileExtractionService = profileExtractionService;
    }

    @GetMapping("/api/profile/extract")
    public ResponseEntity<Map<String, String>> extractProfile(
            @RequestParam String fileName) {

        String profile = profileExtractionService.extractProfile(fileName);

        return ResponseEntity.ok(Map.of("profile", profile));
    }
}

