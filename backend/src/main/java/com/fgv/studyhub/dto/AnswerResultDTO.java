package com.fgv.studyhub.dto;
public record AnswerResultDTO(Long questionId, int answer, int correctIndex, boolean correct, String explanation) {}
