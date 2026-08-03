package com.fgv.studyhub.repository;

import com.fgv.studyhub.entity.AnswerHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Long> {
    @EntityGraph(attributePaths = {"question", "question.topic"})
    Page<AnswerHistory> findAllByOrderByAnsweredAtDesc(Pageable pageable);
}
