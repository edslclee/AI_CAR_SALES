import { NavLink, Route, Routes } from 'react-router-dom';
import Home from './routes/Home.jsx';
import Survey from './routes/Survey.jsx';
import Recommendations from './routes/Recommendations.jsx';
import Favorites from './routes/Favorites.jsx';
import NotFound from './routes/NotFound.jsx';
import './App.css';

export default function App() {
  return (
    <div className="layout">
      <header className="topbar">
        <div className="brand">AI Car Sales</div>
        <nav className="nav-links">
          <NavLink to="/" end>홈</NavLink>
          <NavLink to="/survey">설문</NavLink>
          <NavLink to="/results">추천</NavLink>
          <NavLink to="/favorites">즐겨찾기</NavLink>
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/survey" element={<Survey />} />
          <Route path="/results" element={<Recommendations />} />
          <Route path="/favorites" element={<Favorites />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>

      <footer className="footer">
        <small>© {new Date().getFullYear()} AI Car Sales MVP</small>
      </footer>
    </div>
  );
}
