package com.fgv.studyhub.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration @EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
 @Bean WebClient aiWebClient(WebClient.Builder builder,AppProperties p){var configured=builder.baseUrl(p.ai().url());if(p.ai().apiKey()!=null&&!p.ai().apiKey().isBlank())configured.defaultHeader("Authorization","Bearer "+p.ai().apiKey());return configured.build();}
}
