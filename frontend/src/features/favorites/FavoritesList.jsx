import { useEffect, useState } from 'react';
import { fetchFavorites, removeFavorite } from './favoritesApi.js';
import './favorites.css';

export default function FavoritesList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;
    async function load() {
      setLoading(true);
      try {
        const data = await fetchFavorites();
        if (isMounted) {
          setItems(data);
        }
      } catch (err) {
        if (isMounted) {
          setError('즐겨찾기를 불러오지 못했습니다.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }
    load();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleRemove = async (carId) => {
    try {
      await removeFavorite(carId);
      setItems((prev) => prev.filter((item) => item.id !== carId));
    } catch (err) {
      setError('즐겨찾기 삭제 중 문제가 발생했습니다.');
    }
  };

  if (loading) {
    return <div className="card">즐겨찾기를 불러오는 중입니다...</div>;
  }

  if (error) {
    return <div className="card error-text">{error}</div>;
  }

  if (items.length === 0) {
    return (
      <div className="favorites-empty">
        아직 즐겨찾기한 차량이 없습니다. 추천 결과에서 마음에 드는 차량을 저장해 보세요.
      </div>
    );
  }

  return (
    <div className="favorites-grid">
      {items.map((item) => (
        <article key={item.id} className="favorite-card">
          <h3>{item.name}</h3>
          <p className="favorite-card__meta">{item.trim} · {item.bodyType} · {item.fuelType}</p>
          <div className="favorite-card__footer">
            <span>{item.price.toLocaleString()} 만원</span>
            <button type="button" onClick={() => handleRemove(item.id)}>삭제</button>
          </div>
          <small>저장 시각: {new Date(item.savedAt).toLocaleString()}</small>
        </article>
      ))}
    </div>
  );
}
