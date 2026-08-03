package com.fgv.studyhub.service;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.exception.AiParsingException;
import com.fgv.studyhub.rag.*;
import com.fgv.studyhub.validation.QuestionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
@Service @RequiredArgsConstructor @Slf4j
public class AiGeneratorService {
 private final AiGateway ai; private final JsonResponseParser parser; private final QuestionValidator validator; private final AppProperties properties;
 public List<AiQuestionDTO> generate(String topicName,int amount){
  String prompt="""
Atue como um examinador da banca FGV (Fundação Getulio Vargas) para concursos de TI.

Gere %d questões difíceis, inéditas e de múltipla escolha sobre o tema \"%s\".

Retorne EXATAMENTE UM JSON válido contendo apenas:

{
  \"questions\":[
    {
      \"statement\":\"\",
      \"options\":[
        \"\",
        \"\",
        \"\",
        \"\",
        \"\"
      ],
      \"correctIndex\":0,
      \"explanation\":\"\"
    }
  ]
}

A explanation deve obrigatoriamente explicar:

- por que a alternativa correta está correta;

- por que cada uma das outras quatro alternativas está incorreta;

- utilizar quebras de linha (\\n) para separar as explicações.

Não escreva nenhum texto antes nem depois do JSON.""".formatted(amount,topicName);
  AiParsingException lastFailure=null;
  for(int attempt=1;attempt<=2;attempt++){
   try{
    AiResponseDTO response=parser.parseFirstObject(ai.chatQuestions(prompt,properties.ai().quizModel(),amount),AiResponseDTO.class);
    if(response.questions()==null||response.questions().size()!=amount)throw new AiParsingException("AI must return exactly "+amount+" questions");
    var normalized=response.questions().stream().map(this::normalizeFormatting).toList();
    normalized.forEach(validator::validate);
    return normalized;
   }catch(AiParsingException exception){lastFailure=exception;log.warn("Question generation attempt {} returned invalid content: {}",attempt,exception.getMessage());}
  }
  throw new AiParsingException("A IA local não conseguiu produzir questões válidas após duas tentativas",lastFailure);
 }
 private AiQuestionDTO normalizeFormatting(AiQuestionDTO question){
  if(question==null||question.explanation()==null)return question;
  String explanation=question.explanation().replace("\\n","\n").replaceAll("(?i)\\s+(?=(?:alternativa\\s*)?[A-E1-5][).:\\-]\\s*)","\n").trim();
  return new AiQuestionDTO(question.statement(),question.options(),question.correctIndex(),explanation);
 }
}
