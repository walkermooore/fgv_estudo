package com.fgv.studyhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.dto.NotebookLmStatusDTO;
import com.fgv.studyhub.dto.SummaryRequestDTO;
import com.fgv.studyhub.entity.StudyMaterial;
import com.fgv.studyhub.repository.StudyChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotebookLmSummaryService {
    private static final String PROVIDER = "notebooklm-py";
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final StudyChunkRepository chunks;

    public Optional<String> summarize(SummaryRequestDTO request, List<StudyMaterial> materials) {
        if (!properties.notebooklm().enabled()) return Optional.empty();
        try {
            Map<String, Object> payload = basePayload("summarize");
            payload.put("prompt", summaryPrompt(request));
            payload.put("materials", materials.stream().map(this::materialPayload).toList());
            JsonNode response = invoke(payload);
            if (response.path("ok").asBoolean(false) && !response.path("content").asText().isBlank()) {
                return Optional.of(response.path("content").asText().trim());
            }
            log.warn("NotebookLM não gerou o resumo; usando Ollama local: {}", response.path("error").asText("resposta inválida"));
        } catch (Exception exception) {
            log.warn("NotebookLM indisponível; usando Ollama local: {}", exception.getMessage());
        }
        return Optional.empty();
    }

    public NotebookLmStatusDTO status() {
        if (!properties.notebooklm().enabled()) {
            return new NotebookLmStatusDTO(false, false, PROVIDER, "NotebookLM desativado; resumos usam o Ollama local");
        }
        try {
            JsonNode response = invoke(basePayload("status"));
            boolean authenticated = response.path("authenticated").asBoolean(false);
            return new NotebookLmStatusDTO(true, authenticated, PROVIDER, response.path("message").asText(authenticated ? "NotebookLM conectado" : "NotebookLM não autenticado"));
        } catch (Exception exception) {
            return new NotebookLmStatusDTO(true, false, PROVIDER, "NotebookLM indisponível; o fallback local permanece ativo");
        }
    }

    private Map<String, Object> basePayload(String operation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operation", operation);
        payload.put("storagePath", blankToNull(properties.notebooklm().storagePath()));
        payload.put("timeoutSeconds", properties.notebooklm().timeoutSeconds());
        return payload;
    }

    private Map<String, Object> materialPayload(StudyMaterial material) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("title", material.getTitle());
        source.put("storedPath", absoluteStoredPath(material));
        source.put("originalUrl", material.getOriginalUrl());
        source.put("text", materialText(material.getId()));
        return source;
    }

    private String absoluteStoredPath(StudyMaterial material) {
        if (material.getStoredPath() == null || material.getStoredPath().isBlank()) return null;
        return Path.of(material.getStoredPath()).toAbsolutePath().normalize().toString();
    }

    private String materialText(Long materialId) {
        StringBuilder text = new StringBuilder();
        chunks.findByMaterialIdOrderByChunkIndex(materialId).forEach(chunk -> {
            if (chunk.getChapter() != null && !chunk.getChapter().isBlank()) text.append("\n\n## ").append(chunk.getChapter());
            text.append("\n\n").append(chunk.getContent());
        });
        return text.toString().trim();
    }

    private String summaryPrompt(SummaryRequestDTO request) {
        String custom = request.request() == null ? "" : request.request().trim();
        return """
                Produza um resumo em português do Brasil usando EXCLUSIVAMENTE os materiais deste notebook.
                Formato solicitado: %s.
                %s
                Organize a resposta em Markdown, priorize precisão e não acrescente conhecimento externo.
                Se algo pedido não estiver nas fontes, informe claramente que não foi encontrado nos documentos.
                Ao final, inclua uma seção curta chamada \"Fontes utilizadas\" com os nomes dos materiais usados.
                """.formatted(request.type(), custom.isBlank() ? "Faça um resumo geral e completo." : "Pedido específico do usuário: " + custom).trim();
    }

    private JsonNode invoke(Map<String, Object> payload) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command());
        Process process = processBuilder.start();
        try (var input = process.getOutputStream()) {
            objectMapper.writeValue(input, payload);
        }
        CompletableFuture<String> stdout = readAsync(process.getInputStream());
        CompletableFuture<String> stderr = readAsync(process.getErrorStream());
        long timeout = Math.max(10, properties.notebooklm().timeoutSeconds());
        if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("NotebookLM excedeu o limite de " + Duration.ofSeconds(timeout));
        }
        String output = stdout.get(5, TimeUnit.SECONDS).trim();
        String errors = stderr.get(5, TimeUnit.SECONDS).trim();
        if (output.isBlank()) throw new IOException("A ponte NotebookLM não retornou JSON" + compactError(errors));
        JsonNode response = objectMapper.readTree(output);
        if (process.exitValue() != 0 && !response.path("ok").asBoolean(false)) {
            throw new IOException(response.path("error").asText("Falha no NotebookLM") + compactError(errors));
        }
        return response;
    }

    private List<String> command() {
        String executable = required(properties.notebooklm().pythonExecutable(), "pythonExecutable");
        String script = required(properties.notebooklm().bridgeScript(), "bridgeScript");
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add(script);
        return command;
    }

    private CompletableFuture<String> readAsync(java.io.InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private String compactError(String error) {
        if (error == null || error.isBlank()) return "";
        return ": " + error.substring(0, Math.min(error.length(), 500));
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException("NotebookLM " + name + " não configurado");
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
