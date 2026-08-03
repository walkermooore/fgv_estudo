package com.fgv.studyhub.dto;
import java.util.*;
public record StudyQueryResponseDTO(String answer, List<String> materialsUsed, List<SourceChunkDTO> chunksUsed, long elapsedMillis) {}
