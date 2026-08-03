package com.fgv.studyhub.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.entity.*;
import com.fgv.studyhub.exception.*;
import com.fgv.studyhub.rag.AiGateway;
import com.fgv.studyhub.repository.*;
import com.fgv.studyhub.vector.VectorStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.time.Instant;
@Service @RequiredArgsConstructor
public class MaterialProcessingService {
 private final StudyMaterialRepository materials; private final StudyChunkRepository chunks; private final TextExtractionService extraction; private final TextChunker chunker; private final AiGateway ai; private final ObjectMapper mapper; private final VectorStore vectors;
 public StudyMaterial processFile(StudyMaterial material,Path path){material.setStatus(MaterialStatus.PROCESSING);materials.save(material);try{return process(material,extraction.fromFile(path,material.getType()));}catch(RuntimeException e){fail(material,e);throw e;}}
 public StudyMaterial processText(StudyMaterial material,String text){material.setStatus(MaterialStatus.PROCESSING);materials.save(material);try{return process(material,text);}catch(RuntimeException e){fail(material,e);throw e;}}
 private StudyMaterial process(StudyMaterial material,String text){var parts=chunker.split(text);if(parts.isEmpty())throw new ExtractionException("No chunks could be created");for(var part:parts){var embedding=ai.embed(part.content());try{var chunk=chunks.save(StudyChunk.builder().material(material).chunkIndex(part.index()).chapter(part.chapter()).content(part.content()).tokenCount(part.tokenCount()).embeddingJson(mapper.writeValueAsString(embedding)).build());vectors.index(chunk,embedding);}catch(Exception e){throw new AiServiceException("Failed to persist an embedding",e);}}material.setStatus(MaterialStatus.READY);material.setProcessedAt(Instant.now());material.setFailureReason(null);return materials.save(material);}
 private void fail(StudyMaterial material,RuntimeException e){chunks.deleteByMaterialId(material.getId());material.setStatus(MaterialStatus.FAILED);material.setFailureReason(e.getMessage()==null?"Processing failed":e.getMessage().substring(0,Math.min(1000,e.getMessage().length())));materials.save(material);}
}
