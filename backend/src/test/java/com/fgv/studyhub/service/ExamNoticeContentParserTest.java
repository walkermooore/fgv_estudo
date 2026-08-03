package com.fgv.studyhub.service;

import com.fgv.studyhub.entity.StudyChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExamNoticeContentParserTest {
    private final ExamNoticeContentParser parser = new ExamNoticeContentParser();

    @Test
    void separatesTopicsSubtopicsAndKeywordsWithoutCallingAi() {
        var chunks = List.of(
                chunk(0, "ANEXO I – CONTEÚDO PROGRAMÁTICO", "ANEXO I – CONTEÚDO PROGRAMÁTICO MÓDULO I - CONHECIMENTOS GERAIS (PARA TODOS OS CARGOS/PERFIS) LÍNGUA PORTUGUESA: 1 Compreensão de textos. 2 Emprego da crase."),
                chunk(1, "ANEXO I – CONTEÚDO PROGRAMÁTICO", "PERFIL 1: DESENVOLVIMENTO DE SOFTWARE: JAVA: 1 Orientação a objetos. 1.1 Encapsulamento. 1.2 Polimorfismo. 2 Spring Boot."),
                chunk(2, "CARGO: ANALISTA", "CARGO: ANALISTA DE TI Requisitos: diploma de graduação.")
        );

        var result = parser.parse(chunks);

        assertThat(result).extracting(topic -> topic.topic().toUpperCase())
                .anyMatch(topic -> topic.contains("LÍNGUA PORTUGUESA"))
                .anyMatch(topic -> topic.contains("JAVA"));
        assertThat(result.stream().flatMap(topic -> topic.subtopics().stream()).toList())
                .anyMatch(subtopic -> subtopic.name().contains("Orientação a objetos") && subtopic.keywords().stream().anyMatch(keyword -> keyword.contains("Encapsulamento")));
    }

    private StudyChunk chunk(int index, String chapter, String content) {
        return StudyChunk.builder().chunkIndex(index).chapter(chapter).content(content).build();
    }
}
