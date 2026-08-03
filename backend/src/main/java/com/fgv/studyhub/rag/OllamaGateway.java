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
        Map<String, Object> body = chatBody(prompt, model,
                Map.of("temperature", 0.2, "num_ctx", 8192, "num_predict", 8192, "num_thread", 8));
        return requestChat(body);
    }

    @Override
    public String chatQuestions(String prompt, String model, int amount) {
        int predictionLimit = Math.min(4096, Math.max(650, amount * 550));
        Map<String, Object> body = chatBody(prompt, model,
                Map.of("temperature", 0.2, "num_ctx", 4096, "num_predict", predictionLimit, "num_thread", 4));
        body.put("format", questionFormat(amount));
        return requestChat(body);
    }

    private Map<String, Object> chatBody(String prompt, String model, Map<String, Object> options) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("think", false);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("options", options);
        return body;
    }

    private String requestChat(Map<String, Object> body) {
        JsonNode response = post("/api/chat", body);
        String content = response.path("message").path("content").asText();
        if (content.isBlank()) throw new AiServiceException("O Ollama não retornou conteúdo");
        return removeThinkingBlock(content);
    }

    private Map<String, Object> questionFormat(int amount) {
        Map<String, Object> statement = Map.of("type", "string", "minLength", 10, "maxLength", 800);
        Map<String, Object> option = Map.of("type", "string", "minLength", 1, "maxLength", 300);
        Map<String, Object> explanation = Map.of("type", "string", "minLength", 20, "maxLength", 900);
        Map<String, Object> question = Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("statement", "options", "correctIndex", "explanation"),
                "properties", Map.of(
                        "statement", statement,
                        "options", Map.of("type", "array", "minItems", 5, "maxItems", 5, "items", option),
                        "correctIndex", Map.of("type", "integer", "minimum", 0, "maximum", 4),
                        "explanation", explanation
                )
        );
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("questions"),
                "properties", Map.of("questions", Map.of(
                        "type", "array", "minItems", amount, "maxItems", amount, "items", question
                ))
        );
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
