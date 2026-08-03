package com.fgv.studyhub.dto;
import jakarta.validation.constraints.NotBlank;
public record StudyQueryRequestDTO(@NotBlank String question) {}
