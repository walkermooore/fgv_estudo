package com.fgv.studyhub.mapper;

import com.fgv.studyhub.dto.AnswerHistoryResponseDTO;
import com.fgv.studyhub.entity.AnswerHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnswerHistoryMapper {
    public AnswerHistoryResponseDTO toResponse(AnswerHistory history) {
        var question = history.getQuestion();
        return new AnswerHistoryResponseDTO(
                history.getId(),
                question.getId(),
                question.getTopic().getName(),
                question.getStatement(),
                List.copyOf(question.getOptions()),
                history.getSelectedAnswer(),
                question.getCorrectIndex(),
                history.isCorrect(),
                question.getExplanation(),
                history.getAnsweredAt()
        );
    }
}
