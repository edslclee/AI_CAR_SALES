import { useMemo, useState } from 'react';

const initialState = {
  budget: {
    minBudget: '',
    maxBudget: '',
    purchaseType: 'lease'
  },
  usage: {
    purpose: '',
    passengers: ''
  },
  preferences: {
    bodyTypes: [],
    brands: []
  },
  condition: {
    minYear: '',
    maxMileage: ''
  },
  options: {
    mustHaves: ''
  }
};

export function useSurveyForm() {
  const [formData, setFormData] = useState(initialState);
  const [currentStep, setCurrentStep] = useState(0);
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);

  const updateSection = (section, values) => {
    setFormData((prev) => ({
      ...prev,
      [section]: {
        ...prev[section],
        ...values
      }
    }));
  };

  const togglePreference = (section, value) => {
    setFormData((prev) => {
      const current = prev.preferences[section];
      const exists = current.includes(value);
      return {
        ...prev,
        preferences: {
          ...prev.preferences,
          [section]: exists
            ? current.filter((item) => item !== value)
            : [...current, value]
        }
      };
    });
  };

  const reset = () => {
    setFormData(initialState);
    setCurrentStep(0);
    setStatus('idle');
    setError(null);
    setResult(null);
  };

  return useMemo(
    () => ({
      formData,
      updateSection,
      togglePreference,
      currentStep,
      setCurrentStep,
      status,
      setStatus,
      error,
      setError,
      result,
      setResult,
      reset
    }),
    [formData, currentStep, status, error, result]
  );
}

export function validateStep(stepId, data) {
  const errors = {};
  switch (stepId) {
    case 'budget': {
      const { minBudget, maxBudget, purchaseType } = data.budget;
      if (!minBudget || Number(minBudget) <= 0) {
        errors.minBudget = '최소 예산을 입력해 주세요.';
      }
      if (!maxBudget || Number(maxBudget) <= 0) {
        errors.maxBudget = '최대 예산을 입력해 주세요.';
      }
      if (minBudget && maxBudget && Number(minBudget) > Number(maxBudget)) {
        errors.maxBudget = '최대 예산은 최소 예산보다 커야 합니다.';
      }
      if (!purchaseType) {
        errors.purchaseType = '구매 방식을 선택해 주세요.';
      }
      break;
    }
    case 'usage': {
      const { purpose, passengers } = data.usage;
      if (!purpose) {
        errors.purpose = '사용 목적을 선택해 주세요.';
      }
      if (!passengers) {
        errors.passengers = '탑승 인원을 선택해 주세요.';
      }
      break;
    }
    case 'preferences': {
      const { bodyTypes } = data.preferences;
      if (bodyTypes.length === 0) {
        errors.bodyTypes = '선호 차종을 하나 이상 선택해 주세요.';
      }
      break;
    }
    case 'condition': {
      const { minYear, maxMileage } = data.condition;
      if (!minYear) {
        errors.minYear = '허용 가능한 최소 연식을 선택해 주세요.';
      }
      if (!maxMileage) {
        errors.maxMileage = '허용 가능한 최대 주행거리를 입력해 주세요.';
      }
      break;
    }
    default:
      break;
  }
  return { valid: Object.keys(errors).length === 0, errors };
}
