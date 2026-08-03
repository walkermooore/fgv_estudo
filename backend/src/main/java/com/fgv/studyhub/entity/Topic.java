package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="topic") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Topic {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String name;
    @Column(name="normalized_name", nullable=false, unique=true) private String normalizedName;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @PrePersist void prePersist(){ if(createdAt==null) createdAt=Instant.now(); }
}
