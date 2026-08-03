package com.fgv.studyhub.service;

import com.fgv.studyhub.dto.ExamNoticeAnalysisDTO;
import com.fgv.studyhub.entity.StudyChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExamNoticeDateExtractor {
    private static final Pattern DATE = Pattern.compile(
            "(?iu)\\b(?:\\d{1,2}(?:º)?\\s+de\\s+(?:janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)\\s+de\\s+\\d{4}|\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4})\\b");
    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put("inscri", "Inscrições");
        LABELS.put("isenc", "Isenção da taxa");
        LABELS.put("pagamento", "Pagamento");
        LABELS.put("prova", "Prova");
        LABELS.put("resultado", "Resultado");
        LABELS.put("recurso", "Recurso");
        LABELS.put("convoca", "Convocação");
        LABELS.put("publica", "Publicação");
    }

    public List<ExamNoticeAnalysisDTO.DateItem> extract(List<StudyChunk> chunks) {
        LinkedHashMap<String, ExamNoticeAnalysisDTO.DateItem> dates = new LinkedHashMap<>();
        for (StudyChunk chunk : chunks) {
            String content = chunk.getContent().replaceAll("\\s+", " ");
            Matcher matcher = DATE.matcher(content);
            while (matcher.find()) {
                String date = matcher.group().trim();
                String details = context(content, matcher.start(), matcher.end());
                String label = label(details);
                dates.putIfAbsent((label + "|" + date).toLowerCase(Locale.ROOT), new ExamNoticeAnalysisDTO.DateItem(label, date, details));
                if (dates.size() >= 40) return List.copyOf(dates.values());
            }
        }
        return List.copyOf(dates.values());
    }

    private String context(String content, int start, int end) {
        int from = Math.max(0, start - 110);
        int to = Math.min(content.length(), end + 110);
        String value = content.substring(from, to).trim();
        return value.length() > 240 ? value.substring(0, 237).trim() + "..." : value;
    }

    private String label(String details) {
        String normalized = details.toLowerCase(Locale.ROOT);
        return LABELS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Data prevista no edital");
    }
}
