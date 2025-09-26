import FavoritesList from '../features/favorites/FavoritesList.jsx';

export default function Favorites() {
  return (
    <section className="card">
      <h2>즐겨찾기</h2>
      <p className="survey-step-description">
        추천 결과에서 저장한 차량들이 최신 순으로 정리됩니다.
      </p>
      <FavoritesList />
    </section>
  );
}
