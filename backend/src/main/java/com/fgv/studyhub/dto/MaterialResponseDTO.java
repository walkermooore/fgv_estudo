package com.fgv.studyhub.dto;
import com.fgv.studyhub.entity.*;
import java.time.Instant;
public record MaterialResponseDTO(Long id, String title, String description, MaterialType type, String fileName, String originalUrl, long size, MaterialStatus status, String failureReason, Instant uploadedAt, Instant processedAt, long chunkCount, long tokenCount) {}
