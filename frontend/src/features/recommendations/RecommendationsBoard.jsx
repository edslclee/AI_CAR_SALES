import { useRecommendations } from './useRecommendations.js';
import RecommendationCard from './RecommendationCard.jsx';
import './recommendations.css';

export default function RecommendationsBoard({ recommendationId }) {
  const {
    items,
    compareList,
    compareItems,
    favorites,
    loading,
    error,
    toggleCompare,
    toggleFavorite
  } = useRecommendations(recommendationId);

  if (loading) {
    return <div className="card">추천 결과를 불러오는 중입니다...</div>;
  }

  if (error) {
    return <div className="card error-text">{error}</div>;
  }

  return (
    <div className="recommendations-layout">
      <div className="reco-grid">
        {items.map((item) => (
          <RecommendationCard
            key={item.id}
            item={item}
            isFavorite={favorites.has(item.id)}
            isCompared={compareList.includes(item.id)}
            onToggleFavorite={toggleFavorite}
            onToggleCompare={toggleCompare}
          />
        ))}
      </div>
      <aside className="compare-panel">
        <h4>비교표 ({compareList.length}/4)</h4>
        <p className="hint">최대 4개 차량을 선택해 비교할 수 있습니다.</p>
        {compareItems.length === 0 ? (
          <div className="favorites-empty">비교할 차량을 선택하세요.</div>
        ) : (
          <div className="compare-list">
            {compareItems.map((item) => (
              <div key={item.id} className="compare-list__item">
                <span>{item.name}</span>
                <button type="button" onClick={() => toggleCompare(item.id)}>제거</button>
              </div>
            ))}
          </div>
        )}
        <button type="button" className="button primary" disabled={compareItems.length < 2}>
          비교표 보기 (미구현)
        </button>
      </aside>
    </div>
  );
}
