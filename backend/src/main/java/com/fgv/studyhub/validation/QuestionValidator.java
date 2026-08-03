package com.fgv.studyhub.validation;
import com.fgv.studyhub.dto.AiQuestionDTO;
import com.fgv.studyhub.entity.Question;
import com.fgv.studyhub.exception.AiParsingException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class QuestionValidator {
 private static final Pattern PLACEHOLDER=Pattern.compile("(?i)^(?:alternativa\\s*)?[a-e](?:[).:\\-])?$");
 private static final Pattern QUESTION_DIRECTIVE=Pattern.compile("(?i).*(\\?|\\b(?:assinale|indique|marque|julgue|qual\\s+(?:é|das)|é\\s+correto\\s+afirmar|é\\s+incorreto\\s+afirmar|a\\s+alternativa\\s+correta)\\b).*");
 private static final Pattern META_OPTION=Pattern.compile("(?i).*(?:\\b(?:opção|alternativa)\\s+[a-e]\\b|outra\\s+(?:opção|alternativa)|não\\s+aborda\\s+o\\s+que|descreve\\s+(?:exatamente\\s+)?o\\s+que\\s+foi|conceito\\s+relacionado).*" );

 public void validate(AiQuestionDTO q){
  if(q==null||q.statement()==null||q.statement().trim().length()<20) fail("statement must contain a complete question");
  if(!QUESTION_DIRECTIVE.matcher(q.statement().trim()).matches())fail("statement must ask an objective question or request the correct alternative");
  validateOptions(q.options());
  if(q.correctIndex()<0||q.correctIndex()>4) fail("correctIndex must be between 0 and 4");
  if(q.explanation()==null||q.explanation().trim().length()<100) fail("explanation must justify all alternatives");
  long explanationLines=q.explanation().lines().filter(line->!line.isBlank()).count();
  if(explanationLines<5) fail("explanation must separate the five alternatives with line breaks");
  long copiedOptions=q.options().stream().filter(option->q.statement().toLowerCase(Locale.ROOT).contains(option.trim().toLowerCase(Locale.ROOT))).count();
  if(copiedOptions>=3) fail("statement must not contain a duplicated list of alternatives");
 }

 public boolean isUsable(Question question){
  try {
   validate(new AiQuestionDTO(question.getStatement(),question.getOptions(),question.getCorrectIndex(),question.getExplanation()));
   return true;
  } catch (AiParsingException exception) {
   return false;
  }
 }

 private void validateOptions(java.util.List<String> options){
  if(options==null||options.size()!=5)fail("exactly five options are required");
  if(options.stream().anyMatch(option->option==null||option.isBlank()||option.trim().length()<8||PLACEHOLDER.matcher(option.trim()).matches()||META_OPTION.matcher(option.trim()).matches()))fail("options must contain substantive answers without meta commentary");
  long distinct=options.stream().map(option->option.trim().toLowerCase(Locale.ROOT)).distinct().count();
  if(distinct!=5)fail("the five options must be different");
 }

 private void fail(String message){ throw new AiParsingException("Invalid AI question: "+message); }
}
