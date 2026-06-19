import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom';
import BrowsePage from './pages/BrowsePage';
import ArtworkDetailPage from './pages/ArtworkDetailPage';
import UploadArtworkPage from './pages/UploadArtworkPage';

function UploadArtworkRoute() {
    const navigate = useNavigate();
    return <UploadArtworkPage onNavigate={(route) => navigate(`/supplier/${route}`)} />;
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/"                  element={<BrowsePage />} />
                <Route path="/browse"            element={<BrowsePage />} />
                <Route path="/artworks/:id"      element={<ArtworkDetailPage />} />
                <Route path="/supplier/upload"   element={<UploadArtworkRoute />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;