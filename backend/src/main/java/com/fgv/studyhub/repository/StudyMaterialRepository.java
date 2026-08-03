package com.fgv.studyhub.repository;
import com.fgv.studyhub.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial,Long>{ List<StudyMaterial> findAllByOrderByUploadedAtDesc(); List<StudyMaterial> findByTitleContainingIgnoreCaseOrderByUploadedAtDesc(String query); }
