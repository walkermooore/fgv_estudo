package com.fgv.studyhub.service;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.entity.*;
import com.fgv.studyhub.exception.*;
import com.fgv.studyhub.mapper.QuestionMapper;
import com.fgv.studyhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class QuestionService {
 private final TopicRepository topics; private final QuestionRepository questions; private final AiGeneratorService generator; private final QuestionMapper mapper;
 @Transactional
 public synchronized List<QuestionResponseDTO> getOrGenerateQuestions(String topicName){return getOrGenerateQuestions(topicName,5);}
 @Transactional
 public synchronized List<QuestionResponseDTO> getOrGenerateQuestions(String topicName,int amount){
  if(topicName==null||topicName.isBlank())throw new BadRequestException("Topic is required"); if(amount<1||amount>50)throw new BadRequestException("Amount must be between 1 and 50");
  String normalized=normalize(topicName); Topic topic=topics.findByNormalizedName(normalized).orElseGet(()->topics.save(Topic.builder().name(cleanName(topicName)).normalizedName(normalized).build()));
  long existing=questions.countByTopicId(topic.getId()); int missing=(int)Math.max(0,amount-existing); if(missing>0){var created=generator.generate(topic.getName(),missing).stream().map(q->mapper.fromAi(q,topic)).toList();questions.saveAll(created);}
  return questions.findByTopicIdOrderByCreatedAtAsc(topic.getId(),PageRequest.of(0,amount)).stream().map(q->mapper.toResponse(q,false)).toList();
 }
 @Transactional(readOnly=true) public List<QuestionResponseDTO> random(int amount){if(amount<1||amount>100)throw new BadRequestException("Amount must be between 1 and 100");return questions.findRandom(amount).stream().map(q->mapper.toResponse(q,false)).toList();}
 @Transactional(readOnly=true) public QuizSubmissionResponseDTO submit(List<AnswerDTO> answers){
  if(answers==null||answers.isEmpty())throw new BadRequestException("At least one answer is required"); var ids=answers.stream().map(AnswerDTO::id).toList();var found=questions.findAllById(ids).stream().collect(java.util.stream.Collectors.toMap(Question::getId,q->q));
  var results=answers.stream().map(a->{var q=Optional.ofNullable(found.get(a.id())).orElseThrow(()->new NotFoundException("Question "+a.id()+" not found"));boolean correct=a.answer()==q.getCorrectIndex();return new AnswerResultDTO(q.getId(),a.answer(),q.getCorrectIndex(),correct,q.getExplanation());}).toList();int score=(int)results.stream().filter(AnswerResultDTO::correct).count();return new QuizSubmissionResponseDTO(score,Math.round(score*10000.0/results.size())/100.0,score,results.size()-score,results);
 }
 private String normalize(String value){return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");}
 private String cleanName(String value){return value.trim().replaceAll("\\s+"," ");}
}
