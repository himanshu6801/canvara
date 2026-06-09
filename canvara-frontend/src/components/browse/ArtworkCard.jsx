import { useState } from 'react';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

export default function ArtworkCard({ artwork }) {
  const [imgError, setImgError] = useState(false);

  const imageUrl = artwork.imageFilename
    ? `${API_BASE}/api/files/${artwork.imageFilename}`
    : null;

  return (
    <article className="art-card" aria-label={`${artwork.title} by ${artwork.artistName}`}>
      <div className="art-img-wrap">
        {imageUrl && !imgError ? (
          <img
            src={imageUrl}
            alt={artwork.title}
            className="art-img"
            onError={() => setImgError(true)}
            loading="lazy"
          />
        ) : (
          <div className="art-img-placeholder" aria-hidden="true" />
        )}
      </div>

      <div className="art-info">
        <p className="art-title">{artwork.title}</p>
        <p className="art-artist">{artwork.artistName}</p>
        <div className="art-meta">
          <span className="art-price">
            {artwork.price != null
              ? `£${Number(artwork.price).toLocaleString('en-GB')}`
              : 'Price on request'}
          </span>
          <span className="art-tag">
            {[artwork.category, artwork.size].filter(Boolean).join(' · ')}
          </span>
        </div>
      </div>
    </article>
  );
}
