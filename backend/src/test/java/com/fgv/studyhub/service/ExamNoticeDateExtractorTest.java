package com.fgv.studyhub.service;

import com.fgv.studyhub.entity.StudyChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExamNoticeDateExtractorTest {
    private final ExamNoticeDateExtractor extractor = new ExamNoticeDateExtractor();

    @Test
    void extractsAndLabelsDatesFromTheWholeNotice() {
        var chunks = List.of(
                StudyChunk.builder().content("As inscrições ocorrerão de 1º de setembro de 2026 até 30 de setembro de 2026.").build(),
                StudyChunk.builder().content("A prova objetiva será aplicada em 15/11/2026 e o resultado será publicado em 20.12.2026.").build()
        );

        var result = extractor.extract(chunks);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(item -> item.date()).contains("1º de setembro de 2026", "15/11/2026", "20.12.2026");
        assertThat(result).extracting(item -> item.label()).contains("Inscrições", "Prova");
    }
}
