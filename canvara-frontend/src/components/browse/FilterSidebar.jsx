import { useState } from 'react';

const FILTER_SECTIONS = [
  {
    key: 'categories',
    label: 'Category',
    options: ['Landscape', 'Portrait', 'Abstract', 'Still Life', 'Figurative'],
  },
  {
    key: 'styles',
    label: 'Style',
    options: ['Impressionist', 'Realist', 'Expressionist', 'Minimalist', 'Surrealist'],
  },
  {
    key: 'mediums',
    label: 'Medium',
    options: ['Oil', 'Watercolour', 'Acrylic', 'Charcoal', 'Mixed Media'],
  },
  {
    key: 'sizes',
    label: 'Size',
    options: ['Small', 'Medium', 'Large'],
  },
];

function FilterSection({ section, selected, onChange }) {
  const [open, setOpen] = useState(section.key === 'categories');

  return (
    <div className="filter-section">
      <button
        className={`filter-toggle ${open ? 'open' : ''}`}
        onClick={() => setOpen(o => !o)}
        aria-expanded={open}
      >
        {section.label}
        <svg
          className={`chevron ${open ? 'open' : ''}`}
          width="14" height="14" viewBox="0 0 24 24"
          fill="none" stroke="currentColor" strokeWidth="2"
          aria-hidden="true"
        >
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </button>

      {open && (
        <div className="filter-body">
          {section.options.map(opt => (
            <label key={opt} className="filter-option">
              <input
                type="checkbox"
                checked={selected.includes(opt)}
                onChange={() => onChange(section.key, opt)}
              />
              <span className="filter-label">{opt}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  );
}

export default function FilterSidebar({ filters, onFilterChange, activeChips, onRemoveChip, onClearAll }) {
  return (
    <aside className="sidebar" aria-label="Artwork filters">
      <div className="sidebar-header">
        <div className="logo">can<span>vara</span></div>
        <div className="tagline">Genuine art, directly from the artist</div>
      </div>

      <div className="search-wrap">
        <div className="search-inner">
          <svg className="search-icon" width="15" height="15" viewBox="0 0 24 24"
            fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            className="search-input"
            type="text"
            placeholder="Search artist or title…"
            value={filters.search}
            onChange={e => onFilterChange('search', e.target.value)}
            aria-label="Search artworks"
          />
          {filters.search && (
            <button
              className="search-clear"
              onClick={() => onFilterChange('search', '')}
              aria-label="Clear search"
            >
              ×
            </button>
          )}
        </div>
      </div>

      {activeChips.length > 0 && (
        <div className="active-chips" aria-label="Active filters">
          {activeChips.map(({ key, value }) => (
            <span key={`${key}-${value}`} className="chip">
              {value}
              <button
                onClick={() => onRemoveChip(key, value)}
                aria-label={`Remove ${value} filter`}
              >
                ×
              </button>
            </span>
          ))}
          <button className="clear-all" onClick={onClearAll}>
            Clear all
          </button>
        </div>
      )}

      {FILTER_SECTIONS.map(section => (
        <FilterSection
          key={section.key}
          section={section}
          selected={filters[section.key]}
          onChange={onFilterChange}
        />
      ))}
    </aside>
  );
}
