package com.fgv.studyhub.dto;

import java.util.List;

public record AnswerHistoryPageDTO(
        List<AnswerHistoryResponseDTO> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
