
package com.mahi.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.mahi.demo.entity.GovernmentScheme;
import com.mahi.demo.service.GovernmentSchemeService;

@RestController
@RequestMapping("/api/schemes")
public class GovernmentSchemeController {

    private final GovernmentSchemeService schemeService;

    public GovernmentSchemeController(GovernmentSchemeService schemeService) {
        this.schemeService = schemeService;
    }

    // 1. Get all government schemes
    @GetMapping
    public List<GovernmentScheme> getAllSchemes() {
        return schemeService.getAllSchemes();
    }

    // 2. Get a scheme by ID
    @GetMapping("/{id}")
    public ResponseEntity<GovernmentScheme> getSchemeById(
            @PathVariable Long id) {

        return schemeService.getSchemeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Search schemes
    @GetMapping("/search")
    public List<GovernmentScheme> searchSchemes(
            @RequestParam String keyword) {

        return schemeService.searchSchemes(keyword);
    }

    // 4. Add a new scheme
    @PostMapping
    public ResponseEntity<GovernmentScheme> addScheme(
            @Valid @RequestBody GovernmentScheme scheme) {

        GovernmentScheme savedScheme = schemeService.addScheme(scheme);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedScheme);
    }

    // 5. Update an existing scheme
    @PutMapping("/{id}")
    public ResponseEntity<GovernmentScheme> updateScheme(
            @PathVariable Long id,
            @Valid @RequestBody GovernmentScheme updatedScheme) {

        return schemeService.updateScheme(id, updatedScheme)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. Delete a scheme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable Long id) {

        boolean deleted = schemeService.deleteScheme(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}

