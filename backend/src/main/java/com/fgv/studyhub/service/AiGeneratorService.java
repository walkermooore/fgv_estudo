package com.fgv.studyhub.service;
import com.fgv.studyhub.dto.*;
import com.fgv.studyhub.exception.AiParsingException;
import com.fgv.studyhub.rag.*;
import com.fgv.studyhub.validation.QuestionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class AiGeneratorService {
 private final AiGateway ai; private final JsonResponseParser parser; private final QuestionValidator validator;
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
  AiResponseDTO response=parser.parseFirstObject(ai.chat(prompt),AiResponseDTO.class); if(response.questions()==null||response.questions().size()!=amount)throw new AiParsingException("AI must return exactly "+amount+" questions");response.questions().forEach(validator::validate);return response.questions();
 }
}
