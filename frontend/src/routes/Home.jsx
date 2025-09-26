import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <section className="card">
      <h2>시작하기</h2>
      <p>
        간단한 설문을 통해 예산과 선호에 맞는 차량을 추천받아 보세요. 진행 상황은 브라우저에 저장되므로 언제든 이어갈 수 있습니다.
      </p>
      <div className="cta-group">
        <Link className="button primary" to="/survey">설문 시작</Link>
        <Link className="button" to="/favorites">즐겨찾기 보기</Link>
      </div>
    </section>
  );
}
