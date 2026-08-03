package com.fgv.studyhub.dto;
import java.util.List;
public record QuizSubmissionResponseDTO(int score, double percentage, int correctQuestions, int wrongQuestions, List<AnswerResultDTO> results) {}
