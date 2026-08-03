package com.fgv.studyhub.validation;
import com.fgv.studyhub.entity.MaterialType;
import com.fgv.studyhub.exception.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@Component
public class MaterialValidator {
 private static final long MAX=25L*1024*1024;
 private static final Map<String,MaterialType> TYPES=Map.of("pdf",MaterialType.PDF,"docx",MaterialType.DOCX,"txt",MaterialType.TXT,"md",MaterialType.MARKDOWN,"markdown",MaterialType.MARKDOWN,"csv",MaterialType.CSV,"html",MaterialType.HTML,"htm",MaterialType.HTML);
 public MaterialType validate(MultipartFile file){ if(file==null||file.isEmpty()) throw new BadRequestException("The file is empty"); if(file.getSize()>MAX) throw new PayloadTooLargeException("Maximum file size is 25 MB"); String name=Optional.ofNullable(file.getOriginalFilename()).orElse(""); int dot=name.lastIndexOf('.'); String ext=dot<0?"":name.substring(dot+1).toLowerCase(Locale.ROOT); MaterialType type=TYPES.get(ext); if(type==null) throw new UnsupportedMediaException("Supported formats: PDF, DOCX, TXT, Markdown, CSV and HTML"); return type; }
}
