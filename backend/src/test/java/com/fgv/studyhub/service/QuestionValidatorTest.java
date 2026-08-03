package com.fgv.studyhub.service;

import com.fgv.studyhub.dto.AiQuestionDTO;
import com.fgv.studyhub.exception.AiParsingException;
import com.fgv.studyhub.validation.QuestionValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionValidatorTest {
    private final QuestionValidator validator = new QuestionValidator();

    @Test
    void rejectsLetterPlaceholders() {
        var question = new AiQuestionDTO("Qual alternativa descreve corretamente o comportamento apresentado?", List.of("A", "B", "C", "D", "E"), 0, explanation());
        assertThatThrownBy(() -> validator.validate(question)).isInstanceOf(AiParsingException.class).hasMessageContaining("substantive answers");
    }

    @Test
    void acceptsACompleteQuestion() {
        var question = new AiQuestionDTO("Em uma aplicação Spring, qual alternativa descreve corretamente a inversão de controle?", List.of(
                "O contêiner cria e injeta as dependências dos componentes",
                "Cada classe instancia obrigatoriamente todas as suas dependências",
                "O banco de dados controla o ciclo de vida dos objetos Java",
                "O compilador substitui automaticamente todas as interfaces",
                "O servidor HTTP elimina a necessidade de injeção de dependências"), 0, explanation());
        assertThatCode(() -> validator.validate(question)).doesNotThrowAnyException();
    }

    private String explanation() {
        return "A) Correta porque descreve a responsabilidade real do contêiner.\n" +
                "B) Incorreta porque acopla a classe à construção das dependências.\n" +
                "C) Incorreta porque o banco não controla objetos da aplicação.\n" +
                "D) Incorreta porque o compilador não realiza essa substituição.\n" +
                "E) Incorreta porque HTTP e injeção tratam responsabilidades diferentes.";
    }
}
