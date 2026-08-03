package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record QuestionGenerationDTO(@NotBlank String topicName, @Min(1) @Max(50) int amount) {}
