export default function BudgetStep({ data, errors, onChange }) {
  const handleChange = (evt) => {
    const { name, value } = evt.target;
    onChange({ [name]: value });
  };

  return (
    <div className="survey-form">
      <p className="survey-step-description">
        예상 예산 범위와 구매 방식을 입력하면 추천 대상의 가격대를 조정할 수 있습니다.
      </p>
      <div className="field-group">
        <label htmlFor="minBudget">최소 예산 (만원)</label>
        <input
          id="minBudget"
          name="minBudget"
          type="number"
          min={0}
          value={data.minBudget}
          onChange={handleChange}
          placeholder="예: 2500"
        />
        {errors?.minBudget && <span className="error-text">{errors.minBudget}</span>}
      </div>
      <div className="field-group">
        <label htmlFor="maxBudget">최대 예산 (만원)</label>
        <input
          id="maxBudget"
          name="maxBudget"
          type="number"
          min={0}
          value={data.maxBudget}
          onChange={handleChange}
          placeholder="예: 4000"
        />
        {errors?.maxBudget && <span className="error-text">{errors.maxBudget}</span>}
      </div>
      <div className="field-group">
        <label>구매 방식</label>
        <div className="checkbox-grid">
          {[
            { value: 'lease', label: '리스/렌트' },
            { value: 'finance', label: '할부/금융' },
            { value: 'cash', label: '일시불' }
          ].map((option) => {
            const active = data.purchaseType === option.value;
            return (
              <label
                key={option.value}
                className={`checkbox-tile ${active ? 'active' : ''}`}
              >
                <input
                  type="radio"
                  name="purchaseType"
                  value={option.value}
                  checked={active}
                  onChange={handleChange}
                />
                <span>{option.label}</span>
              </label>
            );
          })}
        </div>
        {errors?.purchaseType && <span className="error-text">{errors.purchaseType}</span>}
      </div>
    </div>
  );
}
