package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record ExplainRequestDTO(@NotNull Long materialId, @NotBlank String topic) {}
