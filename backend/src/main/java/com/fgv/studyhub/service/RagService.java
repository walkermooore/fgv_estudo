package com.fgv.studyhub.service;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.exception.*;
import com.fgv.studyhub.rag.*;
import com.fgv.studyhub.repository.StudyChunkRepository;
import com.fgv.studyhub.validation.QuestionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class RagService {
 private final SemanticSearchService semantic; private final AiGateway ai; private final MaterialService materials; private final StudyChunkRepository chunks; private final JsonResponseParser parser; private final QuestionValidator questionValidator;
 public StudyQueryResponseDTO query(String question){long start=System.nanoTime();var sources=semantic.search(question);String answer=sources.isEmpty()?"Esse assunto não foi encontrado na sua biblioteca.":ai.chat(RagPrompts.grounded("Responda à pergunta: "+question,sources));return new StudyQueryResponseDTO(answer,sourceNames(sources),sources,(System.nanoTime()-start)/1_000_000);}
 public MarkdownResponseDTO summarize(SummaryRequestDTO r){Long id=r.materialId();if(id==null){if(r.url()==null||r.url().isBlank())throw new BadRequestException("materialId or url is required");id=materials.addUrl(new UrlMaterialRequestDTO(r.url(),null,"Added for summary")).id();}var sources=allSources(id);String task="Create a %s summary, organized in Markdown.".formatted(r.type());return new MarkdownResponseDTO(ai.chat(RagPrompts.grounded(task,sources)),sources);}
 public MarkdownResponseDTO explain(ExplainRequestDTO r){materials.entity(r.materialId());var sources=semantic.search(r.topic(),r.materialId(),10);String teacher="Explain the topic \"%s\" in teacher mode. Include all sections: beginner explanation, intermediate explanation, advanced explanation, examples, analogy, real cases, common mistakes, FGV exam traps, and final summary.".formatted(r.topic());return new MarkdownResponseDTO(ai.chat(RagPrompts.grounded(teacher,sources)),sources);}
 public MarkdownResponseDTO materialMarkdown(Long id,String task){var sources=allSources(id);return new MarkdownResponseDTO(ai.chat(RagPrompts.grounded(task,sources)),sources);}
 public FlashcardResponseDTO flashcards(Long id){var sources=allSources(id);String task="Generate useful flashcards. Return only valid JSON in this shape: {\"flashcards\":[{\"question\":\"\",\"answer\":\"\",\"difficulty\":\"BEGINNER|INTERMEDIATE|ADVANCED\",\"category\":\"\"}]}";var parsed=parser.parseFirstObject(ai.chat(RagPrompts.grounded(task,sources)),FlashcardsJson.class);if(parsed.flashcards()==null)throw new AiParsingException("Invalid flashcard response");return new FlashcardResponseDTO(parsed.flashcards(),sources);}
 public MaterialQuestionsResponseDTO questions(MaterialQuestionRequestDTO r){materials.entity(r.materialId());String subject=r.topic()==null||r.topic().isBlank()?"all relevant content":r.topic();var sources=semantic.search(subject,r.materialId(),Math.max(10,r.amount()));if(sources.isEmpty())return new MaterialQuestionsResponseDTO(List.of(),sources,"Insufficient content in the material");String task="Generate exactly %d difficult, original FGV-style multiple-choice questions about %s, using only the excerpts. Each explanation must explain all five alternatives and cite the excerpt id. Return only valid JSON: {\"questions\":[{\"statement\":\"\",\"options\":[\"\",\"\",\"\",\"\",\"\"],\"correctIndex\":0,\"explanation\":\"\"}]}".formatted(r.amount(),subject);var parsed=parser.parseFirstObject(ai.chat(RagPrompts.grounded(task,sources)),AiResponseDTO.class);if(parsed.questions()==null||parsed.questions().size()!=r.amount())throw new AiParsingException("AI did not return the requested number of questions");parsed.questions().forEach(questionValidator::validate);return new MaterialQuestionsResponseDTO(parsed.questions(),sources,null);}
 @Transactional(readOnly=true) protected List<SourceChunkDTO> allSources(Long id){var material=materials.entity(id);if(material.getStatus()!=com.fgv.studyhub.entity.MaterialStatus.READY)throw new BadRequestException("Material is not ready");return chunks.findByMaterialIdOrderByChunkIndex(id).stream().limit(40).map(c->new SourceChunkDTO(c.getId(),id,material.getTitle(),c.getChapter(),c.getContent(),1)).toList();}
 private List<String> sourceNames(List<SourceChunkDTO>s){return s.stream().map(SourceChunkDTO::materialTitle).distinct().toList();}
 private record FlashcardsJson(List<FlashcardDTO> flashcards){}
}
