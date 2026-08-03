package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record MaterialQuestionRequestDTO(@NotNull Long materialId, String topic, @Min(1) @Max(30) int amount) {}
