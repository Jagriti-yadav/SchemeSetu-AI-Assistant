package com.mahi.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mahi.demo.entity.DocumentChunk;
import com.mahi.demo.repository.DocumentChunkRepository;

@Service
public class DocumentChunkService {

    private final DocumentChunkRepository documentChunkRepository;

    public DocumentChunkService(
            DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public void saveChunks(String fileName, List<String> chunks) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name cannot be empty.");
        }

        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No text chunks available to save.");
        }

        // Remove old chunks for this filename before saving new ones
        documentChunkRepository.deleteByFileName(fileName);

        List<DocumentChunk> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            DocumentChunk document = new DocumentChunk(
                    fileName,
                    i + 1,
                    chunks.get(i)
            );

            documents.add(document);
        }

        documentChunkRepository.saveAll(documents);
    }

    public List<DocumentChunk> getAllChunks() {
        return documentChunkRepository.findAll();
    }
}