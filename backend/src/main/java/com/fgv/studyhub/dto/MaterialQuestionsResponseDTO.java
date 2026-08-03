package com.fgv.studyhub.dto;
import java.util.List;
public record MaterialQuestionsResponseDTO(List<AiQuestionDTO> questions, List<SourceChunkDTO> sources, String message) {}
