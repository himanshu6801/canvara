import { BrowserRouter, Routes, Route } from 'react-router-dom';
import BrowsePage from './pages/BrowsePage';
import ArtworkDetailPage from './pages/ArtworkDetailPage';

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/"             element={<BrowsePage />} />
          <Route path="/browse"       element={<BrowsePage />} />
          <Route path="/artworks/:id" element={<ArtworkDetailPage />} />
        </Routes>
      </BrowserRouter>
  );
}

export default App;