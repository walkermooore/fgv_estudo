package com.fgv.studyhub.controller;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api") @RequiredArgsConstructor @Validated
public class QuizController {
 private final QuestionService service;
 @GetMapping("/quiz") public List<QuestionResponseDTO> quiz(@RequestParam @NotBlank String topic,@RequestParam(defaultValue="5") @Min(1) @Max(50) int amount){return service.getOrGenerateQuestions(topic,amount);}
 @GetMapping("/random") public List<QuestionResponseDTO> random(@RequestParam(defaultValue="20") @Min(1) @Max(100) int amount){return service.random(amount);}
 @PostMapping("/quiz/submit") public QuizSubmissionResponseDTO submit(@RequestBody @Valid List<@Valid AnswerDTO> answers){return service.submit(answers);}
}
