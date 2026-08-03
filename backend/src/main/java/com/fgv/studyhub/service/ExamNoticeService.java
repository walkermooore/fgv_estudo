package com.fgv.studyhub.service;

import com.fgv.studyhub.dto.ExamNoticeResponseDTO;
import com.fgv.studyhub.entity.ExamNotice;
import com.fgv.studyhub.entity.ExamNoticeStatus;
import com.fgv.studyhub.exception.BadRequestException;
import com.fgv.studyhub.exception.NotFoundException;
import com.fgv.studyhub.mapper.ExamNoticeMapper;
import com.fgv.studyhub.repository.ExamNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamNoticeService {
    private final ExamNoticeRepository notices;
    private final MaterialService materials;
    private final ExamNoticeProcessingService processing;
    private final ExamNoticeMapper mapper;

    public ExamNoticeResponseDTO upload(MultipartFile file, String title) {
        var materialResponse = materials.upload(file, title, "Edital enviado pela área de editais");
        var notice = notices.save(ExamNotice.builder()
                .material(materials.entity(materialResponse.id()))
                .status(ExamNoticeStatus.PROCESSING)
                .build());
        processing.process(notice.getId());
        return mapper.toResponse(notice);
    }

    @Transactional(readOnly = true)
    public List<ExamNoticeResponseDTO> list() {
        return notices.findAllByOrderByCreatedAtDesc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExamNoticeResponseDTO get(Long id) {
        return mapper.toResponse(entity(id));
    }

    @Transactional
    public void delete(Long id) {
        var notice = entity(id);
        if (notice.getStatus() == ExamNoticeStatus.PROCESSING) {
            throw new BadRequestException("Wait for the exam notice analysis to finish before deleting it");
        }
        Long materialId = notice.getMaterial().getId();
        notices.delete(notice);
        notices.flush();
        materials.delete(materialId);
    }

    private ExamNotice entity(Long id) {
        return notices.findById(id).orElseThrow(() -> new NotFoundException("Exam notice " + id + " not found"));
    }
}
