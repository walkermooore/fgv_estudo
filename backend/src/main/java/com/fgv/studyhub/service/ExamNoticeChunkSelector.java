package com.fgv.studyhub.service;

import com.fgv.studyhub.entity.StudyChunk;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;

@Component
public class ExamNoticeChunkSelector {
    private static final int MAX_CHUNKS = 12;
    private static final Map<String, Integer> KEYWORDS = Map.ofEntries(
            Map.entry("cronograma", 8), Map.entry("inscricao", 7), Map.entry("data da prova", 8),
            Map.entry("taxa", 5), Map.entry("isencao", 5), Map.entry("vagas", 6),
            Map.entry("remuneracao", 6), Map.entry("salario", 6), Map.entry("requisitos", 5),
            Map.entry("etapas", 5), Map.entry("prova objetiva", 5), Map.entry("prova discursiva", 5),
            Map.entry("validade", 4), Map.entry("resultado", 4), Map.entry("convocacao", 4),
            Map.entry("jornada", 4), Map.entry("cargo", 3), Map.entry("banca", 4)
    );

    public List<StudyChunk> select(List<StudyChunk> chunks) {
        if (chunks.size() <= MAX_CHUNKS) return List.copyOf(chunks);

        LinkedHashSet<StudyChunk> selected = new LinkedHashSet<>();
        chunks.stream().limit(3).forEach(selected::add);
        chunks.stream()
                .sorted(Comparator.comparingInt(this::score).reversed().thenComparingInt(StudyChunk::getChunkIndex))
                .filter(chunk -> score(chunk) > 0)
                .limit(MAX_CHUNKS - selected.size())
                .forEach(selected::add);

        if (selected.size() < MAX_CHUNKS) {
            chunks.stream().filter(chunk -> !isProgramContent(chunk)).limit(MAX_CHUNKS - selected.size()).forEach(selected::add);
        }
        return selected.stream().sorted(Comparator.comparingInt(StudyChunk::getChunkIndex)).toList();
    }

    private int score(StudyChunk chunk) {
        if (isProgramContent(chunk)) return 0;
        String text = normalize((chunk.getChapter() == null ? "" : chunk.getChapter()) + " " + chunk.getContent());
        return KEYWORDS.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private boolean isProgramContent(StudyChunk chunk) {
        return normalize(chunk.getChapter()).contains("conteudo programatico");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
