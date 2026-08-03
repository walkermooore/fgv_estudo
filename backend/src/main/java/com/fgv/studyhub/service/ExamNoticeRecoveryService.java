package com.fgv.studyhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.dto.ExamNoticeAnalysisDTO;
import com.fgv.studyhub.entity.ExamNoticeStatus;
import com.fgv.studyhub.repository.ExamNoticeRepository;
import com.fgv.studyhub.repository.StudyChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamNoticeRecoveryService {
    private final ExamNoticeRepository notices;
    private final StudyChunkRepository chunks;
    private final ExamNoticeContentParser contentParser;
    private final ExamNoticeDateExtractor dateExtractor;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void markInterruptedAnalyses() {
        notices.findByStatus(ExamNoticeStatus.PROCESSING).forEach(notice -> {
            notice.setStatus(ExamNoticeStatus.FAILED);
            notice.setFailureReason("Análise interrompida. Exclua o edital e envie-o novamente para usar o processamento otimizado.");
            notice.setProcessedAt(Instant.now());
        });
        notices.findByStatus(ExamNoticeStatus.READY).forEach(this::refreshLocalExtraction);
    }

    private void refreshLocalExtraction(com.fgv.studyhub.entity.ExamNotice notice) {
        try {
            var materialChunks = chunks.findByMaterialIdOrderByChunkIndex(notice.getMaterial().getId());
            var current = objectMapper.readValue(notice.getAnalysisJson(), ExamNoticeAnalysisDTO.class);
            var localContents = contentParser.parse(materialChunks);
            var refreshed = new ExamNoticeAnalysisDTO(
                    current.organization(), current.examiningBoard(), current.position(), current.summary(),
                    mergeDates(current.dates(), dateExtractor.extract(materialChunks)),
                    localContents.isEmpty() ? current.contents() : localContents,
                    current.usefulInformation()
            );
            notice.setAnalysisJson(objectMapper.writeValueAsString(refreshed));
        } catch (Exception exception) {
            log.warn("Could not refresh local extraction for exam notice {}", notice.getId(), exception);
        }
    }

    private List<ExamNoticeAnalysisDTO.DateItem> mergeDates(List<ExamNoticeAnalysisDTO.DateItem> first, List<ExamNoticeAnalysisDTO.DateItem> second) {
        LinkedHashMap<String, ExamNoticeAnalysisDTO.DateItem> merged = new LinkedHashMap<>();
        if (first != null) first.forEach(item -> merged.putIfAbsent(dateKey(item), item));
        if (second != null) second.forEach(item -> merged.putIfAbsent(dateKey(item), item));
        return List.copyOf(merged.values());
    }

    private String dateKey(ExamNoticeAnalysisDTO.DateItem item) {
        return ((item.label() == null ? "" : item.label()) + "|" + (item.date() == null ? "" : item.date())).toLowerCase(Locale.ROOT);
    }
}
