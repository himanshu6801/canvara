import asyncio
import os
from urllib.parse import urlparse, unquote

import mysql.connector
from app.models.schemas import ArtworkFilters


def _parse_database_url(database_url: str) -> dict:
    normalized_url = database_url.strip()
    if normalized_url.startswith("jdbc:"):
        normalized_url = normalized_url[len("jdbc:"):]

    parsed = urlparse(normalized_url)
    if parsed.scheme not in ("mysql", "mysql+mysqlconnector"):
        raise RuntimeError("Unsupported DATABASE_URL scheme: must start with mysql:// or jdbc:mysql://")

    return {
        "host": parsed.hostname or "localhost",
        "port": parsed.port or 3306,
        "user": unquote(parsed.username) if parsed.username else os.getenv("DATABASE_USER", "root"),
        "password": unquote(parsed.password) if parsed.password else os.getenv("DATABASE_PASSWORD", "password"),
        "database": parsed.path.lstrip("/") or os.getenv("DATABASE_NAME", "home"),
    }


def _get_db_config() -> dict:
    database_url = os.getenv("DATABASE_URL")
    if database_url:
        return _parse_database_url(database_url)

    return {
        "host": os.getenv("DATABASE_HOST", "localhost"),
        "port": int(os.getenv("DATABASE_PORT", "3306")),
        "user": os.getenv("DATABASE_USER", "root"),
        "password": os.getenv("DATABASE_PASSWORD", "password"),
        "database": os.getenv("DATABASE_NAME", "home"),
    }


def _connect_db():
    config = _get_db_config()
    return mysql.connector.connect(**config)


def _build_query(filters: ArtworkFilters) -> tuple[str, list]:
    query = "SELECT DISTINCT a.* FROM artworks a"
    joins: list[str] = []
    conditions: list[str] = []
    params: list = []

    if filters.type:
        joins.append("JOIN artwork_styles s ON s.artwork_id = a.id")
        conditions.append("s.style = %s")
        params.append(filters.type)
    if filters.max_price is not None:
        conditions.append("a.price <= %s")
        params.append(filters.max_price)
    if filters.min_price is not None:
        conditions.append("a.price >= %s")
        params.append(filters.min_price)
    if filters.size:
        conditions.append("a.size = %s")
        params.append(filters.size)
    if filters.keywords:
        for keyword in filters.keywords:
            kw = f"%{keyword}%"
            conditions.append(
                "(a.title LIKE %s"
                " OR a.description LIKE %s"
                " OR a.story_title LIKE %s"
                " OR a.story_content LIKE %s"
                " OR EXISTS (SELECT 1 FROM artwork_categories c WHERE c.artwork_id = a.id AND c.category LIKE %s)"
                " OR EXISTS (SELECT 1 FROM artwork_mediums m WHERE m.artwork_id = a.id AND m.medium LIKE %s))"
            )
            params.extend([kw, kw, kw, kw, kw, kw])

    if joins:
        query += " " + " ".join(joins)

    if conditions:
        query += " WHERE " + " AND ".join(conditions)

    if filters.sort_by == "latest":
        query += " ORDER BY a.created_at DESC"
    elif filters.sort_by == "price_asc":
        query += " ORDER BY a.price ASC"
    elif filters.sort_by == "price_desc":
        query += " ORDER BY a.price DESC"

    return query, params


def _fetch_artworks_from_db(filters: ArtworkFilters) -> list:
    with _connect_db() as conn:
        cursor = conn.cursor(dictionary=True)
        query, params = _build_query(filters)
        cursor.execute(query, params)
        artworks = cursor.fetchall()
        cursor.close()
        return artworks


async def fetch_artworks(filters: ArtworkFilters) -> list:
    """Fetch artworks directly from the MySQL database using extracted filters."""
    return await asyncio.to_thread(_fetch_artworks_from_db, filters)
