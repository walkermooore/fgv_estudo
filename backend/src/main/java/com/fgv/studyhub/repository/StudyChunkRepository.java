package com.fgv.studyhub.repository;
import com.fgv.studyhub.entity.StudyChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
public interface StudyChunkRepository extends JpaRepository<StudyChunk,Long>{
 @EntityGraph(attributePaths="material") List<StudyChunk> findByMaterialIdOrderByChunkIndex(Long materialId);
 @Override @EntityGraph(attributePaths="material") List<StudyChunk> findAll();
 long countByMaterialId(Long materialId);
 @Transactional void deleteByMaterialId(Long materialId);
}
