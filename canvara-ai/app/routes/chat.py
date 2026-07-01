from fastapi import APIRouter, HTTPException
from app.models.schemas import ChatRequest, ChatResponse, MessageRole, ChatMessage
from app.services.llm_service import parse_artwork_query, generate_response
from app.services.artwork_service import fetch_artworks

router = APIRouter()


@router.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    """
    Accept a natural language message and return matching artworks.

    Example prompts:
    - "Show me latest artwork of abstract type"
    - "Show me artwork less than 400$ but in large size having baby's face"
    """
    try:
        # Step 1: Use LLM to extract structured filters from the user message
        filters = await parse_artwork_query(req.message)

        # Step 2: Fetch artworks from the Java backend using extracted filters
        artworks = await fetch_artworks(filters)

        # Step 3: Generate a human-readable reply
        reply = await generate_response(req.message, filters, artworks)

        return ChatResponse(
            reply=reply,
            artworks=artworks,
            filters_applied=filters,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
