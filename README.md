# Simula+

Plataforma full stack para gerar simulados FGV com cache local e construir uma biblioteca pessoal RAG. A IA padrão é **100% local e gratuita**, executada pelo Ollama. O backend nunca expõe entidades e centraliza IA no `AiGateway`, implementado com `WebClient`. O modo local usa H2 em compatibilidade PostgreSQL e similaridade vetorial em Java; o perfil `postgres` ativa PostgreSQL/PgVector e índice HNSW.

## Executar localmente

Pré-requisitos: JDK 17+, Maven 3.9+, Node 20+ e Ollama.

```bash
ollama serve
ollama pull qwen3:4b-instruct
ollama pull embeddinggemma
```

Em outro terminal:

```bash
cd backend && mvn spring-boot:run
```

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. O H2 persiste em `backend/data`. A primeira geração pode demorar alguns minutos em CPU; questões já armazenadas são reutilizadas imediatamente. Nenhuma chave ou cobrança é necessária.

## Serviços em segundo plano

Nesta máquina, Ollama, backend e frontend estão instalados como serviços do usuário. Comandos úteis:

```bash
systemctl --user status fgv-ollama fgv-backend fgv-frontend
systemctl --user restart fgv-ollama fgv-backend fgv-frontend
journalctl --user -u fgv-backend -f
```

Os serviços reiniciam automaticamente em caso de falha e são iniciados junto à sessão do usuário.

## PostgreSQL + PgVector

Execute:

```bash
docker compose up --build
```

O Compose sobe PostgreSQL, PgVector, Ollama, baixa os modelos e inicia a aplicação. O perfil `postgres` usa `vector(768)` para o EmbeddingGemma e cria índice HNSW. Ao trocar o modelo de embeddings, ajuste a dimensão na migração antes da primeira subida.

## Endpoints

- `GET /api/quiz?topic=Java&amount=20`, `GET /api/random?amount=20`, `POST /api/quiz/submit`
- `GET /api/history?page=0&size=20` para consultar as questões respondidas
- `POST /api/materials/upload`, `POST /api/materials/url`, `GET/DELETE /api/materials/{id}`
- `POST /api/study/query`
- `POST /api/materials/summarize|explain|flashcards|mindmap|questions|notes`
- `POST /api/notices/upload`, `GET /api/notices`, `GET/DELETE /api/notices/{id}`

O gerador de resumos aceita um ou vários documentos e um pedido personalizado. Exemplo: `{"materialIds":[1,2],"type":"TECHNICAL","request":"Resuma autenticação e compare as abordagens."}`. A resposta usa exclusivamente trechos dos materiais selecionados.

Uploads aceitos: PDF, DOCX, TXT, Markdown, CSV e HTML, até 25 MB por arquivo. URLs privadas/loopback são rejeitadas para prevenir SSRF.

## Variáveis

Por padrão: `AI_PROVIDER=ollama`, `AI_URL=http://localhost:11434`, `AI_MODEL=qwen3:4b-instruct`, `AI_EMBEDDING_MODEL=embeddinggemma` e `AI_TIMEOUT_SECONDS=1800`. Também existem `STORAGE_PATH`, `CORS_ALLOWED_ORIGINS` e, no perfil PostgreSQL, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`. Em máquinas apenas com CPU, o primeiro simulado de cinco questões pode levar vários minutos; o cache torna as próximas consultas imediatas.

OpenAI permanece opcional para quem quiser: configure `AI_PROVIDER=openai`, `AI_URL=https://api.openai.com/v1`, `AI_MODEL`, `AI_EMBEDDING_MODEL` e `OPENAI_API_KEY`.

O prompt completo para redesenhar a interface no Lovable está em [`docs/lovable-frontend-prompt.md`](docs/lovable-frontend-prompt.md).
