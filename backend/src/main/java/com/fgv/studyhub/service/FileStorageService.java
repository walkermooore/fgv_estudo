package com.fgv.studyhub.service;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.exception.ExtractionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class FileStorageService {
 private final AppProperties p; private Path root;
 @PostConstruct void init(){try{root=Path.of(p.storage().path()).toAbsolutePath().normalize();Files.createDirectories(root);}catch(IOException e){throw new ExtractionException("Could not initialize upload storage",e);}}
 public Path store(Long materialId,MultipartFile file){String original=Path.of(file.getOriginalFilename()==null?"material":file.getOriginalFilename()).getFileName().toString();Path target=root.resolve(materialId+"-"+UUID.randomUUID()+"-"+original).normalize();if(!target.startsWith(root))throw new ExtractionException("Invalid file name");try(InputStream in=file.getInputStream()){Files.copy(in,target,StandardCopyOption.REPLACE_EXISTING);return target;}catch(IOException e){throw new ExtractionException("Could not store uploaded file",e);}}
 public void delete(Path path){if(path==null)return;try{Files.deleteIfExists(path);}catch(IOException e){throw new ExtractionException("Could not delete stored file",e);}}
}
