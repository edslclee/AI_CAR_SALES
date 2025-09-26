export default function Favorites() {
  return (
    <section className="card">
      <h2>즐겨찾기</h2>
      <p>
        즐겨찾기한 차량이 최근 저장 순으로 정렬되어 표시됩니다. 추후 비교표로 보내거나 상담 요청을 연결할 수 있습니다.
      </p>
      <div className="empty-state">
        <p>아직 즐겨찾기한 차량이 없습니다.</p>
        <p>추천 결과에서 하트 아이콘을 눌러 목록을 채워보세요.</p>
      </div>
    </section>
  );
}
