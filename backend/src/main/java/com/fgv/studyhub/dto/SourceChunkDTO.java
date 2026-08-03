package com.fgv.studyhub.dto;
public record SourceChunkDTO(Long chunkId, Long materialId, String materialTitle, String chapter, String content, double similarity) {}
