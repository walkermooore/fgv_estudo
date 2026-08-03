package com.fgv.studyhub.service;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.dto.SourceChunkDTO;
import com.fgv.studyhub.rag.AiGateway;
import com.fgv.studyhub.vector.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class SemanticSearchService {
 private final AiGateway ai; private final VectorStore vectors; private final AppProperties p;
 public List<SourceChunkDTO> search(String question){return search(question,null,p.vector().topK());}
 public List<SourceChunkDTO> search(String question,Long materialId,int limit){return vectors.search(ai.embed(question),limit,materialId).stream().map(this::map).toList();}
 public List<SourceChunkDTO> searchAcrossMaterials(String question,List<Long> materialIds,int limitPerMaterial){var embedding=ai.embed(question);return materialIds.stream().flatMap(materialId->vectors.search(embedding,limitPerMaterial,materialId).stream()).map(this::map).toList();}
 private SourceChunkDTO map(VectorMatch m){var c=m.chunk();return new SourceChunkDTO(c.getId(),c.getMaterial().getId(),c.getMaterial().getTitle(),c.getChapter(),c.getContent(),Math.round(m.similarity()*10000.0)/10000.0);}
}
