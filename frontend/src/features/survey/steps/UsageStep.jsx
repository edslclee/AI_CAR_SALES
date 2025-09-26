export default function UsageStep({ data, errors, onChange }) {
  const handleChange = (evt) => {
    const { name, value } = evt.target;
    onChange({ [name]: value });
  };

  return (
    <div className="survey-form">
      <p className="survey-step-description">
        차량을 활용할 주요 목적과 평균 탑승 인원을 선택하면 차량 크기와 연비 우선순위를 조정할 수 있습니다.
      </p>
      <div className="field-group">
        <label htmlFor="purpose">사용 목적</label>
        <select
          id="purpose"
          name="purpose"
          value={data.purpose}
          onChange={handleChange}
        >
          <option value="">선택해 주세요</option>
          <option value="commute">출퇴근/도심</option>
          <option value="family">가족 나들이</option>
          <option value="business">비즈니스/고객 응대</option>
          <option value="leisure">레저/캠핑</option>
        </select>
        {errors?.purpose && <span className="error-text">{errors.purpose}</span>}
      </div>

      <div className="field-group">
        <label htmlFor="passengers">평균 탑승 인원</label>
        <select
          id="passengers"
          name="passengers"
          value={data.passengers}
          onChange={handleChange}
        >
          <option value="">선택해 주세요</option>
          <option value="1-2">1~2명</option>
          <option value="3-4">3~4명</option>
          <option value="5-7">5~7명</option>
          <option value="8+">8명 이상</option>
        </select>
        {errors?.passengers && <span className="error-text">{errors.passengers}</span>}
      </div>
    </div>
  );
}
