package com.fgv.studyhub.service;
import com.fgv.studyhub.entity.*;
import com.fgv.studyhub.mapper.QuestionMapper;
import com.fgv.studyhub.repository.*;
import com.fgv.studyhub.validation.QuestionValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {
 @Mock TopicRepository topics; @Mock QuestionRepository questions; @Mock AiGeneratorService generator; @Mock AnswerHistoryService history;
 QuestionService service;
 @BeforeEach void setUp(){service=new QuestionService(topics,questions,generator,new QuestionMapper(),history,new QuestionValidator());}
 @Test void returnsCachedQuestionsWithoutCallingAi(){var topic=Topic.builder().id(1L).name("Java").normalizedName("java").build();var cached=new ArrayList<Question>();for(long id=1;id<=5;id++)cached.add(question(id,topic));when(topics.findByNormalizedName("java")).thenReturn(Optional.of(topic));when(questions.findByTopicIdOrderByCreatedAtAsc(1L)).thenReturn(cached);when(questions.countByTopicId(1L)).thenReturn(5L);when(questions.findByTopicIdOrderByCreatedAtAsc(eq(1L),any(Pageable.class))).thenReturn(cached);var result=service.getOrGenerateQuestions("  JAVA  ",5);assertThat(result).hasSize(5);verifyNoInteractions(generator);verify(questions,never()).saveAll(any());}
 @Test void recordsSubmittedAnswerInHistory(){var topic=Topic.builder().id(1L).name("Java").normalizedName("java").build();var question=question(10L,topic);when(questions.findAllById(List.of(10L))).thenReturn(List.of(question));var result=service.submit(List.of(new com.fgv.studyhub.dto.AnswerDTO(10L,2)));assertThat(result.score()).isZero();assertThat(result.wrongQuestions()).isEqualTo(1);verify(history).record(question,2,false);}
 private Question question(long id,Topic topic){return Question.builder().id(id).topic(topic).statement("Which statement correctly answers complete question number "+id+"?").options(List.of("Dependency injection is handled by the application container","Every component creates all dependencies with direct constructors","The database engine replaces the application dependency container","The HTTP protocol automatically instantiates domain services","The Java compiler downloads all runtime service implementations")).correctIndex(0).explanation("A) Correct because it satisfies the premise in full.\nB) Incorrect because it contradicts the first condition.\nC) Incorrect because it ignores the stated constraint.\nD) Incorrect because it describes another concept entirely.\nE) Incorrect because its conclusion does not follow from the premise.").createdAt(Instant.now()).build();}
}
