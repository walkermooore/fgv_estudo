package com.fgv.studyhub.service;

import com.fgv.studyhub.dto.ExamNoticeAnalysisDTO;
import com.fgv.studyhub.entity.StudyChunk;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExamNoticeContentParser {
    private static final Pattern HEADING = Pattern.compile(
            "([A-ZÁÀÂÃÉÊÍÓÔÕÚÜÇ][A-ZÁÀÂÃÉÊÍÓÔÕÚÜÇ0-9 /(),.\\-–]{3,120}):");
    private static final Pattern NUMBERED_ITEM = Pattern.compile(
            "(?:^|\\s)(\\d{1,2}(?:\\.\\d{1,2})*)\\s+(.+?)(?=(?:\\s+\\d{1,2}(?:\\.\\d{1,2})*\\s+[A-ZÁÀÂÃÉÊÍÓÔÕÚÜÇ])|$)");
    private static final Pattern FIRST_MODULE = Pattern.compile("(?iu)M[ÓO]DULO\\s+[IVX]+\\b");
    private static final Pattern PROFILE_MARKER = Pattern.compile("(?iu)\\bPERFIL\\s+(\\d{1,2})\\s*:");

    public List<ExamNoticeAnalysisDTO.ContentTopic> parse(List<StudyChunk> chunks) {
        String text = programText(chunks);
        return parseText(text);
    }

    public List<ExamNoticeAnalysisDTO.ContentTopic> parseProfile(List<StudyChunk> chunks, int profileNumber) {
        String text = programText(chunks);
        return parseText(profileText(text, profileNumber));
    }

    private List<ExamNoticeAnalysisDTO.ContentTopic> parseText(String text) {
        if (text.isBlank()) return List.of();

        List<HeadingMatch> headings = headings(text);
        LinkedHashMap<String, TopicAccumulator> topics = new LinkedHashMap<>();
        String scope = "";

        for (int index = 0; index < headings.size(); index++) {
            HeadingMatch heading = headings.get(index);
            String topicHeading = heading.name();
            if (isStructural(heading.name())) {
                StructuralHeading structural = splitStructuralHeading(heading.name());
                scope = structural.scope();
                if (structural.topic().isBlank()) continue;
                topicHeading = structural.topic();
            }
            if (isNoise(topicHeading)) continue;
            int end = index + 1 < headings.size() ? headings.get(index + 1).start() : text.length();
            String segment = text.substring(heading.end(), end);
            List<ExamNoticeAnalysisDTO.Subtopic> subtopics = subtopics(segment);
            if (subtopics.isEmpty()) continue;

            String topicName = scope.isBlank() ? clean(topicHeading) : scope + " — " + clean(topicHeading);
            topics.computeIfAbsent(normalize(topicName), ignored -> new TopicAccumulator(topicName)).add(subtopics);
        }
        return topics.values().stream().map(TopicAccumulator::build).toList();
    }

    private String profileText(String text, int profileNumber) {
        Matcher matcher = PROFILE_MARKER.matcher(text);
        int start = -1;
        int end = text.length();
        while (matcher.find()) {
            int currentProfile = Integer.parseInt(matcher.group(1));
            if (start < 0 && currentProfile == profileNumber) {
                start = matcher.start();
            } else if (start >= 0) {
                end = matcher.start();
                break;
            }
        }
        return start < 0 ? "" : text.substring(start, end);
    }

    private String programText(List<StudyChunk> chunks) {
        int start = -1;
        int end = chunks.size();
        for (int index = 0; index < chunks.size(); index++) {
            StudyChunk chunk = chunks.get(index);
            String searchable = normalize((chunk.getChapter() == null ? "" : chunk.getChapter()) + " " + chunk.getContent());
            if (start < 0 && searchable.contains("conteudo programatico")) {
                start = index;
                continue;
            }
            if (start >= 0 && isProgramEnd(chunk)) {
                end = index;
                break;
            }
        }
        if (start < 0) return "";
        String text = chunks.subList(start, end).stream().map(StudyChunk::getContent).reduce((a, b) -> a + " " + b).orElse("")
                .replaceAll("\\s+", " ").trim();
        Matcher firstModule = FIRST_MODULE.matcher(text);
        return firstModule.find() ? text.substring(firstModule.start()) : text;
    }

    private boolean isProgramEnd(StudyChunk chunk) {
        String text = normalize(chunk.getContent());
        String chapter = normalize(chunk.getChapter());
        return text.matches(".*anexo\\s+(ii|2)\\b.*")
                || (text.contains("cargo:") && text.contains("requisitos:"))
                || (chapter.startsWith("cargo:") && !chapter.contains("conteudo programatico"));
    }

    private List<HeadingMatch> headings(String text) {
        List<HeadingMatch> result = new ArrayList<>();
        Matcher matcher = HEADING.matcher(text);
        while (matcher.find()) result.add(new HeadingMatch(matcher.group(1), matcher.start(), matcher.end()));
        return result;
    }

    private boolean isStructural(String value) {
        String normalized = normalize(value);
        return normalized.startsWith("modulo") || normalized.startsWith("perfil")
                || normalized.startsWith("cargo") || normalized.startsWith("anexo")
                || normalized.startsWith("conteudo programatico");
    }

    private boolean isNoise(String value) {
        String cleaned = clean(value);
        return cleaned.matches("(?i)^O?\\s*20\\d{2}.*") || (cleaned.length() < 8 && cleaned.endsWith(")"));
    }

    private StructuralHeading splitStructuralHeading(String value) {
        String cleaned = clean(value);
        if (normalize(cleaned).startsWith("modulo")) {
            int closeParenthesis = cleaned.lastIndexOf(')');
            if (closeParenthesis > 0 && closeParenthesis + 1 < cleaned.length()) {
                return new StructuralHeading(cleaned.substring(0, closeParenthesis + 1).trim(), cleaned.substring(closeParenthesis + 1).trim());
            }
        }
        return new StructuralHeading(cleaned, "");
    }

    private List<ExamNoticeAnalysisDTO.Subtopic> subtopics(String segment) {
        LinkedHashMap<String, SubtopicAccumulator> values = new LinkedHashMap<>();
        Matcher matcher = NUMBERED_ITEM.matcher(segment);
        SubtopicAccumulator current = null;
        while (matcher.find()) {
            String number = matcher.group(1);
            String description = concise(matcher.group(2));
            if (description.isBlank()) continue;
            if (!number.contains(".")) {
                current = values.computeIfAbsent(normalize(description), ignored -> new SubtopicAccumulator(description));
                current.addKeyword(description);
            } else if (current != null) {
                current.addKeyword(description);
            }
        }

        if (values.isEmpty()) {
            Arrays.stream(segment.split("[.;]"))
                    .map(this::concise)
                    .filter(value -> value.length() >= 4)
                    .limit(20)
                    .forEach(value -> values.computeIfAbsent(normalize(value), ignored -> new SubtopicAccumulator(value)).addKeyword(value));
        }
        return values.values().stream().map(SubtopicAccumulator::build).toList();
    }

    private String concise(String value) {
        String cleaned = value.replaceAll("\\s+", " ").trim().replaceAll("^[,;:. -]+|[,;:. -]+$", "");
        int sentence = cleaned.indexOf('.');
        if (sentence > 3) cleaned = cleaned.substring(0, sentence);
        return cleaned.length() > 160 ? cleaned.substring(0, 157).trim() + "..." : cleaned;
    }

    private String clean(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record HeadingMatch(String name, int start, int end) {}
    private record StructuralHeading(String scope, String topic) {}

    private static final class TopicAccumulator {
        private final String name;
        private final LinkedHashMap<String, SubtopicAccumulator> subtopics = new LinkedHashMap<>();

        private TopicAccumulator(String name) { this.name = name; }

        void add(List<ExamNoticeAnalysisDTO.Subtopic> values) {
            values.forEach(value -> subtopics.computeIfAbsent(value.name().toLowerCase(Locale.ROOT), ignored -> new SubtopicAccumulator(value.name())).addKeywords(value.keywords()));
        }

        ExamNoticeAnalysisDTO.ContentTopic build() {
            return new ExamNoticeAnalysisDTO.ContentTopic(name, subtopics.values().stream().map(SubtopicAccumulator::build).toList());
        }
    }

    private static final class SubtopicAccumulator {
        private final String name;
        private final LinkedHashMap<String, String> keywords = new LinkedHashMap<>();

        private SubtopicAccumulator(String name) { this.name = name; }

        void addKeywords(List<String> values) { values.forEach(this::addKeyword); }

        void addKeyword(String value) {
            String keyword = value.length() > 80 ? value.substring(0, 77).trim() + "..." : value;
            keywords.putIfAbsent(keyword.toLowerCase(Locale.ROOT), keyword);
        }

        ExamNoticeAnalysisDTO.Subtopic build() {
            return new ExamNoticeAnalysisDTO.Subtopic(name, List.copyOf(keywords.values()));
        }
    }
}
