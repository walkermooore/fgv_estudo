create extension if not exists vector;
alter table study_chunk add column embedding vector(768);
create index idx_chunk_embedding_hnsw on study_chunk using hnsw (embedding vector_cosine_ops);
