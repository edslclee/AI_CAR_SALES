const STATUS_MAP = {
  PENDING: 'processing',
  PROCESSING: 'processing',
  SUCCEEDED: 'completed',
  FAILED: 'failed'
};

const DEFAULT_MESSAGES = {
  processing: '검증이 진행되고 있습니다. 잠시만 기다려 주세요.',
  completed: '업로드가 완료되었습니다.',
  failed: '업로드 처리 중 오류가 발생했습니다.'
};

export function interpretStatus(payload) {
  if (!payload || typeof payload.status !== 'string') {
    return {
      status: 'error',
      message: '상태 정보를 불러오지 못했습니다.',
      error: '상태 응답 형식이 올바르지 않습니다.'
    };
  }

  const normalized = STATUS_MAP[payload.status] || 'processing';
  const baseMessage = (payload.message && payload.message.trim()) || DEFAULT_MESSAGES[normalized] || '';
  const error = normalized === 'failed'
    ? payload.errorReport || payload.message || DEFAULT_MESSAGES.failed
    : null;

  return {
    status: normalized,
    message: normalized === 'completed'
      ? `업로드 완료 - ${baseMessage}`
      : normalized === 'failed'
        ? `업로드 실패 - ${baseMessage}`
        : baseMessage,
    error
  };
}

export function formatStatusLog(payload) {
  if (!payload || typeof payload.status !== 'string') {
    return '';
  }

  const segments = [`상태=${payload.status}`];

  if (payload.message) {
    segments.push(`message=${payload.message}`);
  }

  if (payload.errorReport) {
    segments.push(`error=${payload.errorReport}`);
  }

  if (payload.completedAt) {
    segments.push(`completedAt=${payload.completedAt}`);
  }

  return segments.join(' | ');
}
