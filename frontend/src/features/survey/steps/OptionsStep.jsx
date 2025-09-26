export default function OptionsStep({ data, onChange }) {
  const handleChange = (evt) => {
    const { value } = evt.target;
    onChange({ mustHaves: value });
  };

  return (
    <div className="survey-form">
      <p className="survey-step-description">
        꼭 필요한 옵션이나 라이프스타일 정보를 자유롭게 적어 주세요. (예: 통풍시트, 자율주행, 대형 트렁크 등)
      </p>
      <div className="field-group">
        <label htmlFor="mustHaves">필수 옵션 및 요청사항</label>
        <textarea
          id="mustHaves"
          rows={4}
          value={data.mustHaves}
          onChange={handleChange}
          placeholder="예: 캠핑 장비를 싣기 위한 넓은 트렁크, 1회 충전 주행거리 400km 이상"
        />
      </div>
    </div>
  );
}
