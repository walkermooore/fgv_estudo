package com.fgv.studyhub.mapper;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.entity.*;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class QuestionMapper {
 public QuestionResponseDTO toResponse(Question q, boolean reveal){ return new QuestionResponseDTO(q.getId(),q.getTopic().getName(),q.getStatement(),List.copyOf(q.getOptions()),reveal?q.getCorrectIndex():null,reveal?q.getExplanation():null); }
 public Question fromAi(AiQuestionDTO dto, Topic topic){ return Question.builder().topic(topic).statement(dto.statement().trim()).options(dto.options().stream().map(String::trim).toList()).correctIndex(dto.correctIndex()).explanation(dto.explanation().trim()).build(); }
}
