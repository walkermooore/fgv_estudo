package com.fgv.studyhub.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app")
public record AppProperties(Cors cors, Storage storage, Ai ai, Vector vector) {
 public record Cors(String allowedOrigins){}
 public record Storage(String path){}
 public record Ai(String apiKey,String model,String url,String embeddingModel,long timeoutSeconds){}
 public record Vector(int topK,String provider){}
}
