import { useState, useRef } from 'react';
import '../styles/upload-artwork.css';

// ── Constants ────────────────────────────────────────────────────────────────

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

const CATEGORIES = ['LANDSCAPE', 'PORTRAIT', 'ABSTRACT', 'STILL_LIFE', 'FIGURATIVE', 'CITYSCAPE', 'WILDLIFE'];
const MEDIUMS    = ['OIL', 'ACRYLIC', 'WATERCOLOUR', 'CHARCOAL', 'PENCIL', 'INK', 'MIXED_MEDIA', 'DIGITAL'];
const STYLES     = ['IMPRESSIONIST', 'REALIST', 'EXPRESSIONIST', 'MINIMALIST', 'SURREALIST'];
const SIZES      = ['XS', 'S', 'M', 'L', 'XL'];




// ── Sub-components ───────────────────────────────────────────────────────────

function FilterSection({ title, options, selected, onChange }) {
  const [open, setOpen] = useState(true);

  function toggle(option) {
    onChange(
      selected.includes(option)
        ? selected.filter(o => o !== option)
        : [...selected, option]
    );
  }

  return (
    <div className="ua-filter-section">
      <button
        type="button"
        className="ua-filter-toggle"
        onClick={() => setOpen(o => !o)}
        aria-expanded={open}
      >
        <span>
          {title}
          {selected.length > 0 && (
            <span className="ua-filter-count"> · {selected.length} selected</span>
          )}
        </span>
        <span className={`ua-chevron${open ? ' open' : ''}`}>›</span>
      </button>

      {open && (
        <div className="ua-filter-body">
          {options.map(option => (
            <label key={option} className="ua-cb-pill">
              <input
                type="checkbox"
                checked={selected.includes(option)}
                onChange={() => toggle(option)}
              />
              {option}
            </label>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Main component ───────────────────────────────────────────────────────────

export default function UploadArtworkPage({ onBack, onNavigate }) {
  // Image state
  const [imageFile, setImageFile]         = useState(null);
  const [imagePreview, setImagePreview]   = useState(null);
  const [imageFilename, setImageFilename] = useState('');   // returned from POST /files/upload
  const [uploading, setUploading]         = useState(false);
  const [dragOver, setDragOver]           = useState(false);
  const fileInputRef = useRef(null);

  // Form state
  const [title, setTitle]             = useState('');
  const [price, setPrice]             = useState('');
  const [dimensions, setDimensions]   = useState('');
  const [size, setSize]               = useState('');
  const [description, setDescription] = useState('');
  const [categories, setCategories]   = useState([]);
  const [mediums, setMediums]         = useState([]);
  const [styles, setStyles]           = useState([]);

  // Submission state
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors]         = useState({});

  const [supplierEmail, setSupplierEmail] = useState('');

  // ── Image handling ──────────────────────────────────────────────────────

  function handleFileSelect(file) {
    if (!file || !file.type.startsWith('image/')) return;

    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
    setImageFilename('');   // reset any prior upload
    uploadImage(file);
  }

  async function uploadImage(file) {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);

      const res = await fetch(`${API_BASE}/api/upload/artwork`, {
        method: 'POST',
        body: formData
      });

      if (!res.ok) throw new Error('Upload failed');

      const data = await res.json();
      setImageFilename(data.filename);   // e.g. "abc123.jpg"
    } catch (err) {
      console.error('Image upload error:', err);
      setErrors(prev => ({ ...prev, image: 'Image upload failed. Please try again.' }));
    } finally {
      setUploading(false);
    }
  }

  function handleDropZoneClick() {
    fileInputRef.current?.click();
  }

  function handleFileInputChange(e) {
    handleFileSelect(e.target.files?.[0]);
  }

  function handleDrop(e) {
    e.preventDefault();
    setDragOver(false);
    handleFileSelect(e.dataTransfer.files?.[0]);
  }

  function handleDragOver(e) {
    e.preventDefault();
    setDragOver(true);
  }

  function handleDragLeave() {
    setDragOver(false);
  }

  // ── Validation ──────────────────────────────────────────────────────────

  function validate() {
    const errs = {};
    if (!title.trim())       errs.title = 'Title is required.';
    if (!price.trim())       errs.price = 'Price is required.';
    if (isNaN(Number(price)) || Number(price) <= 0) errs.price = 'Enter a valid price.';
    if (!imageFilename)      errs.image = uploading ? 'Image is still uploading.' : 'Please upload an image.';
    if (!supplierEmail.trim()) errs.supplierEmail = 'Supplier email is required.';
    return errs;
  }

  // ── Submit ──────────────────────────────────────────────────────────────

  async function handleSubmit(status) {
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setErrors({});
    setSubmitting(true);

    const payload = {
      title:         title.trim(),
      supplierEmail: supplierEmail.trim(),
      description:   description.trim(),
      price:         parseFloat(price),
      dimensions:    dimensions.trim(),
      size:          size || null,
      imageFilename,
      status,                          // 'DRAFT' or 'AVAILABLE'
      categories,
      mediums,
      styles,

    };

    try {
      const res = await fetch(`${API_BASE}/api/artworks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) throw new Error('Submission failed');

      // Navigate back to listings on success
      onNavigate?.('listings');
    } catch (err) {
      console.error('Submission error:', err);
      setErrors({ submit: 'Something went wrong. Please try again.' });
    } finally {
      setSubmitting(false);
    }
  }

  // ── Render ──────────────────────────────────────────────────────────────

  const canPublish = !!imageFilename && !uploading && !submitting;

  return (
    <div className="ua-page">

      {/* ── Sidebar ── */}
      <aside className="ua-sidebar">
        <div className="ua-sidebar-head">
          <div className="ua-logo">Can<em>vara</em></div>
          <div className="ua-tagline">Genuine art, directly from the artist.</div>
        </div>

        <nav className="ua-nav">
          <button type="button" className="ua-nav-item" onClick={() => onNavigate?.('listings')}>
            <span className="ua-nav-icon">⊞</span> My Listings
          </button>
          <button type="button" className="ua-nav-item active">
            <span className="ua-nav-icon">↑</span> Upload Artwork
          </button>
          <button type="button" className="ua-nav-item" onClick={() => onNavigate?.('analytics')}>
            <span className="ua-nav-icon">↗</span> Analytics
          </button>
          <button type="button" className="ua-nav-item" onClick={() => onNavigate?.('profile')}>
            <span className="ua-nav-icon">◎</span> Profile
          </button>
        </nav>
      </aside>

      {/* ── Main ── */}
      <main className="ua-main">

        {/* Breadcrumb topbar */}
        <div className="ua-topbar">
          <button type="button" className="ua-bc-link" onClick={() => onNavigate?.('listings')}>
            Dashboard
          </button>
          <span className="ua-bc-sep">›</span>
          <span className="ua-bc-current">Upload Artwork</span>
        </div>

        {/* Two-column body */}
        <div className="ua-body">

          {/* ── Image column ── */}
          <div className="ua-img-col">
            <div
              className={`ua-upload-zone${dragOver ? ' drag-over' : ''}${errors.image ? ' has-error' : ''}`}
              onClick={handleDropZoneClick}
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              role="button"
              tabIndex={0}
              aria-label="Upload artwork image"
              onKeyDown={e => e.key === 'Enter' && handleDropZoneClick()}
            >
              <div className="ua-upload-accent" />

              {imagePreview ? (
                <>
                  <img src={imagePreview} alt="Artwork preview" className="ua-preview-img" />
                  {uploading && (
                    <div className="ua-upload-overlay">
                      <span className="ua-upload-spinner" />
                      <span className="ua-upload-overlay-text">Uploading…</span>
                    </div>
                  )}
                  {imageFilename && !uploading && (
                    <div className="ua-upload-success-badge">✓ Uploaded</div>
                  )}
                </>
              ) : (
                <div className="ua-upload-empty">
                  <div className="ua-upload-icon">↑</div>
                  <div className="ua-upload-primary">Drop your artwork here</div>
                  <div className="ua-upload-sub">
                    or click to browse · JPG, PNG, WEBP · max 10 MB
                  </div>
                </div>
              )}
            </div>

            {errors.image && (
              <p className="ua-field-error">{errors.image}</p>
            )}

            <p className="ua-img-caption">
              This will be the primary listing image visible to buyers.
            </p>

            {/* Hidden file input */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="ua-file-input"
              onChange={handleFileInputChange}
            />
          </div>

          {/* ── Form column ── */}
          <div className="ua-form-col">

            <div className={`ua-field${errors.title ? ' field-error' : ''}`}>
              <label className="ua-lbl" htmlFor="ua-title">Title *</label>
              <input
                id="ua-title"
                className="ua-input"
                value={title}
                onChange={e => setTitle(e.target.value)}
                placeholder="e.g. Morning Light Over the Ghats"
                maxLength={200}
              />
              {errors.title && <span className="ua-field-error">{errors.title}</span>}
            </div>

            <div className={`ua-field${errors.supplierEmail ? ' field-error' : ''}`}>
              <label className="ua-lbl" htmlFor="ua-email">Supplier Email *</label>
              <input
                  id="ua-email"
                  className="ua-input"
                  type="email"
                  value={supplierEmail}
                  onChange={e => setSupplierEmail(e.target.value)}
                  placeholder="e.g. artist@example.com"
              />
              {errors.supplierEmail && <span className="ua-field-error">{errors.supplierEmail}</span>}
            </div>

            <div className={`ua-field${errors.price ? ' field-error' : ''}`}>
              <label className="ua-lbl" htmlFor="ua-price">Price *</label>
              <div className="ua-input-row">
                <span className="ua-input-prefix">₹</span>
                <input
                  id="ua-price"
                  className="ua-input prefixed"
                  value={price}
                  onChange={e => setPrice(e.target.value)}
                  placeholder="0.00"
                  inputMode="decimal"
                />
              </div>
              {errors.price && <span className="ua-field-error">{errors.price}</span>}
            </div>

            <div className="ua-field">
              <label className="ua-lbl" htmlFor="ua-dimensions">Dimensions</label>
              <input
                id="ua-dimensions"
                className="ua-input"
                value={dimensions}
                onChange={e => setDimensions(e.target.value)}
                placeholder="e.g. 60 × 80 cm"
                maxLength={100}
              />
            </div>

            <div className="ua-field">
              <span className="ua-lbl">Size</span>
              <div className="ua-seg">
                {SIZES.map(s => (
                  <button
                    key={s}
                    type="button"
                    className={`ua-seg-btn${size === s ? ' selected' : ''}`}
                    onClick={() => setSize(prev => prev === s ? '' : s)}
                    aria-pressed={size === s}
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>

            <div className="ua-field">
              <label className="ua-lbl" htmlFor="ua-description">Description</label>
              <textarea
                id="ua-description"
                className="ua-textarea"
                value={description}
                onChange={e => setDescription(e.target.value)}
                placeholder="Describe the work — mood, technique, inspiration…"
                rows={4}
              />
            </div>

            <FilterSection
              title="Categories"
              options={CATEGORIES}
              selected={categories}
              onChange={setCategories}
            />

            <FilterSection
              title="Mediums"
              options={MEDIUMS}
              selected={mediums}
              onChange={setMediums}
            />

            <FilterSection
              title="Styles"
              options={STYLES}
              selected={styles}
              onChange={setStyles}
            />

          </div>
        </div>

        {/* ── Footer ── */}
        <div className="ua-footer">
          {errors.submit && (
            <span className="ua-submit-error">{errors.submit}</span>
          )}
          <button
            type="button"
            className="ua-btn-ghost"
            onClick={() => handleSubmit('DRAFT')}
            disabled={submitting}
          >
            Save as Draft
          </button>
          <button
            type="button"
            className="ua-btn-primary"
            onClick={() => handleSubmit('AVAILABLE')}
            disabled={!canPublish}
          >
            {submitting ? 'Publishing…' : 'Publish Artwork'}
          </button>
        </div>

      </main>
    </div>
  );
}
