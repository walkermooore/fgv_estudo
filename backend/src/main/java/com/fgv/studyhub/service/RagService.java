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
 private static final int MAX_SUMMARY_SOURCES=12;
 private final SemanticSearchService semantic; private final AiGateway ai; private final MaterialService materials; private final StudyChunkRepository chunks; private final JsonResponseParser parser; private final QuestionValidator questionValidator; private final NotebookLmSummaryService notebookLm;
 public StudyQueryResponseDTO query(String question){long start=System.nanoTime();var sources=semantic.search(question);String answer=sources.isEmpty()?"Esse assunto não foi encontrado na sua biblioteca.":ai.chat(RagPrompts.grounded("Responda à pergunta: "+question,sources));return new StudyQueryResponseDTO(answer,sourceNames(sources),sources,(System.nanoTime()-start)/1_000_000);}
 public MarkdownResponseDTO summarize(SummaryRequestDTO request){
  var materialIds=summaryMaterialIds(request);
  var customRequest=request.request()==null?"":request.request().trim();
  var sources=customRequest.isBlank()?summarySources(materialIds):relevantSummarySources(materialIds,customRequest);
  if(sources.isEmpty())return new MarkdownResponseDTO("Esse assunto não foi encontrado na sua biblioteca.",List.of());
  String task="Crie um resumo do tipo %s, organizado em Markdown.%s Não acrescente informações externas e deixe claro quando algum ponto solicitado não estiver nos documentos.".formatted(request.type(),customRequest.isBlank()?"":" Atenda a este pedido específico do usuário: \""+customRequest+"\".");
  var selectedMaterials=materialIds.stream().map(materials::entity).toList();
  String content=notebookLm.summarize(request,selectedMaterials).orElseGet(()->ai.chat(RagPrompts.grounded(task,sources)));
  return new MarkdownResponseDTO(content,sources);
 }
 public MarkdownResponseDTO explain(ExplainRequestDTO r){materials.entity(r.materialId());var sources=semantic.search(r.topic(),r.materialId(),10);String teacher="Explain the topic \"%s\" in teacher mode. Include all sections: beginner explanation, intermediate explanation, advanced explanation, examples, analogy, real cases, common mistakes, FGV exam traps, and final summary.".formatted(r.topic());return new MarkdownResponseDTO(ai.chat(RagPrompts.grounded(teacher,sources)),sources);}
 public MarkdownResponseDTO materialMarkdown(Long id,String task){var sources=allSources(id);return new MarkdownResponseDTO(ai.chat(RagPrompts.grounded(task,sources)),sources);}
 public FlashcardResponseDTO flashcards(Long id){var sources=allSources(id);String task="Generate useful flashcards. Return only valid JSON in this shape: {\"flashcards\":[{\"question\":\"\",\"answer\":\"\",\"difficulty\":\"BEGINNER|INTERMEDIATE|ADVANCED\",\"category\":\"\"}]}";var parsed=parser.parseFirstObject(ai.chat(RagPrompts.grounded(task,sources)),FlashcardsJson.class);if(parsed.flashcards()==null)throw new AiParsingException("Invalid flashcard response");return new FlashcardResponseDTO(parsed.flashcards(),sources);}
 public MaterialQuestionsResponseDTO questions(MaterialQuestionRequestDTO r){materials.entity(r.materialId());String subject=r.topic()==null||r.topic().isBlank()?"all relevant content":r.topic();var sources=semantic.search(subject,r.materialId(),Math.max(10,r.amount()));if(sources.isEmpty())return new MaterialQuestionsResponseDTO(List.of(),sources,"Insufficient content in the material");String task="Generate exactly %d difficult, original FGV-style multiple-choice questions about %s, using only the excerpts. Each explanation must explain all five alternatives and cite the excerpt id. Return only valid JSON: {\"questions\":[{\"statement\":\"\",\"options\":[\"\",\"\",\"\",\"\",\"\"],\"correctIndex\":0,\"explanation\":\"\"}]}".formatted(r.amount(),subject);var parsed=parser.parseFirstObject(ai.chat(RagPrompts.grounded(task,sources)),AiResponseDTO.class);if(parsed.questions()==null||parsed.questions().size()!=r.amount())throw new AiParsingException("AI did not return the requested number of questions");parsed.questions().forEach(questionValidator::validate);return new MaterialQuestionsResponseDTO(parsed.questions(),sources,null);}
 @Transactional(readOnly=true) protected List<SourceChunkDTO> allSources(Long id){var material=materials.entity(id);if(material.getStatus()!=com.fgv.studyhub.entity.MaterialStatus.READY)throw new BadRequestException("Material is not ready");return chunks.findByMaterialIdOrderByChunkIndex(id).stream().limit(40).map(c->new SourceChunkDTO(c.getId(),id,material.getTitle(),c.getChapter(),c.getContent(),1)).toList();}
 private List<Long> summaryMaterialIds(SummaryRequestDTO request){
  var ids=new LinkedHashSet<Long>();
  if(request.materialId()!=null)ids.add(request.materialId());
  if(request.materialIds()!=null)ids.addAll(request.materialIds());
  if(ids.isEmpty()&&request.url()!=null&&!request.url().isBlank())ids.add(materials.addUrl(new UrlMaterialRequestDTO(request.url(),null,"Added for summary")).id());
  if(ids.isEmpty())throw new BadRequestException("At least one material is required");
  return List.copyOf(ids);
 }
 private List<SourceChunkDTO> summarySources(List<Long> materialIds){int perMaterial=Math.max(1,MAX_SUMMARY_SOURCES/materialIds.size());return materialIds.stream().flatMap(id->distributedSources(id,perMaterial).stream()).limit(MAX_SUMMARY_SOURCES).toList();}
 private List<SourceChunkDTO> relevantSummarySources(List<Long> materialIds,String request){materialIds.forEach(this::ensureReady);int perMaterial=Math.max(2,(int)Math.ceil((double)MAX_SUMMARY_SOURCES/materialIds.size()));return semantic.searchAcrossMaterials(request,materialIds,perMaterial).stream().sorted(Comparator.comparingDouble(SourceChunkDTO::similarity).reversed()).limit(MAX_SUMMARY_SOURCES).toList();}
 private List<SourceChunkDTO> distributedSources(Long id,int limit){var material=materials.entity(id);ensureReady(id);var available=chunks.findByMaterialIdOrderByChunkIndex(id);if(available.isEmpty())return List.of();int amount=Math.min(limit,available.size());var selected=new ArrayList<SourceChunkDTO>();for(int i=0;i<amount;i++){int index=amount==1?0:(int)Math.round((double)i*(available.size()-1)/(amount-1));var chunk=available.get(index);selected.add(new SourceChunkDTO(chunk.getId(),id,material.getTitle(),chunk.getChapter(),chunk.getContent(),1));}return selected;}
 private void ensureReady(Long id){var material=materials.entity(id);if(material.getStatus()!=com.fgv.studyhub.entity.MaterialStatus.READY)throw new BadRequestException("Material is not ready: "+material.getTitle());}
 private List<String> sourceNames(List<SourceChunkDTO>s){return s.stream().map(SourceChunkDTO::materialTitle).distinct().toList();}
 private record FlashcardsJson(List<FlashcardDTO> flashcards){}
}
