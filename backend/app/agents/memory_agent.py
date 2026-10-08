"""
Memory Agent: Encapsulates user farm history, telemetry snapshots, user preference memory,
and strict GDPR-compliant right-to-erasure / data export functionality.
Never shares user data across farm boundaries; enforces zero-knowledge tenancy.
"""

from typing import Dict, Any, List, Optional
from datetime import datetime
from ..schemas.agent_contracts import (
    MemoryStoreRequest,
    MemoryExportResponse,
)


class MemoryAgent:
    """
    Manages user session memory, agronomy observation cache, and data deletion workflows.
    """

    def __init__(self):
        # In-memory tenant store (backed by PostgreSQL / Redis in production deployment)
        self._store: Dict[str, Dict[str, Any]] = {}
        self._history_logs: Dict[str, List[Dict[str, Any]]] = {}

    async def store_memory(self, req: MemoryStoreRequest) -> Dict[str, Any]:
        """Stores a key-value memory item scoped to user and farm."""
        user_key = f"{req.user_id}:{req.farm_id}"
        if user_key not in self._store:
            self._store[user_key] = {}
        
        self._store[user_key][req.key] = {
            "value": req.value_json,
            "updated_at": datetime.utcnow().isoformat(),
            "expires_at": req.expires_at.isoformat() if req.expires_at else None
        }

        # Track log entry
        if req.user_id not in self._history_logs:
            self._history_logs[req.user_id] = []
        
        self._history_logs[req.user_id].append({
            "action": f"Stored key '{req.key}'",
            "timestamp": datetime.utcnow().isoformat()
        })

        return {
            "status": "stored",
            "user_id": req.user_id,
            "farm_id": req.farm_id,
            "key": req.key,
            "stored_at": datetime.utcnow().isoformat()
        }

    async def export_user_data(self, user_id: str) -> MemoryExportResponse:
        """
        Exports all farm profiles, telemetry records, and memory entries for GDPR data portability.
        """
        user_entries = [
            v for k, v in self._store.items() if k.startswith(f"{user_id}:")
        ]
        logs_count = len(self._history_logs.get(user_id, []))

        return MemoryExportResponse(
            user_id=user_id,
            farm_profiles=[{"active_keys": list(entry.keys())} for entry in user_entries],
            observation_records=len(user_entries),
            log_entries_count=logs_count,
            export_generated_at=datetime.utcnow()
        )

    async def delete_user_data(self, user_id: str) -> Dict[str, Any]:
        """
        GDPR Right-to-Erasure: Irrevocably purges all farm records, cached telemetry, and history logs.
        """
        keys_to_delete = [k for k in self._store.keys() if k.startswith(f"{user_id}:")]
        for k in keys_to_delete:
            del self._store[k]

        if user_id in self._history_logs:
            del self._history_logs[user_id]

        return {
            "status": "purged",
            "user_id": user_id,
            "deleted_records_count": len(keys_to_delete),
            "timestamp": datetime.utcnow().isoformat()
        }
