package com.fgv.studyhub.dto;

import com.fgv.studyhub.entity.ExamNoticeStatus;

import java.time.Instant;

public record ExamNoticeResponseDTO(
        Long id,
        Long materialId,
        String title,
        String fileName,
        ExamNoticeStatus status,
        int processedBatches,
        int totalBatches,
        int progressPercentage,
        String failureReason,
        Instant createdAt,
        Instant processedAt,
        ExamNoticeAnalysisDTO analysis
) {}
