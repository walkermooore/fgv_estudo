package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record AnswerDTO(@NotNull Long id, @Min(0) @Max(4) int answer) {}
