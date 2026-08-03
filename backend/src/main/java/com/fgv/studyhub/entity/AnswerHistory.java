package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "answer_history", indexes = {
        @Index(name = "idx_answer_history_answered_at", columnList = "answered_at"),
        @Index(name = "idx_answer_history_question", columnList = "question_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_answer", nullable = false)
    private int selectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @PrePersist
    void prePersist() {
        if (answeredAt == null) {
            answeredAt = Instant.now();
        }
    }
}
