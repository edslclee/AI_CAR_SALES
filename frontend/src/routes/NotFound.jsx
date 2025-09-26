import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="card">
      <h2>페이지를 찾을 수 없습니다</h2>
      <p>URL을 다시 확인하거나 홈으로 이동해 주세요.</p>
      <Link className="button" to="/">홈으로</Link>
    </section>
  );
}
