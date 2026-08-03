package com.fgv.studyhub.controller;

import com.fgv.studyhub.dto.ExamNoticeResponseDTO;
import com.fgv.studyhub.dto.ProfileTopicsResponseDTO;
import com.fgv.studyhub.service.ExamNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class ExamNoticeController {
    private final ExamNoticeService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExamNoticeResponseDTO upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) String title
    ) {
        return service.upload(file, title);
    }

    @GetMapping
    public List<ExamNoticeResponseDTO> list() {
        return service.list();
    }

    @GetMapping("/topics")
    public ProfileTopicsResponseDTO topics() {
        return service.profileTopics();
    }

    @GetMapping("/{id}")
    public ExamNoticeResponseDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/retry")
    public ExamNoticeResponseDTO retry(@PathVariable Long id) {
        return service.retry(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
