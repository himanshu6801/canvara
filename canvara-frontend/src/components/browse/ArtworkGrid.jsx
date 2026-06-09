import ArtworkCard from './ArtworkCard';

const PAGE_SIZE = 20;

export default function ArtworkGrid({ artworks, loading, error, page, onPageChange, totalCount }) {
  if (error) {
    return (
      <div className="grid-state">
        <p className="state-title">Something went wrong</p>
        <p className="state-sub">{error}</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="masonry-grid">
        {Array.from({ length: 9 }).map((_, i) => (
          <div key={i} className="skeleton-card" style={{ height: `${180 + (i % 4) * 50}px` }} />
        ))}
      </div>
    );
  }

  if (!artworks.length) {
    return (
      <div className="grid-state">
        <p className="state-title">No artworks found</p>
        <p className="state-sub">Try adjusting your filters or search term.</p>
      </div>
    );
  }

  const totalPages = Math.ceil(totalCount / PAGE_SIZE);

  return (
    <>
      <div className="masonry-grid">
        {artworks.map(artwork => (
          <ArtworkCard key={artwork.id} artwork={artwork} />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="page-btn"
            onClick={() => onPageChange(p => p - 1)}
            disabled={page === 0}
            aria-label="Previous page"
          >
            ← Prev
          </button>
          <span className="page-info">
            {page + 1} / {totalPages}
          </span>
          <button
            className="page-btn"
            onClick={() => onPageChange(p => p + 1)}
            disabled={page >= totalPages - 1}
            aria-label="Next page"
          >
            Next →
          </button>
        </div>
      )}
    </>
  );
}
