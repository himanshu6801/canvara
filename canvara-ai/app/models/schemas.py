from pydantic import BaseModel
from typing import Optional, List, Any
from enum import Enum


class MessageRole(str, Enum):
    user = "user"
    assistant = "assistant"


class ChatMessage(BaseModel):
    role: MessageRole
    content: str


class ChatRequest(BaseModel):
    message: str

    class Config:
        json_schema_extra = {
            "example": {
                "message": "Show me abstract artworks under $400 in large size"
            }
        }


class ArtworkFilters(BaseModel):
    """Structured filters extracted by the LLM from the user's message."""
    type: Optional[str] = None          # e.g. "abstract", "portrait", "landscape"
    max_price: Optional[float] = None   # maximum price in USD
    min_price: Optional[float] = None   # minimum price in USD
    size: Optional[str] = None          # "small", "medium", "large"
    keywords: Optional[List[str]] = None  # descriptive keywords e.g. ["baby", "face"]
    sort_by: Optional[str] = None       # "latest", "price_asc", "price_desc"


class ChatResponse(BaseModel):
    reply: str                          # Human-readable assistant message
    artworks: List[Any]                 # List of artwork objects from backend
    filters_applied: ArtworkFilters     # Filters the LLM extracted
