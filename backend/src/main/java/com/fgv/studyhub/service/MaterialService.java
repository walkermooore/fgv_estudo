package com.fgv.studyhub.service;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.entity.*;
import com.fgv.studyhub.exception.NotFoundException;
import com.fgv.studyhub.mapper.MaterialMapper;
import com.fgv.studyhub.repository.*;
import com.fgv.studyhub.validation.MaterialValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.*;
@Service @RequiredArgsConstructor
public class MaterialService {
 private final StudyMaterialRepository materials; private final StudyChunkRepository chunks; private final MaterialValidator validator; private final FileStorageService storage; private final TextExtractionService extraction; private final MaterialProcessingService processing; private final MaterialMapper mapper;
 public MaterialResponseDTO upload(MultipartFile file,String title,String description){MaterialType type=validator.validate(file);String original=Optional.ofNullable(file.getOriginalFilename()).orElse("material");var material=materials.save(StudyMaterial.builder().title(title==null||title.isBlank()?original:title.trim()).description(description).type(type).fileName(original).size(file.getSize()).status(MaterialStatus.UPLOADING).build());Path path=storage.store(material.getId(),file);material.setStoredPath(path.toString());materials.save(material);return mapper.toResponse(processing.processFile(material,path));}
 public MaterialResponseDTO addUrl(UrlMaterialRequestDTO request){var extracted=extraction.fromUrl(request.url());var material=materials.save(StudyMaterial.builder().title(request.title()==null||request.title().isBlank()?extracted.title():request.title().trim()).description(request.description()).type(MaterialType.URL).originalUrl(request.url()).size(extracted.size()).status(MaterialStatus.UPLOADING).build());return mapper.toResponse(processing.processText(material,extracted.text()));}
 @Transactional(readOnly=true) public List<MaterialResponseDTO> list(String query){var list=query==null||query.isBlank()?materials.findAllByOrderByUploadedAtDesc():materials.findByTitleContainingIgnoreCaseOrderByUploadedAtDesc(query.trim());return list.stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public MaterialResponseDTO get(Long id){return mapper.toResponse(entity(id));}
 @Transactional public void delete(Long id){var material=entity(id);chunks.deleteByMaterialId(id);materials.delete(material);if(material.getStoredPath()!=null)storage.delete(Path.of(material.getStoredPath()));}
 public StudyMaterial entity(Long id){return materials.findById(id).orElseThrow(()->new NotFoundException("Material "+id+" not found"));}
}
