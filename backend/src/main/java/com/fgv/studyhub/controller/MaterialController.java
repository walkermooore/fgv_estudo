package com.fgv.studyhub.controller;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController @RequestMapping("/api/materials") @RequiredArgsConstructor
public class MaterialController {
 private final MaterialService materials; private final RagService rag; private final NotebookLmSummaryService notebookLm;
 @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED) public MaterialResponseDTO upload(@RequestPart("file") MultipartFile file,@RequestPart(value="title",required=false) String title,@RequestPart(value="description",required=false) String description){return materials.upload(file,title,description);}
 @PostMapping("/url") @ResponseStatus(HttpStatus.CREATED) public MaterialResponseDTO url(@RequestBody @Valid UrlMaterialRequestDTO request){return materials.addUrl(request);}
 @GetMapping public List<MaterialResponseDTO> list(@RequestParam(required=false) String search){return materials.list(search);}
 @GetMapping("/{id}") public MaterialResponseDTO get(@PathVariable Long id){return materials.get(id);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){materials.delete(id);}
 @PostMapping("/summarize") public MarkdownResponseDTO summarize(@RequestBody @Valid SummaryRequestDTO r){return rag.summarize(r);}
 @GetMapping("/summarize/provider") public NotebookLmStatusDTO summaryProvider(){return notebookLm.status();}
 @PostMapping("/explain") public MarkdownResponseDTO explain(@RequestBody @Valid ExplainRequestDTO r){return rag.explain(r);}
 @PostMapping("/flashcards") public FlashcardResponseDTO flashcards(@RequestBody @Valid MaterialActionRequestDTO r){return rag.flashcards(r.materialId());}
 @PostMapping("/mindmap") public MarkdownResponseDTO mindmap(@RequestBody @Valid MaterialActionRequestDTO r){return rag.materialMarkdown(r.materialId(),"Generate a hierarchical Markdown mind map using nested lists.");}
 @PostMapping("/questions") public MaterialQuestionsResponseDTO questions(@RequestBody @Valid MaterialQuestionRequestDTO r){return rag.questions(r);}
 @PostMapping("/notes") public MarkdownResponseDTO notes(@RequestBody @Valid MaterialActionRequestDTO r){return rag.materialMarkdown(r.materialId(),"Create study notes with: Summary, Important points, Definitions, Keywords, Checklist, and Review tips.");}
}
