package com.fgv.studyhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.dto.ExamNoticeAnalysisDTO;
import com.fgv.studyhub.entity.ExamNotice;
import com.fgv.studyhub.entity.ExamNoticeStatus;
import com.fgv.studyhub.entity.StudyChunk;
import com.fgv.studyhub.exception.AiServiceException;
import com.fgv.studyhub.exception.NotFoundException;
import com.fgv.studyhub.rag.AiGateway;
import com.fgv.studyhub.rag.ExamNoticePrompt;
import com.fgv.studyhub.rag.JsonResponseParser;
import com.fgv.studyhub.repository.ExamNoticeRepository;
import com.fgv.studyhub.repository.StudyChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamNoticeProcessingService {
    private static final int CHUNKS_PER_BATCH = 6;

    private final ExamNoticeRepository notices;
    private final StudyChunkRepository chunks;
    private final AiGateway ai;
    private final JsonResponseParser parser;
    private final ObjectMapper objectMapper;

    @Async("noticeTaskExecutor")
    public void process(Long noticeId) {
        try {
            ExamNotice notice = find(noticeId);
            List<StudyChunk> materialChunks = chunks.findByMaterialIdOrderByChunkIndex(notice.getMaterial().getId());
            if (materialChunks.isEmpty()) throw new AiServiceException("The exam notice has no processed text");

            List<List<StudyChunk>> batches = partition(materialChunks);
            notice.setTotalBatches(batches.size());
            notice.setProcessedBatches(0);
            notices.save(notice);

            AnalysisAccumulator accumulator = new AnalysisAccumulator();
            for (int index = 0; index < batches.size(); index++) {
                String raw = ai.chat(ExamNoticePrompt.extraction(batches.get(index)));
                accumulator.add(parser.parseFirstObject(raw, ExamNoticeAnalysisDTO.class));
                notice.setProcessedBatches(index + 1);
                notices.save(notice);
            }

            notice.setAnalysisJson(objectMapper.writeValueAsString(accumulator.build()));
            notice.setStatus(ExamNoticeStatus.READY);
            notice.setFailureReason(null);
            notice.setProcessedAt(Instant.now());
            notices.save(notice);
        } catch (Exception exception) {
            fail(noticeId, exception);
        }
    }

    private List<List<StudyChunk>> partition(List<StudyChunk> source) {
        List<List<StudyChunk>> batches = new ArrayList<>();
        for (int start = 0; start < source.size(); start += CHUNKS_PER_BATCH) {
            batches.add(source.subList(start, Math.min(source.size(), start + CHUNKS_PER_BATCH)));
        }
        return batches;
    }

    private ExamNotice find(Long id) {
        return notices.findById(id).orElseThrow(() -> new NotFoundException("Exam notice " + id + " not found"));
    }

    private void fail(Long noticeId, Exception exception) {
        notices.findById(noticeId).ifPresent(notice -> {
            String message = exception.getMessage() == null ? "Failed to analyze the exam notice" : exception.getMessage();
            notice.setStatus(ExamNoticeStatus.FAILED);
            notice.setFailureReason(message.substring(0, Math.min(1000, message.length())));
            notice.setProcessedAt(Instant.now());
            notices.save(notice);
        });
    }

    private static final class AnalysisAccumulator {
        private String organization = "";
        private String examiningBoard = "";
        private String position = "";
        private final LinkedHashMap<String, String> summaries = new LinkedHashMap<>();
        private final LinkedHashMap<String, ExamNoticeAnalysisDTO.DateItem> dates = new LinkedHashMap<>();
        private final LinkedHashMap<String, TopicAccumulator> topics = new LinkedHashMap<>();
        private final LinkedHashMap<String, ExamNoticeAnalysisDTO.UsefulInformation> useful = new LinkedHashMap<>();

        void add(ExamNoticeAnalysisDTO partial) {
            if (partial == null) return;
            organization = firstValue(organization, partial.organization());
            examiningBoard = firstValue(examiningBoard, partial.examiningBoard());
            position = firstValue(position, partial.position());

            addText(summaries, partial.summary());
            safe(partial.dates()).forEach(item -> {
                if (item == null || allBlank(item.label(), item.date(), item.details())) return;
                dates.putIfAbsent(key(item.label(), item.date()), item);
            });
            safe(partial.usefulInformation()).forEach(item -> {
                if (item == null || allBlank(item.category(), item.title(), item.details())) return;
                useful.putIfAbsent(key(item.category(), item.title()), item);
            });
            safe(partial.contents()).forEach(topic -> {
                if (topic == null || blank(topic.topic())) return;
                topics.computeIfAbsent(normalize(topic.topic()), ignored -> new TopicAccumulator(topic.topic()))
                        .add(topic.subtopics());
            });
        }

        ExamNoticeAnalysisDTO build() {
            return new ExamNoticeAnalysisDTO(
                    organization,
                    examiningBoard,
                    position,
                    String.join("\n\n", summaries.values()),
                    List.copyOf(dates.values()),
                    topics.values().stream().map(TopicAccumulator::build).toList(),
                    List.copyOf(useful.values())
            );
        }

        private static final class TopicAccumulator {
            private final String name;
            private final LinkedHashMap<String, SubtopicAccumulator> subtopics = new LinkedHashMap<>();

            private TopicAccumulator(String name) {
                this.name = name.trim();
            }

            void add(List<ExamNoticeAnalysisDTO.Subtopic> values) {
                safe(values).forEach(subtopic -> {
                    if (subtopic == null || blank(subtopic.name())) return;
                    subtopics.computeIfAbsent(normalize(subtopic.name()), ignored -> new SubtopicAccumulator(subtopic.name()))
                            .add(subtopic.keywords());
                });
            }

            ExamNoticeAnalysisDTO.ContentTopic build() {
                return new ExamNoticeAnalysisDTO.ContentTopic(
                        name,
                        subtopics.values().stream().map(SubtopicAccumulator::build).toList()
                );
            }
        }

        private static final class SubtopicAccumulator {
            private final String name;
            private final LinkedHashMap<String, String> keywords = new LinkedHashMap<>();

            private SubtopicAccumulator(String name) {
                this.name = name.trim();
            }

            void add(List<String> values) {
                safe(values).forEach(value -> addText(keywords, value));
            }

            ExamNoticeAnalysisDTO.Subtopic build() {
                return new ExamNoticeAnalysisDTO.Subtopic(name, List.copyOf(keywords.values()));
            }
        }

        private static String firstValue(String current, String candidate) {
            return blank(current) && !blank(candidate) ? candidate.trim() : current;
        }

        private static void addText(Map<String, String> target, String value) {
            if (!blank(value)) target.putIfAbsent(normalize(value), value.trim());
        }

        private static String key(String... values) {
            return Arrays.stream(values).map(AnalysisAccumulator::normalize).reduce((a, b) -> a + "|" + b).orElse("");
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        }

        private static boolean blank(String value) {
            return value == null || value.isBlank();
        }

        private static boolean allBlank(String... values) {
            return Arrays.stream(values).allMatch(AnalysisAccumulator::blank);
        }

        private static <T> List<T> safe(List<T> values) {
            return values == null ? List.of() : values;
        }
    }
}
