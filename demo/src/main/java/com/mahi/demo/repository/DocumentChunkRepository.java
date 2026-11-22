package com.mahi.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mahi.demo.entity.DocumentChunk;

@Repository
public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByFileName(String fileName);

    List<DocumentChunk> findByContentContainingIgnoreCase(String keyword);

    List<DocumentChunk> findByFileNameContainingIgnoreCase(String fileName);

    void deleteByFileName(String fileName);
}