const BODY_TYPE_OPTIONS = ['SUV', 'Sedan', 'Hatchback', 'Wagon', 'Crossover'];
const BRAND_OPTIONS = ['Hyundai', 'Kia', 'Genesis', 'Tesla', 'BMW', 'Audi', 'Volvo'];

export default function PreferenceStep({ data, errors, onToggle, onChange }) {
  const handleBodyType = (value) => () => onToggle('bodyTypes', value);
  const handleBrand = (evt) => {
    const { value } = evt.target;
    onToggle('brands', value);
  };

  const handleFreeform = (evt) => {
    onChange({ brands: evt.target.value.split(',').map((item) => item.trim()).filter(Boolean) });
  };

  return (
    <div className="survey-form">
      <p className="survey-step-description">
        선호 차종과 브랜드를 알려주시면 추천 우선순위에 반영합니다.
      </p>
      <fieldset className="field-group">
        <legend>선호 차종 (복수 선택 가능)</legend>
        <div className="checkbox-grid" role="group" aria-label="선호 차종">
          {BODY_TYPE_OPTIONS.map((type) => {
            const active = data.bodyTypes.includes(type);
            return (
              <label key={type} className={`checkbox-tile ${active ? 'active' : ''}`}>
                <input
                  type="checkbox"
                  checked={active}
                  onChange={handleBodyType(type)}
                />
                <span>{type}</span>
              </label>
            );
          })}
        </div>
        {errors?.bodyTypes && <span className="error-text">{errors.bodyTypes}</span>}
      </fieldset>

      <fieldset className="field-group">
        <legend>선호 브랜드 (복수 선택 가능)</legend>
        <div className="checkbox-grid" role="group" aria-label="선호 브랜드">
          {BRAND_OPTIONS.map((brand) => {
            const active = data.brands.includes(brand);
            return (
              <label key={brand} className={`checkbox-tile ${active ? 'active' : ''}`}>
                <input
                  type="checkbox"
                  value={brand}
                  checked={active}
                  onChange={handleBrand}
                />
                <span>{brand}</span>
              </label>
            );
          })}
        </div>
        <small className="hint">
          쉼표로 구분된 브랜드를 직접 입력하려면 아래 입력창을 사용하세요.
        </small>
        <label htmlFor="brandFreeform" className="field-label">
          선호 브랜드 직접 입력
        </label>
        <textarea
          id="brandFreeform"
          rows={2}
          placeholder="예: Hyundai, Kia, Genesis"
          defaultValue={data.brands.join(', ')}
          onBlur={handleFreeform}
        />
      </fieldset>
    </div>
  );
}
