package com.fgv.studyhub.rag;
import java.util.List;
public interface AiGateway { String chat(String prompt); List<Double> embed(String text); }
