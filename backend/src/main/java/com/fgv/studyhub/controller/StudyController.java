package com.fgv.studyhub.controller;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/study") @RequiredArgsConstructor
public class StudyController {
 private final RagService rag;
 @PostMapping("/query") public StudyQueryResponseDTO query(@RequestBody @Valid StudyQueryRequestDTO request){return rag.query(request.question());}
}
