package com.fgv.studyhub.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app")
public record AppProperties(Cors cors, Storage storage, Ai ai, Vector vector, NotebookLm notebooklm) {
 public record Cors(String allowedOrigins){}
 public record Storage(String path){}
 public record Ai(String provider,String apiKey,String model,String quizModel,String noticeModel,String url,String embeddingModel,long timeoutSeconds){}
 public record Vector(int topK,String provider){}
 public record NotebookLm(boolean enabled,String pythonExecutable,String bridgeScript,String storagePath,long timeoutSeconds){}
}
