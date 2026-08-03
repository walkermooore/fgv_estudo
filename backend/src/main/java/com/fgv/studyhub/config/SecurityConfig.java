package com.fgv.studyhub.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration @RequiredArgsConstructor
public class SecurityConfig {
 private final AppProperties properties;
 @Bean SecurityFilterChain security(HttpSecurity http)throws Exception{return http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults()).httpBasic(Customizer.withDefaults()).authorizeHttpRequests(a->a.requestMatchers("/api/**","/h2-console/**").permitAll().anyRequest().authenticated()).headers(h->h.frameOptions(f->f.sameOrigin())).build();}
 @Bean CorsConfigurationSource cors(){var c=new CorsConfiguration();c.setAllowedOrigins(Arrays.stream(properties.cors().allowedOrigins().split(",")).map(String::trim).toList());c.setAllowedMethods(List.of("GET","POST","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;}
}
