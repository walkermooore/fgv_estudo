package com.fgv.studyhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="question") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="topic_id") private Topic topic;
    @Lob @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(nullable=false, columnDefinition="text") private String statement;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="question_option", joinColumns=@JoinColumn(name="question_id"))
    @OrderColumn(name="option_order") @Column(name="option_text", nullable=false, columnDefinition="text")
    @Builder.Default private List<String> options=new ArrayList<>();
    @Column(name="correct_index", nullable=false) private int correctIndex;
    @Lob @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(nullable=false, columnDefinition="text") private String explanation;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @PrePersist void prePersist(){ if(createdAt==null) createdAt=Instant.now(); }
}
