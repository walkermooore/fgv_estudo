package com.fgv.studyhub.mapper;
import com.fgv.studyhub.dto.MaterialResponseDTO;
import com.fgv.studyhub.entity.StudyMaterial;
import com.fgv.studyhub.repository.StudyChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class MaterialMapper {
 private final StudyChunkRepository chunks;
 public MaterialResponseDTO toResponse(StudyMaterial m){ var list=chunks.findByMaterialIdOrderByChunkIndex(m.getId()); long tokens=list.stream().mapToLong(c->c.getTokenCount()).sum(); return new MaterialResponseDTO(m.getId(),m.getTitle(),m.getDescription(),m.getType(),m.getFileName(),m.getOriginalUrl(),m.getSize(),m.getStatus(),m.getFailureReason(),m.getUploadedAt(),m.getProcessedAt(),list.size(),tokens); }
}
