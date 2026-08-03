package com.fgv.studyhub.dto;
import jakarta.validation.constraints.*;
public record UrlMaterialRequestDTO(@NotBlank @Pattern(regexp="https?://.+", message="URL must use HTTP or HTTPS") String url, String title, String description) {}
