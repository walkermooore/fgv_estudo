package com.fgv.studyhub.dto;

import java.util.List;

public record ExamNoticeAnalysisDTO(
        String organization,
        String examiningBoard,
        String position,
        String summary,
        List<DateItem> dates,
        List<ContentTopic> contents,
        List<UsefulInformation> usefulInformation
) {
    public record DateItem(String label, String date, String details) {}

    public record ContentTopic(String topic, List<Subtopic> subtopics) {}

    public record Subtopic(String name, List<String> keywords) {}

    public record UsefulInformation(String category, String title, String details) {}
}
