package com.fgv.studyhub.dto;
import java.util.List;
public record FlashcardResponseDTO(List<FlashcardDTO> flashcards, List<SourceChunkDTO> sources) {}
