package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity @Table(name="study_material") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyMaterial {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=500) private String title;
    @Lob @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(columnDefinition="text") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private MaterialType type;
    @Column(name="file_name", length=500) private String fileName;
    @Column(name="stored_path", length=1000) private String storedPath;
    @Column(name="original_url", length=2048) private String originalUrl;
    @Column(name="size_bytes", nullable=false) private long size;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private MaterialStatus status;
    @Column(name="failure_reason", length=1000) private String failureReason;
    @Column(name="uploaded_at", nullable=false) private Instant uploadedAt;
    @Column(name="processed_at") private Instant processedAt;
    @PrePersist void prePersist(){ if(uploadedAt==null) uploadedAt=Instant.now(); if(status==null) status=MaterialStatus.UPLOADING; }
}
