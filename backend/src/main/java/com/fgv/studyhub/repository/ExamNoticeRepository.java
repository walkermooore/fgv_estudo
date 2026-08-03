package com.fgv.studyhub.repository;

import com.fgv.studyhub.entity.ExamNotice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamNoticeRepository extends JpaRepository<ExamNotice, Long> {
    @EntityGraph(attributePaths = "material")
    List<ExamNotice> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "material")
    java.util.Optional<ExamNotice> findById(Long id);
}
