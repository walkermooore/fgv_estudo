package com.fgv.studyhub.dto;
import java.util.List;
public record MarkdownResponseDTO(String content, List<SourceChunkDTO> sources) {}
