import { useState, useEffect, useCallback } from 'react';
import FilterSidebar from '../components/browse/FilterSidebar';
import ArtworkGrid from '../components/browse/ArtworkGrid';
import TopBar from '../components/browse/TopBar';
import '../styles/browse.css';

const SORT_OPTIONS = [
  { label: 'Newest first', value: 'newest' },
  { label: 'Price: low to high', value: 'price_asc' },
  { label: 'Price: high to low', value: 'price_desc' },
];

export default function BrowsePage() {
  const [artworks, setArtworks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [totalCount, setTotalCount] = useState(0);

  const [filters, setFilters] = useState({
    search: '',
    categories: [],
    styles: [],
    mediums: [],
    sizes: [],
  });

  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(0);

  const buildQueryParams = useCallback(() => {
    const params = new URLSearchParams();
    if (filters.search) params.set('search', filters.search);
    if (filters.categories.length) params.set('categories', filters.categories.join(','));
    if (filters.styles.length) params.set('styles', filters.styles.join(','));
    if (filters.mediums.length) params.set('mediums', filters.mediums.join(','));
    if (filters.sizes.length) params.set('sizes', filters.sizes.join(','));
    params.set('sort', sort);
    params.set('page', page);
    params.set('size', 20);
    return params.toString();
  }, [filters, sort, page]);

  useEffect(() => {
    const controller = new AbortController();

    const fetchArtworks = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/artworks?${buildQueryParams()}`,
          { signal: controller.signal }
        );
        if (!res.ok) throw new Error('Failed to fetch artworks');
        const data = await res.json();
        // Expects PagedResponse<ArtworkSummaryResponse>
        setArtworks(data.content ?? []);
        setTotalCount(data.totalElements ?? 0);
      } catch (err) {
        if (err.name !== 'AbortError') setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchArtworks();
    return () => controller.abort();
  }, [buildQueryParams]);

  // Reset to page 0 when filters/sort change
  useEffect(() => {
    setPage(0);
  }, [filters, sort]);

  const handleFilterChange = (key, value) => {
    setFilters(prev => {
      const current = prev[key];
      if (Array.isArray(current)) {
        const updated = current.includes(value)
          ? current.filter(v => v !== value)
          : [...current, value];
        return { ...prev, [key]: updated };
      }
      return { ...prev, [key]: value };
    });
  };

  const removeFilter = (key, value) => {
    setFilters(prev => ({
      ...prev,
      [key]: Array.isArray(prev[key]) ? prev[key].filter(v => v !== value) : '',
    }));
  };

  const clearAllFilters = () => {
    setFilters({ search: '', categories: [], styles: [], mediums: [], sizes: [] });
  };

  const activeChips = [
    ...filters.categories.map(v => ({ key: 'categories', value: v })),
    ...filters.styles.map(v => ({ key: 'styles', value: v })),
    ...filters.mediums.map(v => ({ key: 'mediums', value: v })),
    ...filters.sizes.map(v => ({ key: 'sizes', value: v })),
  ];

  return (
    <div className="browse-layout">
      <FilterSidebar
        filters={filters}
        onFilterChange={handleFilterChange}
        activeChips={activeChips}
        onRemoveChip={removeFilter}
        onClearAll={clearAllFilters}
      />
      <main className="browse-main">
        <TopBar
          totalCount={totalCount}
          sort={sort}
          sortOptions={SORT_OPTIONS}
          onSortChange={setSort}
          loading={loading}
        />
        <ArtworkGrid
          artworks={artworks}
          loading={loading}
          error={error}
          page={page}
          onPageChange={setPage}
          totalCount={totalCount}
        />
      </main>
    </div>
  );
}
