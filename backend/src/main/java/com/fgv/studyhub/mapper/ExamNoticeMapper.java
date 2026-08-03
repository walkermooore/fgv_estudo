package com.fgv.studyhub.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.dto.ExamNoticeAnalysisDTO;
import com.fgv.studyhub.dto.ExamNoticeResponseDTO;
import com.fgv.studyhub.entity.ExamNotice;
import com.fgv.studyhub.exception.AiParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamNoticeMapper {
    private final ObjectMapper objectMapper;

    public ExamNoticeResponseDTO toResponse(ExamNotice notice) {
        int progress = notice.getTotalBatches() == 0
                ? 0
                : Math.min(100, notice.getProcessedBatches() * 100 / notice.getTotalBatches());
        if (notice.getStatus() == com.fgv.studyhub.entity.ExamNoticeStatus.READY) progress = 100;

        return new ExamNoticeResponseDTO(
                notice.getId(),
                notice.getMaterial().getId(),
                notice.getMaterial().getTitle(),
                notice.getMaterial().getFileName(),
                notice.getStatus(),
                notice.getProcessedBatches(),
                notice.getTotalBatches(),
                progress,
                notice.getFailureReason(),
                notice.getCreatedAt(),
                notice.getProcessedAt(),
                parseAnalysis(notice.getAnalysisJson())
        );
    }

    private ExamNoticeAnalysisDTO parseAnalysis(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, ExamNoticeAnalysisDTO.class);
        } catch (Exception exception) {
            throw new AiParsingException("Stored exam notice analysis is invalid", exception);
        }
    }
}
