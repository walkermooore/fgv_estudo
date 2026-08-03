package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "exam_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private StudyMaterial material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExamNoticeStatus status;

    @Column(name = "processed_batches", nullable = false)
    private int processedBatches;

    @Column(name = "total_batches", nullable = false)
    private int totalBatches;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "analysis_json", columnDefinition = "text")
    private String analysisJson;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = ExamNoticeStatus.PROCESSING;
    }
}
