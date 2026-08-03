package com.fgv.studyhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SummaryRequestDTO(
        Long materialId,
        @Size(max = 10) List<@NotNull Long> materialIds,
        @Pattern(regexp = "https?://.+") String url,
        @NotNull SummaryType type,
        @Size(max = 2000) String request
) {
    public enum SummaryType {
        SHORT, COMPLETE, TECHNICAL, BEGINNER, ADVANCED, MAP, CHECKLIST, TABLE, COMPARISON
    }
}
