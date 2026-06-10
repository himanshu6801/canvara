import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/artwork-detail.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

export default function ArtworkDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [artwork, setArtwork] = useState(null);
  const [related, setRelated] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imgError, setImgError] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    const fetchArtwork = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`${API_BASE}/api/artworks/${id}`, {
          signal: controller.signal,
        });
        if (!res.ok) throw new Error('Artwork not found');
        const data = await res.json();
        setArtwork(data);

        // Fetch related artworks by same supplier
        const relRes = await fetch(
          `${API_BASE}/api/artworks?supplierId=${data.supplierId}&size=4&exclude=${id}`,
          { signal: controller.signal }
        );
        if (relRes.ok) {
          const relData = await relRes.json();
          setRelated((relData.content ?? []).filter(a => a.id !== data.id).slice(0, 4));
        }
      } catch (err) {
        if (err.name !== 'AbortError') setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchArtwork();
    return () => controller.abort();
  }, [id]);

  if (loading) return <DetailSkeleton />;
  if (error)   return <DetailError message={error} onBack={() => navigate('/browse')} />;
  if (!artwork) return null;

  const formatTags = arr =>
    (arr ?? []).map(v =>
      v.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())
    );

  const storyTypeLabel = artwork.storyType
    ? artwork.storyType.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())
    : null;

  return (
    <div className="detail-page">
      <aside className="detail-sidebar">
        <div className="detail-sidebar-head">
          <div className="detail-logo">can<em>vara</em></div>
          <div className="detail-tagline">Genuine art, directly from the artist</div>
        </div>
        <button className="detail-back-btn" onClick={() => navigate('/browse')}>
          <span className="detail-back-arrow">←</span>
          Back to browse
        </button>
      </aside>

      <main className="detail-main">
        <nav className="detail-topbar" aria-label="Breadcrumb">
          <button className="detail-breadcrumb-link" onClick={() => navigate('/browse')}>
            Browse
          </button>
          <span className="detail-breadcrumb-sep">›</span>
          {artwork.categories?.[0] && (
            <>
              <button
                className="detail-breadcrumb-link"
                onClick={() => navigate(`/browse?categories=${artwork.categories[0]}`)}
              >
                {formatTags([artwork.categories[0]])[0]}
              </button>
              <span className="detail-breadcrumb-sep">›</span>
            </>
          )}
          <span className="detail-breadcrumb-current">{artwork.title}</span>
        </nav>

        <div className="detail-body">
          <div className="detail-img-col">
            <div className="detail-img-frame">
              <div className="detail-img-accent" aria-hidden="true" />
              {artwork.imageUrl && !imgError ? (
                <img
                  src={artwork.imageUrl}
                  alt={artwork.title}
                  className="detail-img"
                  onError={() => setImgError(true)}
                />
              ) : (
                <div className="detail-img-placeholder" aria-hidden="true" />
              )}
            </div>
            <p className="detail-img-caption">
              {artwork.title}
              {artwork.createdAt ? `, ${new Date(artwork.createdAt).getFullYear()}` : ''}
              {artwork.mediums?.[0] ? ` · ${formatTags([artwork.mediums[0]])[0]} on canvas` : ''}
              {artwork.dimensions ? ` · ${artwork.dimensions}` : ''}
            </p>
          </div>

          <div className="detail-info-col">
            <div className="detail-artist-row">
              <div className="detail-avatar" aria-hidden="true">
                {artwork.supplierName?.split(' ').map(w => w[0]).join('').slice(0, 2)}
              </div>
              <span className="detail-artist-name">{artwork.supplierName}</span>
            </div>

            <h1 className="detail-title">{artwork.title}</h1>

            <div className="detail-price">
              {artwork.price != null
                ? `£${Number(artwork.price).toLocaleString('en-GB')}`
                : 'Price on request'}
            </div>

            <hr className="detail-divider" />

            <div className="detail-tag-group">
              {artwork.categories?.length > 0 && (
                <div className="detail-tag-row">
                  <span className="detail-tag-lbl">Category</span>
                  <div className="detail-tags">
                    {formatTags(artwork.categories).map(v => (
                      <span key={v} className="detail-tag">{v}</span>
                    ))}
                  </div>
                </div>
              )}
              {artwork.styles?.length > 0 && (
                <div className="detail-tag-row">
                  <span className="detail-tag-lbl">Style</span>
                  <div className="detail-tags">
                    {formatTags(artwork.styles).map(v => (
                      <span key={v} className="detail-tag">{v}</span>
                    ))}
                  </div>
                </div>
              )}
              {artwork.mediums?.length > 0 && (
                <div className="detail-tag-row">
                  <span className="detail-tag-lbl">Medium</span>
                  <div className="detail-tags">
                    {formatTags(artwork.mediums).map(v => (
                      <span key={v} className="detail-tag">{v}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <hr className="detail-divider" />

            <div className="detail-meta-grid">
              {artwork.dimensions && (
                <div className="detail-mc">
                  <div className="detail-mc-lbl">Dimensions</div>
                  <div className="detail-mc-val">{artwork.dimensions}</div>
                </div>
              )}
              <div className="detail-mc">
                <div className="detail-mc-lbl">Status</div>
                <div className={`detail-mc-val ${artwork.status === 'AVAILABLE' ? 'detail-mc-available' : ''}`}>
                  {artwork.status?.charAt(0) + artwork.status?.slice(1).toLowerCase()}
                </div>
              </div>
              {artwork.createdAt && (
                <div className="detail-mc">
                  <div className="detail-mc-lbl">Listed</div>
                  <div className="detail-mc-val">
                    {new Date(artwork.createdAt).toLocaleDateString('en-GB', {
                      day: 'numeric', month: 'short', year: 'numeric',
                    })}
                  </div>
                </div>
              )}
              <div className="detail-mc">
                <div className="detail-mc-lbl">Artwork ID</div>
                <div className="detail-mc-val detail-mc-id">#{String(artwork.id).padStart(5, '0')}</div>
              </div>
            </div>

            {artwork.description && (
              <>
                <hr className="detail-divider" />
                <p className="detail-desc">{artwork.description}</p>
              </>
            )}

            <button
              className="detail-cta"
              disabled={artwork.status !== 'AVAILABLE'}
              onClick={() => navigate(`/artworks/${id}/request`)}
            >
              {artwork.status === 'AVAILABLE' ? 'Request to purchase' : 'Unavailable'}
            </button>
          </div>
        </div>

        {artwork.storyContent && (
          <section className="detail-story" aria-label="Artist's story">
            <div className="detail-story-eyebrow">
              <div className="detail-story-line" aria-hidden="true" />
              <span className="detail-story-eye-text">Artist's story</span>
            </div>
            {storyTypeLabel && (
              <span className="detail-story-badge">{storyTypeLabel}</span>
            )}
            {artwork.storyTitle && (
              <p className="detail-story-title">{artwork.storyTitle}</p>
            )}
            <div className="detail-story-body">
              {artwork.storyContent.split('\n').map((line, i) => (
                <span key={i}>{line}<br /></span>
              ))}
            </div>
          </section>
        )}

        {related.length > 0 && (
          <section className="detail-related" aria-label="More by this artist">
            <p className="detail-related-heading">More by {artwork.supplierName}</p>
            <div className="detail-related-grid">
              {related.map(rel => (
                <RelatedCard key={rel.id} artwork={rel} onClick={() => navigate(`/artworks/${rel.id}`)} />
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

function RelatedCard({ artwork, onClick }) {
  const [imgError, setImgError] = useState(false);
  return (
    <article className="detail-rel-card" onClick={onClick} role="button" tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && onClick()}
      aria-label={`${artwork.title} by ${artwork.supplierName}`}>
      <div className="detail-rel-img-wrap">
        {artwork.imageUrl && !imgError ? (
          <img src={artwork.imageUrl} alt={artwork.title}
            className="detail-rel-img" onError={() => setImgError(true)} loading="lazy" />
        ) : (
          <div className="detail-rel-img-placeholder" />
        )}
      </div>
      <div className="detail-rel-info">
        <div className="detail-rel-title">{artwork.title}</div>
        <div className="detail-rel-artist">{artwork.supplierName}</div>
        <div className="detail-rel-price">
          {artwork.price != null ? `£${Number(artwork.price).toLocaleString('en-GB')}` : '—'}
        </div>
      </div>
    </article>
  );
}

function DetailSkeleton() {
  return (
    <div className="detail-page">
      <aside className="detail-sidebar">
        <div className="detail-sidebar-head">
          <div className="detail-logo">can<em>vara</em></div>
          <div className="detail-tagline">Genuine art, directly from the artist</div>
        </div>
      </aside>
      <main className="detail-main">
        <div className="detail-body" style={{ padding: '28px' }}>
          <div className="detail-skeleton-img" />
          <div className="detail-skeleton-info">
            {[80, 200, 60, 100, 160, 140, 120].map((w, i) => (
              <div key={i} className="detail-skeleton-line" style={{ width: `${w}px` }} />
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}

function DetailError({ message, onBack }) {
  return (
    <div className="detail-page">
      <aside className="detail-sidebar">
        <div className="detail-sidebar-head">
          <div className="detail-logo">can<em>vara</em></div>
          <div className="detail-tagline">Genuine art, directly from the artist</div>
        </div>
        <button className="detail-back-btn" onClick={onBack}>
          <span className="detail-back-arrow">←</span>
          Back to browse
        </button>
      </aside>
      <main className="detail-main">
        <div className="detail-error-state">
          <p className="detail-error-title">Artwork not found</p>
          <p className="detail-error-sub">{message}</p>
          <button className="detail-cta" style={{ width: 'auto', padding: '10px 24px' }} onClick={onBack}>
            Back to browse
          </button>
        </div>
      </main>
    </div>
  );
}
