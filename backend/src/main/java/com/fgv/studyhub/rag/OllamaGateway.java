package com.fgv.studyhub.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name="app.ai.provider", havingValue="ollama", matchIfMissing=true)
@RequiredArgsConstructor
public class OllamaGateway implements AiGateway {
    private final WebClient aiWebClient;
    private final AppProperties properties;

    @Override
    public String chat(String prompt) {
        return chat(prompt, properties.ai().model());
    }

    @Override
    public String chat(String prompt, String model) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "think", false,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "options", Map.of("temperature", 0.2, "num_ctx", 8192, "num_predict", 8192, "num_thread", 8)
        );
        JsonNode response = post("/api/chat", body);
        String content = response.path("message").path("content").asText();
        if (content.isBlank()) throw new AiServiceException("O Ollama não retornou conteúdo");
        return removeThinkingBlock(content);
    }

    @Override
    public List<Double> embed(String text) {
        JsonNode response = post("/api/embed", Map.of(
                "model", properties.ai().embeddingModel(),
                "input", text,
                "truncate", true
        ));
        JsonNode embedding = response.path("embeddings").path(0);
        if (!embedding.isArray()) throw new AiServiceException("A resposta de embeddings do Ollama é inválida");
        List<Double> values = new ArrayList<>();
        embedding.forEach(value -> values.add(value.asDouble()));
        return values;
    }

    private JsonNode post(String path, Object body) {
        try {
            return aiWebClient.post().uri(path).bodyValue(body).retrieve()
                    .onStatus(status -> status.value() == 404,
                            response -> Mono.error(new AiServiceException(
                                    "Modelo local não encontrado. Baixe os modelos configurados com ollama pull")))
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class).flatMap(error -> Mono.error(new AiServiceException("Ollama retornou HTTP " + response.statusCode().value()))))
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(properties.ai().timeoutSeconds()))
                    .block();
        } catch (AiServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            if (exception instanceof TimeoutException || exception.getCause() instanceof TimeoutException) {
                throw new AiTimeoutException("O modelo local excedeu o tempo limite", exception);
            }
            throw new AiServiceException("O Ollama local não está disponível em " + properties.ai().url(), exception);
        }
    }

    private String removeThinkingBlock(String content) {
        return content.replaceFirst("(?s)^\\s*<think>.*?</think>\\s*", "").trim();
    }
}
