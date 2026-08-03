package com.fgv.studyhub.validation;
import com.fgv.studyhub.dto.AiQuestionDTO;
import com.fgv.studyhub.exception.AiParsingException;
import org.springframework.stereotype.Component;
@Component
public class QuestionValidator {
 public void validate(AiQuestionDTO q){
  if(q==null||q.statement()==null||q.statement().isBlank()) fail("statement is required");
  if(q.options()==null||q.options().size()!=5||q.options().stream().anyMatch(o->o==null||o.isBlank())) fail("exactly five non-empty options are required");
  if(q.correctIndex()<0||q.correctIndex()>4) fail("correctIndex must be between 0 and 4");
  if(q.explanation()==null||q.explanation().isBlank()) fail("explanation is required");
 }
 private void fail(String message){ throw new AiParsingException("Invalid AI question: "+message); }
}
