export default function ConditionStep({ data, errors, onChange }) {
  const handleChange = (evt) => {
    const { name, value } = evt.target;
    onChange({ [name]: value });
  };

  return (
    <div className="survey-form">
      <p className="survey-step-description">
        허용 가능한 연식과 주행거리를 지정하면 차량 상태 기준을 조정할 수 있습니다.
      </p>
      <div className="field-group">
        <label htmlFor="minYear">최소 연식</label>
        <select
          id="minYear"
          name="minYear"
          value={data.minYear}
          onChange={handleChange}
        >
          <option value="">선택해 주세요</option>
          {[2024, 2023, 2022, 2021, 2020, 2019].map((year) => (
            <option key={year} value={year}>{year}</option>
          ))}
        </select>
        {errors?.minYear && <span className="error-text">{errors.minYear}</span>}
      </div>

      <div className="field-group">
        <label htmlFor="maxMileage">최대 주행거리 (km)</label>
        <input
          id="maxMileage"
          name="maxMileage"
          type="number"
          min={0}
          value={data.maxMileage}
          onChange={handleChange}
          placeholder="예: 80000"
        />
        {errors?.maxMileage && <span className="error-text">{errors.maxMileage}</span>}
      </div>
    </div>
  );
}
