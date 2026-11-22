
package com.mahi.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mahi.demo.entity.DocumentChunk;
import com.mahi.demo.repository.DocumentChunkRepository;

@RestController
public class DocumentController {

    private final DocumentChunkRepository documentChunkRepository;

    public DocumentController(
            DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    // 1. Get all saved document chunks
    @GetMapping("/api/documents/chunks")
    public List<DocumentChunk> getAllChunks() {
        return documentChunkRepository.findAll();
    }

    // 2. Get a specific chunk by ID
    @GetMapping("/api/documents/chunks/{id}")
    public ResponseEntity<DocumentChunk> getChunkById(
            @PathVariable Long id) {

        return documentChunkRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Search document chunks by keyword
    @GetMapping("/api/documents/search")
    public List<DocumentChunk> searchChunks(
            @RequestParam String keyword) {

        return documentChunkRepository
                .findByContentContainingIgnoreCase(keyword);
    }

    // 4. Get all chunks belonging to a particular PDF
    @GetMapping("/api/documents/by-filename")
    public List<DocumentChunk> getChunksByFileName(
            @RequestParam String fileName) {

        return documentChunkRepository.findByFileName(fileName);
    }

    // 5. Delete a specific chunk by ID
    @DeleteMapping("/api/documents/chunks/{id}")
    public ResponseEntity<String> deleteChunk(
            @PathVariable Long id) {

        if (!documentChunkRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        documentChunkRepository.deleteById(id);

        return ResponseEntity.ok(
                "Document chunk deleted successfully.");
    }

    // 6. Delete all chunks belonging to a particular PDF
    @DeleteMapping("/api/documents/delete-by-filename")
    public ResponseEntity<String> deleteChunksByFileName(
            @RequestParam String fileName) {

        List<DocumentChunk> chunks =
                documentChunkRepository.findByFileName(fileName);

        if (chunks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        documentChunkRepository.deleteByFileName(fileName);

        return ResponseEntity.ok(
                "All chunks for file '" + fileName
                        + "' deleted successfully.");
    }
}

