
package com.mahi.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mahi.demo.entity.DocumentChunk;
import com.mahi.demo.repository.DocumentChunkRepository;

@Service
public class RagService {

    private final DocumentChunkRepository documentChunkRepository;
    private final GroqService groqService;

    public RagService(
            DocumentChunkRepository documentChunkRepository,
            GroqService groqService) {
        this.documentChunkRepository = documentChunkRepository;
        this.groqService = groqService;
    }

    public String askQuestion(String question) {

        // Step 1: Find relevant chunks from uploaded documents
        List<DocumentChunk> chunks =
                documentChunkRepository.findByContentContainingIgnoreCase(question);

        // Step 2: If exact phrase search finds nothing, try individual words
        if (chunks.isEmpty()) {
            String[] words = question.split("\\s+");

            for (String word : words) {
                if (word.length() >= 4) {
                    List<DocumentChunk> matches =
                            documentChunkRepository.findByContentContainingIgnoreCase(word);

                    for (DocumentChunk chunk : matches) {
                        boolean alreadyAdded = chunks.stream()
                                .anyMatch(existing ->
                                        existing.getId().equals(chunk.getId()));

                        if (!alreadyAdded) {
                            chunks.add(chunk);
                        }
                    }
                }
            }
        }

        // Step 3: If no relevant information is found, return a message
        if (chunks.isEmpty()) {
            return "Uploaded documents mein is question se related information nahi mili. Please relevant government scheme PDF upload karo.";
        }

        // Step 4: Combine retrieved chunks into context
        String context = chunks.stream()
                .limit(5)
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        // Step 5: Send question and document context to Groq AI
        return groqService.generateAnswer(question, context);
    }
}

