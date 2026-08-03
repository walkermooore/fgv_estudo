# Prompt para o Lovable

Crie um frontend completo e responsivo para uma plataforma de estudos chamada **Simula+**. O backend já existe; não crie nem simule um novo backend. Use **React 18+, Vite, Tailwind CSS, Axios e Lucide React**.

## Direção visual

Quero uma interface bonita, simples, sóbria e com aparência de produto educacional real. Evite a estética típica de ferramentas de IA: não use gradientes chamativos, brilhos, glassmorphism excessivo, robôs, estrelas/sparkles, fundos futuristas, textos como “revolucionado por IA” ou uma hero section gigante. A IA deve ser apenas uma função discreta do produto, não o tema visual.

Use fundo off-white ou cinza muito claro, cartões brancos com bordas sutis, tipografia limpa, bastante espaço em branco e uma única cor de destaque — azul-marinho, índigo escuro ou vinho discreto. Cantos moderadamente arredondados, sombras leves e ícones funcionais. O resultado deve lembrar uma boa plataforma de estudos para concursos, e não uma landing page de startup. Inclua dark mode discreto e acessível.

## Estrutura

Crie um cabeçalho compacto e fixo com o nome “Simula+”, usando um ícone de alvo simples e elegante. Use o mesmo símbolo como favicon da aba do navegador. Crie quatro áreas principais:

1. **Simulados**
2. **Histórico**
3. **Biblioteca**
4. **Editais**

No mobile, use menu compacto. Use estados completos de loading, skeleton, vazio e erro com botão para tentar novamente. Garanta navegação por teclado, contraste adequado, foco visível e `aria-label` nos botões de ícone.

## Página Simulados

- Mantenha a abertura simples: título “Crie seu simulado” e uma frase curta. Não use o texto “Domine qualquer assunto”, selos sobre IA ou uma hero section alta.
- Campo de busca central com placeholder “Digite um assunto...”.
- Seletor simples da quantidade de questões.
- Exemplos discretos: Docker, Java, Spring Security, RabbitMQ e Crase.
- Enter ou botão “Gerar simulado” chama a API.
- Durante a geração local, informe de modo neutro que a primeira geração pode levar alguns minutos.
- Renderize as questões em cartões numerados.
- Cada questão possui exatamente cinco alternativas.
- Ao escolher uma alternativa, bloqueie a questão e envie a resposta.
- Destaque a alternativa correta em verde e a alternativa errada escolhida em vermelho.
- Mostre a explicação com `whitespace-pre-wrap`.
- Não revele gabarito nem explicação antes da resposta.

APIs:

- `GET /api/quiz?topic={tema}&amount={quantidade}`
- `POST /api/quiz/submit` com body `[{ "id": 1, "answer": 2 }]`
- `GET /api/random?amount=20`

## Página Histórico

Mostre todas as questões já respondidas, da mais recente para a mais antiga. No topo, exiba o total de respostas registradas. Cada item deve mostrar:

- tema;
- data e hora;
- enunciado;
- cinco alternativas;
- alternativa selecionada pelo usuário;
- alternativa correta;
- status “Acertou” ou “Errou”;
- explicação completa dentro de uma área expansível.

Use paginação simples com botões “Anterior” e “Próxima”. Crie um empty state amigável quando ainda não houver respostas.

API: `GET /api/history?page=0&size=20`

Formato da resposta:

```json
{
  "items": [
    {
      "id": 1,
      "questionId": 10,
      "topic": "Java",
      "statement": "Enunciado",
      "options": ["A", "B", "C", "D", "E"],
      "selectedAnswer": 2,
      "correctIndex": 3,
      "correct": false,
      "explanation": "Explicação completa",
      "answeredAt": "2026-08-03T15:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

## Página Biblioteca

- Upload múltiplo com drag and drop.
- Campo para adicionar uma URL.
- Busca de materiais.
- Lista com título, tipo, data, status, chunks e tokens.
- Tela de detalhes do material.
- Organize a área em três abas internas: **Materiais**, **Resumos** e **Perguntas**.
- Ações nos detalhes: perguntar, resumir, explicar tópico, flashcards, mapa mental, questões e anotações.
- Chat limpo, semelhante a uma conversa de suporte ou estudo. Não use avatar de robô ou ícone de brilho.
- Nas respostas, mostre fontes, trechos utilizados e tempo de resposta em uma seção secundária expansível.

### Gerador de resumos personalizados

Na aba **Resumos**, crie uma composição em duas colunas no desktop e uma coluna no mobile.

Na coluna esquerda, mostre:

- seleção de um ou vários documentos com status `READY`;
- lista com checkbox, título, tipo e quantidade de trechos;
- seletor do formato do resumo;
- textarea com o título “O que você quer no resumo?”;
- placeholder: “Ex.: Resuma apenas os pontos sobre JWT, destaque diferenças importantes e liste pegadinhas de prova.”;
- contador de até 2.000 caracteres;
- botão “Gerar resumo”.

Formatos permitidos: `SHORT`, `COMPLETE`, `TECHNICAL`, `BEGINNER`, `ADVANCED`, `MAP`, `CHECKLIST`, `TABLE` e `COMPARISON`.

Na coluna direita, mostre o resumo renderizado como Markdown. Abaixo do conteúdo, inclua uma seção expansível chamada “Documentos e capítulos utilizados”. Não invente fontes no frontend; use somente o array `sources` retornado pela API. Enquanto o modelo local estiver trabalhando, mostre um loading discreto com a mensagem “Lendo os documentos e preparando o resumo...”. A requisição pode levar vários minutos e não deve ser cancelada pelo frontend prematuramente.

Chamada:

```http
POST /api/materials/summarize
Content-Type: application/json
```

Body:

```json
{
  "materialIds": [1, 2],
  "type": "TECHNICAL",
  "request": "Resuma os pontos relacionados a autenticação e compare as abordagens dos documentos."
}
```

`materialIds` aceita de 1 a 10 documentos. `request` pode ser omitido ou enviado como `null`; nesse caso, o backend produz um resumo geral. Mantenha compatibilidade com o formato antigo que usa `materialId` para apenas um documento.

Resposta:

```json
{
  "content": "# Resumo\n\nConteúdo em Markdown...",
  "sources": [
    {
      "chunkId": 10,
      "materialId": 1,
      "materialTitle": "Segurança.pdf",
      "chapter": "JWT",
      "content": "Trecho utilizado...",
      "similarity": 0.91
    }
  ]
}
```

Se o backend retornar exatamente “Esse assunto não foi encontrado na sua biblioteca.”, apresente essa mensagem como estado informativo normal, não como erro técnico.

APIs principais:

- `GET /api/materials?search=`
- `GET /api/materials/{id}`
- `POST /api/materials/upload`
- `POST /api/materials/url`
- `DELETE /api/materials/{id}`
- `POST /api/study/query`
- `POST /api/materials/summarize`
- `POST /api/materials/explain`
- `POST /api/materials/flashcards`
- `POST /api/materials/mindmap`
- `POST /api/materials/questions`
- `POST /api/materials/notes`

## Página Editais

Crie uma área própria para enviar e organizar editais. O edital enviado também será armazenado automaticamente na Biblioteca. No topo, use o título “Organize seu edital” e uma explicação curta.

Inclua:

- campo opcional para o nome do edital;
- upload de um arquivo por vez;
- formatos PDF, DOCX, TXT, Markdown e HTML;
- botão “Enviar edital”;
- progresso do upload;
- informação discreta de que a análise local acontece em segundo plano e pode levar vários minutos;
- lista dos editais já enviados, do mais recente para o mais antigo;
- status `PROCESSING`, `READY` e `FAILED` apresentados em português;
- barra de progresso calculada com `processedBatches`, `totalBatches` e `progressPercentage`;
- atualização automática a cada cinco segundos enquanto existir algum edital em processamento;
- opção de excluir apenas quando não estiver processando.

Quando a análise estiver pronta, crie uma página de detalhes bem organizada com:

- nome do edital;
- órgão;
- banca examinadora;
- cargo;
- resumo geral;
- seção de datas e prazos;
- seção de informações úteis, incluindo inscrição, taxas, vagas, requisitos, etapas e regras quando existirem;
- conteúdo programático agrupado por tópico;
- subconteúdos dentro de cada tópico;
- palavras-chave de cada subconteúdo apresentadas como etiquetas discretas.

Não invente informações no frontend e não complete campos vazios com conhecimento próprio. Mostre “Não identificado” quando o backend não encontrar órgão, banca ou cargo.

APIs:

- `POST /api/notices/upload` como `multipart/form-data`, com `file` obrigatório e `title` opcional;
- `GET /api/notices`;
- `GET /api/notices/{id}`;
- `DELETE /api/notices/{id}`.

Formato principal da resposta:

```json
{
  "id": 1,
  "materialId": 20,
  "title": "Concurso SEFAZ 2026",
  "fileName": "edital.pdf",
  "status": "READY",
  "processedBatches": 8,
  "totalBatches": 8,
  "progressPercentage": 100,
  "failureReason": null,
  "createdAt": "2026-08-03T15:00:00Z",
  "processedAt": "2026-08-03T15:30:00Z",
  "analysis": {
    "organization": "Secretaria de Estado de Fazenda",
    "examiningBoard": "FGV",
    "position": "Auditor Fiscal",
    "summary": "Resumo extraído do edital.",
    "dates": [
      {"label": "Inscrições", "date": "01/09/2026 a 30/09/2026", "details": "Inscrição pela internet."}
    ],
    "contents": [
      {
        "topic": "Tecnologia da Informação",
        "subtopics": [
          {"name": "Segurança da Informação", "keywords": ["JWT", "OAuth 2.0", "criptografia"]}
        ]
      }
    ],
    "usefulInformation": [
      {"category": "Taxa", "title": "Taxa de inscrição", "details": "R$ 150,00"}
    ]
  }
}
```

## Requisitos técnicos

- Centralize o Axios em `src/services/api.js` com `axios.create()` e `baseURL` igual a `import.meta.env.VITE_API_URL || '/api'`.
- Organize o código em `pages/`, `components/`, `hooks/`, `services/`, `utils/` e `assets/`.
- Crie componentes pequenos e reutilizáveis.
- Não use dados mockados como solução final.
- Não altere os contratos da API.
- Trate erros retornados no formato `{ "message": "..." }`.
- Formate datas e números em `pt-BR`.
- Entregue todo o código funcional, sem pseudocódigo e sem métodos vazios.
