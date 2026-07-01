import os
import json
from openai import AsyncOpenAI
from app.models.schemas import ArtworkFilters

client = AsyncOpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# ---------------------------------------------------------------------------
# System prompt: instructs the LLM to extract structured filters
# ---------------------------------------------------------------------------
FILTER_EXTRACTION_PROMPT = """
You are a filter extraction engine for an art marketplace.
Given a user's natural language query, extract search filters and return ONLY valid JSON.

JSON schema:
{
  "type": string or null,         // art style: "abstract", "portrait", "landscape", "modern", etc.
  "max_price": number or null,    // maximum price in USD
  "min_price": number or null,    // minimum price in USD
  "size": string or null,         // "small", "medium", "large"
  "keywords": [string] or null,   // descriptive terms like ["baby", "face", "colorful"]
  "sort_by": string or null       // "latest", "price_asc", "price_desc"
}

Rules:
- Return ONLY the JSON object, no explanation.
- Use null for fields not mentioned.
- For "latest" / "newest" queries set sort_by to "latest".
- Prices like "$400", "400 dollars", "under 400" → max_price: 400.
- Sizes: small (<50cm), medium (50-100cm), large (>100cm).
"""


async def parse_artwork_query(user_message: str) -> ArtworkFilters:
    """Call the LLM to extract structured filters from a natural language query."""
    response = await client.chat.completions.create(
        model=os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
        messages=[
            {"role": "system", "content": FILTER_EXTRACTION_PROMPT},
            {"role": "user", "content": user_message},
        ],
        temperature=0,
        response_format={"type": "json_object"},
    )
    raw = json.loads(response.choices[0].message.content)
    return ArtworkFilters(**raw)


# ---------------------------------------------------------------------------
# Generate a conversational reply summarising results
# ---------------------------------------------------------------------------
REPLY_PROMPT = """
You are a friendly art consultant for an online gallery called Canvara.
The user asked: "{query}"
The system extracted these filters: {filters}
We found {count} artwork(s).

Write a short, warm 1-2 sentence reply summarising what was found.
If 0 results, suggest the user broaden their search.
Do NOT list artwork details — they are shown separately.
"""


async def generate_response(query: str, filters: ArtworkFilters, artworks: list) -> str:
    """Generate a human-readable reply about the search results."""
    prompt = REPLY_PROMPT.format(
        query=query,
        filters=filters.model_dump(exclude_none=True),
        count=len(artworks),
    )
    response = await client.chat.completions.create(
        model=os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
        messages=[{"role": "user", "content": prompt}],
        temperature=0.7,
        max_tokens=100,
    )
    return response.choices[0].message.content.strip()
