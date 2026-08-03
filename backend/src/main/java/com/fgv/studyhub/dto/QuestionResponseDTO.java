package com.fgv.studyhub.dto;
import java.util.List;
public record QuestionResponseDTO(Long id, String topic, String statement, List<String> options, Integer correctIndex, String explanation) {}
