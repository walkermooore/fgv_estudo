package com.fgv.studyhub.rag;
import com.fasterxml.jackson.databind.*;
import com.fgv.studyhub.config.AppProperties;
import com.fgv.studyhub.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
@Component @RequiredArgsConstructor
public class OpenAiGateway implements AiGateway {
 private final WebClient aiWebClient; private final AppProperties p; private final ObjectMapper mapper;
 public String chat(String prompt){
  ensureConfigured(); var body=Map.of("model",p.ai().model(),"temperature",0.2,"messages",List.of(Map.of("role","user","content",prompt)));
  JsonNode root=post("/chat/completions",body); String value=root.path("choices").path(0).path("message").path("content").asText(); if(value.isBlank())throw new AiServiceException("AI returned no content"); return value;
 }
 public List<Double> embed(String text){
  ensureConfigured(); JsonNode root=post("/embeddings",Map.of("model",p.ai().embeddingModel(),"input",text)); var node=root.path("data").path(0).path("embedding"); if(!node.isArray())throw new AiServiceException("Embedding response is invalid"); List<Double> out=new ArrayList<>();node.forEach(n->out.add(n.asDouble()));return out;
 }
 private JsonNode post(String uri,Object body){try{return aiWebClient.post().uri(uri).bodyValue(body).retrieve().onStatus(s->s.value()==429,r->Mono.error(new AiRateLimitException("AI rate limit exceeded"))).onStatus(HttpStatusCode::isError,r->r.bodyToMono(String.class).flatMap(x->Mono.error(new AiServiceException("AI request failed with status "+r.statusCode().value())))).bodyToMono(JsonNode.class).timeout(Duration.ofSeconds(p.ai().timeoutSeconds())).block();}catch(AiRateLimitException|AiServiceException e){throw e;}catch(Exception e){if(e instanceof TimeoutException||e.getCause() instanceof TimeoutException)throw new AiTimeoutException("AI request timed out",e);throw new AiServiceException("AI service is unavailable",e);}}
 private void ensureConfigured(){if(p.ai().apiKey()==null||p.ai().apiKey().isBlank())throw new AiServiceException("OPENAI_API_KEY is not configured");}
}
