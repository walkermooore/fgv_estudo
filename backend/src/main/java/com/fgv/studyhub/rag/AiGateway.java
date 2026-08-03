package com.fgv.studyhub.rag;
import java.util.List;
public interface AiGateway { String chat(String prompt); default String chat(String prompt,String model){return chat(prompt);} List<Double> embed(String text); }
