package com.fgv.studyhub.vector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.entity.StudyChunk;
import com.fgv.studyhub.repository.StudyChunkRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CosineSimilarity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;
@Component @ConditionalOnProperty(name="app.vector.provider",havingValue="local",matchIfMissing=true) @RequiredArgsConstructor
public class LocalVectorStore implements VectorStore {
 private final StudyChunkRepository repository; private final ObjectMapper mapper;
 public void index(StudyChunk chunk,List<Double> embedding){}
 public List<VectorMatch> search(List<Double> query,int limit,Long materialId){return repository.findAll().stream().filter(c->materialId==null||c.getMaterial().getId().equals(materialId)).map(c->new VectorMatch(c,cosine(query,read(c)))).sorted(Comparator.comparingDouble(VectorMatch::similarity).reversed()).limit(limit).toList();}
 private List<Double> read(StudyChunk c){try{return mapper.readValue(c.getEmbeddingJson(),new TypeReference<>(){});}catch(Exception e){return List.of();}}
 private double cosine(List<Double>a,List<Double>b){if(a.size()!=b.size()||a.isEmpty())return 0;return CosineSimilarity.between(Embedding.from(toFloats(a)),Embedding.from(toFloats(b)));}
 private List<Float> toFloats(List<Double> values){return values.stream().map(Double::floatValue).toList();}
}
