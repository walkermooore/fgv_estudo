package com.fgv.studyhub.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

@Configuration @EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
 @Bean WebClient aiWebClient(WebClient.Builder builder,AppProperties p){return builder.baseUrl(p.ai().url()).defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+p.ai().apiKey()).build();}
}
