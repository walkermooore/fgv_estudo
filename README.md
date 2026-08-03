# FGV Question Generator + AI Study Hub

Plataforma full stack para gerar simulados FGV com cache local e construir uma biblioteca pessoal RAG. O backend nunca expõe entidades e centraliza IA no `AiGateway`, implementado com `WebClient`. O modo local usa H2 em compatibilidade PostgreSQL e similaridade vetorial em Java; o perfil `postgres` ativa PostgreSQL/PgVector e índice HNSW.

## Executar localmente

Pré-requisitos: JDK 17+, Maven 3.9+, Node 20+ e uma chave OpenAI.

```bash
export OPENAI_API_KEY="sua-chave"
cd backend && mvn spring-boot:run
```

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. O H2 persiste em `backend/data`. A primeira geração ou ingestão precisa da API; questões já armazenadas são reutilizadas sem nova chamada.

## PostgreSQL + PgVector

Copie `.env.example` para `.env`, preencha a chave e execute:

```bash
docker compose up --build
```

O perfil `postgres` aplica a extensão `vector`, coluna `vector(1536)` e índice HNSW. Ao trocar o modelo de embeddings, ajuste a dimensão na migração antes da primeira subida.

## Endpoints

- `GET /api/quiz?topic=Java&amount=20`, `GET /api/random?amount=20`, `POST /api/quiz/submit`
- `POST /api/materials/upload`, `POST /api/materials/url`, `GET/DELETE /api/materials/{id}`
- `POST /api/study/query`
- `POST /api/materials/summarize|explain|flashcards|mindmap|questions|notes`

Uploads aceitos: PDF, DOCX, TXT, Markdown, CSV e HTML, até 25 MB por arquivo. URLs privadas/loopback são rejeitadas para prevenir SSRF.

## Variáveis

`OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_URL`, `OPENAI_EMBEDDING_MODEL`, `OPENAI_TIMEOUT_SECONDS`, `STORAGE_PATH`, `CORS_ALLOWED_ORIGINS`, e, no perfil PostgreSQL, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`.
