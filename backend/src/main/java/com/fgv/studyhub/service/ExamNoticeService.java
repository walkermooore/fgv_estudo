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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class ExamNoticeService {
    private final ExamNoticeRepository notices;
    private final MaterialService materials;
    private final ExamNoticeProcessingService processing;
    private final ExamNoticeMapper mapper;
    private final ConcurrentMap<Long, CompletableFuture<Void>> activeTasks = new ConcurrentHashMap<>();

    public ExamNoticeResponseDTO upload(MultipartFile file, String title) {
        var materialResponse = materials.upload(file, title, "Edital enviado pela área de editais");
        var notice = notices.save(ExamNotice.builder()
                .material(materials.entity(materialResponse.id()))
                .status(ExamNoticeStatus.PROCESSING)
                .build());
        startProcessing(notice.getId());
        return mapper.toResponse(notice);
    }

    public ExamNoticeResponseDTO retry(Long id) {
        var notice = entity(id);
        if (notice.getStatus() == ExamNoticeStatus.PROCESSING) throw new BadRequestException("The exam notice is already being analyzed");
        notice.setStatus(ExamNoticeStatus.PROCESSING);
        notice.setProcessedBatches(0);
        notice.setTotalBatches(0);
        notice.setAnalysisJson(null);
        notice.setFailureReason(null);
        notice.setProcessedAt(null);
        notices.save(notice);
        startProcessing(id);
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
        var task = activeTasks.remove(id);
        if (task != null) task.cancel(true);
        Long materialId = notice.getMaterial().getId();
        notices.delete(notice);
        notices.flush();
        materials.delete(materialId);
    }

    private ExamNotice entity(Long id) {
        return notices.findById(id).orElseThrow(() -> new NotFoundException("Exam notice " + id + " not found"));
    }

    private void startProcessing(Long id) {
        CompletableFuture<Void> task = processing.process(id);
        activeTasks.put(id, task);
        task.whenComplete((ignored, error) -> activeTasks.remove(id));
    }
}
