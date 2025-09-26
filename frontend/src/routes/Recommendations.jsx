export default function Recommendations() {
  return (
    <section className="card">
      <h2>추천 결과 (미리보기)</h2>
      <p>
        설문이 완료되면 우선순위 5개 차량이 여기 리스트업됩니다. 각 항목은 점수, 핵심 스펙, 예상 유지비, 즐겨찾기 토글을 포함하게 됩니다.
      </p>
      <ol className="placeholder-list">
        <li>Hyundai Ioniq 5 – 전기식 SUV, 예산 적합도 92%</li>
        <li>Kia EV6 – 크로스오버, 예산 적합도 88%</li>
        <li>Genesis GV80 – 럭셔리 SUV, 옵션 적합도 81%</li>
      </ol>
      <p className="hint">※ API 연동 및 실제 데이터는 BE-4, FE-3 작업에서 연결됩니다.</p>
    </section>
  );
}
