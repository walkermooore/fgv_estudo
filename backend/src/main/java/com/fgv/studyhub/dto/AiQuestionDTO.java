package com.fgv.studyhub.dto;
import java.util.List;
public record AiQuestionDTO(String statement, List<String> options, int correctIndex, String explanation) {}
