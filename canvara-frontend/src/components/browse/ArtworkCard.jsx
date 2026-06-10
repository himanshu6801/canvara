import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function ArtworkCard({ artwork }) {
    const [imgError, setImgError] = useState(false);
    const navigate = useNavigate();

    const imageUrl = artwork.imageUrl ?? null;

    const formatTag = val =>
        val?.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());

    return (
        <article
            className="art-card"
            aria-label={`${artwork.title} by ${artwork.supplierName}`}
            onClick={() => navigate(`/artworks/${artwork.id}`)}
        >
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
                <p className="art-artist">{artwork.supplierName}</p>
                <div className="art-meta">
          <span className="art-price">
            {artwork.price != null
                ? `£${Number(artwork.price).toLocaleString('en-GB')}`
                : 'Price on request'}
          </span>
                    <span className="art-tag">
            {[formatTag(artwork.categories?.[0]), formatTag(artwork.sizes?.[0])]
                .filter(Boolean)
                .join(' · ')}
          </span>
                </div>
            </div>
        </article>
    );
}