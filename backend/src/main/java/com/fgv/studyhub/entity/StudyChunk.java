package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity @Table(name="study_chunk") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyChunk {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="material_id") private StudyMaterial material;
    @Column(name="chunk_index", nullable=false) private int chunkIndex;
    @Column(length=500) private String chapter;
    @Lob @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(nullable=false, columnDefinition="text") private String content;
    @Column(name="token_count", nullable=false) private int tokenCount;
    @Lob @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(name="embedding_json", nullable=false, columnDefinition="text") private String embeddingJson;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @PrePersist void prePersist(){ if(createdAt==null) createdAt=Instant.now(); }
}
