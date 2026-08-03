package com.fgv.studyhub.rag;
import com.fgv.studyhub.dto.SourceChunkDTO;
import java.util.List;
import java.util.stream.Collectors;
public final class RagPrompts {
 private RagPrompts(){}
 public static String grounded(String task,List<SourceChunkDTO> sources){return """
Você responderá EXCLUSIVAMENTE utilizando os trechos fornecidos.

Nunca utilize conhecimento externo.

Caso a resposta não esteja presente nos materiais, responda exatamente:

\"Esse assunto não foi encontrado na sua biblioteca.\"

Sempre informe:

• quais materiais foram utilizados;

• quais capítulos foram utilizados quando possível.

A resposta deve ser escrita em Markdown.

TAREFA:
%s

TRECHOS FORNECIDOS:
%s
""".formatted(task,context(sources));}
 public static String context(List<SourceChunkDTO> sources){return sources.stream().map(s->"[Trecho %d | Material: %s | Capítulo: %s | Similaridade: %.4f]\n%s".formatted(s.chunkId(),s.materialTitle(),s.chapter()==null?"não identificado":s.chapter(),s.similarity(),s.content())).collect(Collectors.joining("\n\n---\n\n"));}
}
