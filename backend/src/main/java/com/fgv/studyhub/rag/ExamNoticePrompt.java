package com.fgv.studyhub.rag;

import com.fgv.studyhub.entity.StudyChunk;

import java.util.List;
import java.util.stream.Collectors;

public final class ExamNoticePrompt {
    private ExamNoticePrompt() {}

    public static String extraction(List<StudyChunk> chunks) {
        String context = chunks.stream()
                .map(chunk -> "[Trecho %d | Capítulo: %s]\n%s".formatted(
                        chunk.getId(),
                        chunk.getChapter() == null ? "não identificado" : chunk.getChapter(),
                        chunk.getContent()))
                .collect(Collectors.joining("\n\n---\n\n"));

        return """
Você é um analista de editais de concursos públicos.

Extraia EXCLUSIVAMENTE informações explicitamente presentes nos trechos fornecidos. Não utilize conhecimento externo, não presuma dados ausentes e não invente datas, conteúdos ou requisitos.

Organize o conteúdo programático em tópicos. Dentro de cada tópico, identifique subconteúdos e palavras-chave curtas e úteis para estudo. Extraia também todas as datas, prazos, órgão, banca, cargo e informações úteis como inscrições, taxas, vagas, requisitos, etapas, provas, critérios e regras importantes.

Retorne EXATAMENTE UM JSON válido, sem Markdown e sem qualquer texto antes ou depois, neste formato:

{
  "organization":"",
  "examiningBoard":"",
  "position":"",
  "summary":"",
  "dates":[
    {"label":"","date":"","details":""}
  ],
  "contents":[
    {
      "topic":"",
      "subtopics":[
        {"name":"","keywords":[""]}
      ]
    }
  ],
  "usefulInformation":[
    {"category":"","title":"","details":""}
  ]
}

Use arrays vazios quando uma categoria não estiver presente. O campo summary deve resumir apenas os trechos desta etapa.

TRECHOS DO EDITAL:
%s
""".formatted(context);
    }
}
