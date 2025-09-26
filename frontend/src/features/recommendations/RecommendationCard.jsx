import { Heart, Scale } from 'lucide-react';

export default function RecommendationCard({ item, isFavorite, isCompared, onToggleFavorite, onToggleCompare }) {
  return (
    <article className="reco-card">
      <div className="reco-card__rank">#{item.rank}</div>
      <img className="reco-card__image" src={item.image} alt={`${item.name} ${item.trim}`} />
      <div className="reco-card__body">
        <div>
          <h3>{item.name}</h3>
          <p className="reco-card__trim">{item.trim}</p>
          <dl className="reco-specs">
            <div>
              <dt>가격</dt>
              <dd>{item.price.toLocaleString()} 만원</dd>
            </div>
            <div>
              <dt>차종</dt>
              <dd>{item.bodyType}</dd>
            </div>
            <div>
              <dt>연료</dt>
              <dd>{item.fuelType}</dd>
            </div>
            <div>
              <dt>효율</dt>
              <dd>{item.efficiency}</dd>
            </div>
          </dl>
        </div>
        <div className="reco-score">
          <span className="reco-score__value">{item.score}</span>
          <span className="reco-score__label">적합도</span>
        </div>
      </div>
      <div className="reco-actions">
        <button type="button" className={`icon-button ${isFavorite ? 'active' : ''}`} onClick={() => onToggleFavorite(item)}>
          <Heart size={18} />
          <span>즐겨찾기</span>
        </button>
        <button type="button" className={`icon-button ${isCompared ? 'active' : ''}`} onClick={() => onToggleCompare(item.id)}>
          <Scale size={18} />
          <span>비교</span>
        </button>
      </div>
      <div className="reco-breakdown">
        <h4>세부 점수</h4>
        <ul>
          {Object.entries(item.scoreBreakdown).map(([key, value]) => (
            <li key={key}>
              <span>{labelScoreKey(key)}</span>
              <span>{value}</span>
            </li>
          ))}
        </ul>
      </div>
    </article>
  );
}

function labelScoreKey(key) {
  switch (key) {
    case 'budget':
      return '예산 적합도';
    case 'bodyType':
      return '차종 선호';
    case 'options':
      return '옵션 적합도';
    default:
      return key;
  }
}
