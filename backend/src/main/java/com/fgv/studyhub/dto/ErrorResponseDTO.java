package com.fgv.studyhub.dto;
import java.time.Instant;
import java.util.Map;
public record ErrorResponseDTO(Instant timestamp, int status, String error, String message, String path, Map<String,String> validationErrors) {}
