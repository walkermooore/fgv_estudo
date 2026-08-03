# Prompt para o Lovable

Crie um frontend completo e responsivo para uma plataforma de estudos chamada **FGV Study Hub**. O backend já existe; não crie nem simule um novo backend. Use **React 18+, Vite, Tailwind CSS, Axios e Lucide React**.

## Direção visual

Quero uma interface bonita, simples, sóbria e com aparência de produto educacional real. Evite a estética típica de ferramentas de IA: não use gradientes chamativos, brilhos, glassmorphism excessivo, robôs, estrelas/sparkles, fundos futuristas, textos como “revolucionado por IA” ou uma hero section gigante. A IA deve ser apenas uma função discreta do produto, não o tema visual.

Use fundo off-white ou cinza muito claro, cartões brancos com bordas sutis, tipografia limpa, bastante espaço em branco e uma única cor de destaque — azul-marinho, índigo escuro ou vinho discreto. Cantos moderadamente arredondados, sombras leves e ícones funcionais. O resultado deve lembrar uma boa plataforma de estudos para concursos, e não uma landing page de startup. Inclua dark mode discreto e acessível.

## Estrutura

Crie um cabeçalho compacto e fixo com o nome “FGV Study Hub” e três áreas principais:

1. **Simulados**
2. **Histórico**
3. **Biblioteca**

No mobile, use menu compacto. Use estados completos de loading, skeleton, vazio e erro com botão para tentar novamente. Garanta navegação por teclado, contraste adequado, foco visível e `aria-label` nos botões de ícone.

## Página Simulados

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

## Requisitos técnicos

- Centralize o Axios em `src/services/api.js` com `axios.create()` e `baseURL` igual a `import.meta.env.VITE_API_URL || '/api'`.
- Organize o código em `pages/`, `components/`, `hooks/`, `services/`, `utils/` e `assets/`.
- Crie componentes pequenos e reutilizáveis.
- Não use dados mockados como solução final.
- Não altere os contratos da API.
- Trate erros retornados no formato `{ "message": "..." }`.
- Formate datas e números em `pt-BR`.
- Entregue todo o código funcional, sem pseudocódigo e sem métodos vazios.
