"""
Utility helpers for building and formatting prompts.
Extend this module as your prompt engineering needs grow.
"""


def build_filter_context(filters: dict) -> str:
    """Convert a filters dict into a human-readable string for prompts."""
    parts = []
    if filters.get("type"):
        parts.append(f"Art type: {filters['type']}")
    if filters.get("max_price") is not None:
        parts.append(f"Max price: ${filters['max_price']}")
    if filters.get("min_price") is not None:
        parts.append(f"Min price: ${filters['min_price']}")
    if filters.get("size"):
        parts.append(f"Size: {filters['size']}")
    if filters.get("keywords"):
        parts.append(f"Keywords: {', '.join(filters['keywords'])}")
    if filters.get("sort_by"):
        parts.append(f"Sort: {filters['sort_by']}")
    return " | ".join(parts) if parts else "No filters"
