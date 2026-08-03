package com.fgv.studyhub.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.dto.AiResponseDTO;
import com.fgv.studyhub.exception.AiParsingException;
import com.fgv.studyhub.rag.JsonResponseParser;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class JsonResponseParserTest {
 private final JsonResponseParser parser=new JsonResponseParser(new ObjectMapper());
 @Test void extractsFirstJsonObjectWithoutStringSlicing(){var result=parser.parseFirstObject("prefix ```json\n{\"questions\":[]}\n``` suffix",AiResponseDTO.class);assertThat(result.questions()).isEmpty();}
 @Test void rejectsResponseWithoutObject(){assertThatThrownBy(()->parser.parseFirstObject("not json",AiResponseDTO.class)).isInstanceOf(AiParsingException.class);}
}
