export default function TopBar({ totalCount, sort, sortOptions, onSortChange, loading }) {
  return (
    <div className="top-bar">
      <p className="result-count" aria-live="polite" aria-atomic="true">
        {loading ? (
          <span className="count-loading">Loading…</span>
        ) : (
          <><strong>{totalCount.toLocaleString()}</strong> artwork{totalCount !== 1 ? 's' : ''}</>
        )}
      </p>

      <select
        className="sort-select"
        value={sort}
        onChange={e => onSortChange(e.target.value)}
        aria-label="Sort artworks"
      >
        {sortOptions.map(opt => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
    </div>
  );
}
