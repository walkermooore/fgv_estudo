#!/usr/bin/env python3
"""Small JSON-over-stdio bridge between Simula+ and notebooklm-py."""

import asyncio
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

from notebooklm import NotebookLMClient


def emit(payload: dict) -> None:
    json.dump(payload, sys.stdout, ensure_ascii=False)
    sys.stdout.write("\n")


async def client_context(payload: dict):
    storage_path = payload.get("storagePath") or None
    timeout = float(payload.get("timeoutSeconds") or 300)
    return NotebookLMClient.from_storage(
        path=storage_path,
        timeout=min(timeout, 60),
        chat_timeout=timeout,
    )


async def status(payload: dict) -> dict:
    try:
        context = await client_context(payload)
        async with context as client:
            await client.notebooks.list()
        return {"ok": True, "authenticated": True, "message": "NotebookLM conectado"}
    except Exception as exception:
        return {
            "ok": True,
            "authenticated": False,
            "message": f"NotebookLM ainda não autenticado: {exception}",
        }


async def add_source(client, notebook_id: str, source: dict, timeout: float):
    stored_path = source.get("storedPath")
    title = source.get("title") or "Material"
    if stored_path and Path(stored_path).is_file():
        return await client.sources.add_file(
            notebook_id,
            Path(stored_path),
            title=title,
            wait=True,
            wait_timeout=timeout,
        )

    original_url = source.get("originalUrl")
    if original_url:
        try:
            return await client.sources.add_url(
                notebook_id,
                original_url,
                title=title,
                wait=True,
                wait_timeout=timeout,
            )
        except Exception:
            if not source.get("text"):
                raise

    text = source.get("text")
    if text:
        return await client.sources.add_text(
            notebook_id,
            title,
            text,
            wait=True,
            wait_timeout=timeout,
        )
    raise ValueError(f"O material '{title}' não possui arquivo, URL ou texto utilizável")


async def summarize(payload: dict) -> dict:
    timeout = float(payload.get("timeoutSeconds") or 300)
    context = await client_context(payload)
    notebook_id = None
    async with context as client:
        try:
            suffix = datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S")
            notebook = await client.notebooks.create(f"Simula+ resumo {suffix}")
            notebook_id = notebook.id
            sources = []
            for item in payload.get("materials", []):
                sources.append(await add_source(client, notebook_id, item, timeout))
            if not sources:
                raise ValueError("Nenhum material foi enviado ao NotebookLM")
            result = await client.chat.ask(
                notebook_id,
                payload["prompt"],
                source_ids=[source.id for source in sources],
            )
            answer = (result.answer or "").strip()
            if not answer:
                raise ValueError("O NotebookLM retornou um resumo vazio")
            return {"ok": True, "content": answer}
        finally:
            if notebook_id:
                await client.notebooks.delete(notebook_id)


async def main() -> None:
    payload = json.load(sys.stdin)
    operation = payload.get("operation")
    if operation == "status":
        emit(await status(payload))
        return
    if operation == "summarize":
        emit(await summarize(payload))
        return
    raise ValueError(f"Operação desconhecida: {operation}")


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except Exception as exception:
        emit({"ok": False, "error": str(exception), "type": type(exception).__name__})
        sys.exit(1)
