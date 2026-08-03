package com.fgv.studyhub.dto;

import java.util.List;

public record ProfileTopicsResponseDTO(
        int profileNumber,
        String profileName,
        List<String> topics
) {}
