import { useMemo, useState } from 'react';
import BudgetStep from './steps/BudgetStep.jsx';
import UsageStep from './steps/UsageStep.jsx';
import PreferenceStep from './steps/PreferenceStep.jsx';
import ConditionStep from './steps/ConditionStep.jsx';
import OptionsStep from './steps/OptionsStep.jsx';
import SummaryStep from './steps/SummaryStep.jsx';
import { submitSurvey } from './surveyApi.js';
import { useSurveyForm, validateStep } from './useSurveyForm.js';
import './survey.css';

const steps = [
  { id: 'budget', title: '예산 설정', component: BudgetStep },
  { id: 'usage', title: '사용 용도', component: UsageStep },
  { id: 'preferences', title: '선호도', component: PreferenceStep },
  { id: 'condition', title: '차량 조건', component: ConditionStep },
  { id: 'options', title: '추가 옵션', component: OptionsStep },
  { id: 'summary', title: '확인 및 제출', component: SummaryStep }
];

export default function SurveyWizard() {
  const form = useSurveyForm();
  const [stepErrors, setStepErrors] = useState({});

  const progress = useMemo(() => ((form.currentStep + 1) / steps.length) * 100, [form.currentStep]);
  const StepComponent = steps[form.currentStep].component;

  const handleNext = () => {
    const stepId = steps[form.currentStep].id;
    if (stepId !== 'summary') {
      const { valid, errors } = validateStep(stepId, form.formData);
      if (!valid) {
        setStepErrors(errors);
        return;
      }
    }
    setStepErrors({});
    form.setCurrentStep(Math.min(form.currentStep + 1, steps.length - 1));
  };

  const handlePrev = () => {
    setStepErrors({});
    form.setCurrentStep(Math.max(form.currentStep - 1, 0));
  };

  const handleSubmit = async () => {
    form.setStatus('submitting');
    form.setError(null);
    try {
      const response = await submitSurvey(form.formData);
      form.setResult(response);
      form.setStatus('success');
    } catch (err) {
      form.setError('설문 제출 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
      form.setStatus('error');
    }
  };

  const isSummary = steps[form.currentStep].id === 'summary';

  return (
    <div className="survey-container">
      <div className="survey-progress">
        <div className="survey-progress__bar">
          <div className="survey-progress__fill" style={{ width: `${progress}%` }} />
        </div>
        <span>{form.currentStep + 1} / {steps.length}</span>
      </div>

      <div>
        <h2 className="survey-step-title">{steps[form.currentStep].title}</h2>
        <StepComponent
          data={mapStepData(steps[form.currentStep].id, form.formData)}
          errors={stepErrors}
          onChange={(updates) => handleSectionChange(steps[form.currentStep].id, updates, form)}
          onToggle={(key, value) => form.togglePreference(key, value)}
        />
      </div>

      {form.status === 'success' && form.result && (
        <div className="success-banner">
          추천이 생성되었습니다! 추천 ID: {form.result.recommendationId}
        </div>
      )}
      {form.error && <div className="error-text">{form.error}</div>}

      <div className="survey-actions">
        <button
          type="button"
          className="secondary"
          onClick={handlePrev}
          disabled={form.currentStep === 0}
        >
          이전 단계
        </button>

        {!isSummary && (
          <button type="button" className="primary" onClick={handleNext}>
            다음 단계
          </button>
        )}

        {isSummary && (
          <button
            type="button"
            className="primary"
            onClick={handleSubmit}
            disabled={form.status === 'submitting'}
          >
            {form.status === 'submitting' ? '제출 중...' : '추천 요청하기'}
          </button>
        )}
      </div>
    </div>
  );
}

function mapStepData(stepId, formData) {
  switch (stepId) {
    case 'budget':
      return formData.budget;
    case 'usage':
      return formData.usage;
    case 'preferences':
      return formData.preferences;
    case 'condition':
      return formData.condition;
    case 'options':
      return formData.options;
    default:
      return formData;
  }
}

function handleSectionChange(stepId, updates, form) {
  switch (stepId) {
    case 'budget':
      form.updateSection('budget', updates);
      break;
    case 'usage':
      form.updateSection('usage', updates);
      break;
    case 'preferences':
      if (updates.brands) {
        form.updateSection('preferences', { brands: updates.brands });
      }
      break;
    case 'condition':
      form.updateSection('condition', updates);
      break;
    case 'options':
      form.updateSection('options', updates);
      break;
    default:
      break;
  }
}
