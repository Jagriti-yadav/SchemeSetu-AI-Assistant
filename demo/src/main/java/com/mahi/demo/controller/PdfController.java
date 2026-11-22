
package com.mahi.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mahi.demo.entity.DocumentChunk;
import com.mahi.demo.service.DocumentChunkService;
import com.mahi.demo.service.PdfService;
import com.mahi.demo.service.TextChunkService;

@RestController
public class PdfController {

    private final PdfService pdfService;
    private final TextChunkService textChunkService;
    private final DocumentChunkService documentChunkService;

    public PdfController(
            PdfService pdfService,
            TextChunkService textChunkService,
            DocumentChunkService documentChunkService) {

        this.pdfService = pdfService;
        this.textChunkService = textChunkService;
        this.documentChunkService = documentChunkService;
    }

    @PostMapping("/api/documents/upload")
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Step 1: Extract text from PDF
            String extractedText = pdfService.extractText(file);

            // Step 2: Divide text into chunks
            List<String> chunks = textChunkService.splitText(extractedText);

            // Step 3: Save chunks into MySQL
            if (!chunks.isEmpty()) {
                documentChunkService.saveChunks(
                        file.getOriginalFilename(),
                        chunks
                );
            }

            // Step 4: Prepare response
            response.put("message", "PDF uploaded and chunks saved successfully!");
            response.put("fileName", file.getOriginalFilename());
            response.put("totalCharacters", extractedText.length());
            response.put("totalChunks", chunks.size());
            response.put(
                    "chunkPreview",
                    chunks.subList(0, Math.min(3, chunks.size()))
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            response.put("error", "Unable to read the PDF file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    
}

