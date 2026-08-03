package com.fgv.studyhub.dto;

import java.time.Instant;
import java.util.List;

public record AnswerHistoryResponseDTO(
        Long id,
        Long questionId,
        String topic,
        String statement,
        List<String> options,
        int selectedAnswer,
        int correctIndex,
        boolean correct,
        String explanation,
        Instant answeredAt
) {}
