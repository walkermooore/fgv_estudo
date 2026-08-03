package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record SummaryRequestDTO(Long materialId, @Pattern(regexp="https?://.+") String url, @NotNull SummaryType type) {
 public enum SummaryType { SHORT, COMPLETE, TECHNICAL, BEGINNER, ADVANCED, MAP, CHECKLIST, TABLE, COMPARISON }
}
