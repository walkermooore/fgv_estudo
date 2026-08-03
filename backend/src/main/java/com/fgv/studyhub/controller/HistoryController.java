package com.fgv.studyhub.controller;

import com.fgv.studyhub.dto.AnswerHistoryPageDTO;
import com.fgv.studyhub.service.AnswerHistoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Validated
public class HistoryController {
    private final AnswerHistoryService service;

    @GetMapping
    public AnswerHistoryPageDTO list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.list(page, size);
    }
}
