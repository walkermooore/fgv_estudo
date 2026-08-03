package com.fgv.studyhub.rag;
import java.util.List;
public interface AiGateway {
    String chat(String prompt);
    default String chat(String prompt, String model) { return chat(prompt); }
    default String chatQuestions(String prompt, String model, int amount) { return chat(prompt, model); }
    List<Double> embed(String text);
}
