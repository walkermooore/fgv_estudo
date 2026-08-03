package com.fgv.studyhub.service;

import com.fgv.studyhub.dto.AnswerHistoryPageDTO;
import com.fgv.studyhub.entity.AnswerHistory;
import com.fgv.studyhub.entity.Question;
import com.fgv.studyhub.exception.BadRequestException;
import com.fgv.studyhub.mapper.AnswerHistoryMapper;
import com.fgv.studyhub.repository.AnswerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerHistoryService {
    private final AnswerHistoryRepository repository;
    private final AnswerHistoryMapper mapper;

    @Transactional
    public void record(Question question, int selectedAnswer, boolean correct) {
        repository.save(AnswerHistory.builder()
                .question(question)
                .selectedAnswer(selectedAnswer)
                .correct(correct)
                .build());
    }

    @Transactional(readOnly = true)
    public AnswerHistoryPageDTO list(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        var result = repository.findAllByOrderByAnsweredAtDesc(PageRequest.of(page, size));
        return new AnswerHistoryPageDTO(
                result.getContent().stream().map(mapper::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }
}
