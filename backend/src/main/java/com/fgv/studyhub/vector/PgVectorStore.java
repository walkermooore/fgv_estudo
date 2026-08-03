package com.fgv.studyhub.vector;
import com.fgv.studyhub.entity.StudyChunk;
import com.fgv.studyhub.repository.StudyChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;
@Component @ConditionalOnProperty(name="app.vector.provider",havingValue="pgvector") @RequiredArgsConstructor
public class PgVectorStore implements VectorStore {
 private final JdbcTemplate jdbc; private final StudyChunkRepository chunks;
 public void index(StudyChunk chunk,List<Double> embedding){jdbc.update("update study_chunk set embedding=cast(? as vector) where id=?",literal(embedding),chunk.getId());}
 public List<VectorMatch> search(List<Double> embedding,int limit,Long materialId){String sql="select id, 1-(embedding <=> cast(? as vector)) similarity from study_chunk where embedding is not null"+(materialId==null?"":" and material_id=?")+" order by embedding <=> cast(? as vector) limit ?";Object[] args=materialId==null?new Object[]{literal(embedding),literal(embedding),limit}:new Object[]{literal(embedding),materialId,literal(embedding),limit};return jdbc.query(sql,(rs,n)->new VectorMatch(chunks.findById(rs.getLong("id")).orElseThrow(),rs.getDouble("similarity")),args);}
 private String literal(List<Double> values){return values.toString();}
}
