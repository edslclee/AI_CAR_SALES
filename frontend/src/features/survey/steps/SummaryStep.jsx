export default function SummaryStep({ data }) {
  return (
    <div className="survey-form">
      <p className="survey-step-description">
        입력한 설문 정보를 다시 한번 확인한 뒤 제출해 주세요.
      </p>
      <div className="summary-card">
        <h3>예산 및 구매 방식</h3>
        <div className="summary-entry">
          <span>예산 범위</span>
          <span>{data.budget.minBudget} ~ {data.budget.maxBudget} 만원</span>
        </div>
        <div className="summary-entry">
          <span>구매 방식</span>
          <span>{labelPurchase(data.budget.purchaseType)}</span>
        </div>
      </div>

      <div className="summary-card">
        <h3>사용 목적</h3>
        <div className="summary-entry">
          <span>주요 용도</span>
          <span>{labelPurpose(data.usage.purpose)}</span>
        </div>
        <div className="summary-entry">
          <span>탑승 인원</span>
          <span>{labelPassengers(data.usage.passengers)}</span>
        </div>
      </div>

      <div className="summary-card">
        <h3>선호 및 조건</h3>
        <div className="summary-entry">
          <span>선호 차종</span>
          <span>{data.preferences.bodyTypes.join(', ') || '-'}</span>
        </div>
        <div className="summary-entry">
          <span>선호 브랜드</span>
          <span>{data.preferences.brands.join(', ') || '-'}</span>
        </div>
        <div className="summary-entry">
          <span>최소 연식</span>
          <span>{data.condition.minYear || '-'}</span>
        </div>
        <div className="summary-entry">
          <span>최대 주행거리</span>
          <span>{data.condition.maxMileage ? `${data.condition.maxMileage} km` : '-'}</span>
        </div>
      </div>

      {data.options.mustHaves && (
        <div className="summary-card">
          <h3>추가 요청사항</h3>
          <p>{data.options.mustHaves}</p>
        </div>
      )}
    </div>
  );
}

function labelPurchase(value) {
  switch (value) {
    case 'lease':
      return '리스/렌트';
    case 'finance':
      return '할부/금융';
    case 'cash':
      return '일시불';
    default:
      return '-';
  }
}

function labelPurpose(value) {
  switch (value) {
    case 'commute':
      return '출퇴근/도심';
    case 'family':
      return '가족 나들이';
    case 'business':
      return '비즈니스/고객 응대';
    case 'leisure':
      return '레저/캠핑';
    default:
      return '-';
  }
}

function labelPassengers(value) {
  switch (value) {
    case '1-2':
      return '1~2명';
    case '3-4':
      return '3~4명';
    case '5-7':
      return '5~7명';
    case '8+':
      return '8명 이상';
    default:
      return '-';
  }
}
