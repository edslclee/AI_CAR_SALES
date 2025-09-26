const BASE_PATH = '/api/v1/admin/cars';

async function parseError(response) {
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    try {
      const data = await response.json();
      if (typeof data === 'string') {
        return data;
      }
      if (data && typeof data.message === 'string') {
        return data.message;
      }
    } catch (err) {
      // fall through to text parsing
    }
  }

  try {
    const text = await response.text();
    if (text) {
      return text;
    }
  } catch (err) {
    // ignore and return default message
  }

  return '요청을 처리하는 중 오류가 발생했습니다.';
}

export async function uploadCsv(file) {
  if (!file) {
    throw new Error('업로드할 파일이 없습니다.');
  }

  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${BASE_PATH}/upload`, {
    method: 'POST',
    body: formData
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  const data = await response.json();
  if (!data || typeof data.jobId !== 'string') {
    throw new Error('업로드 응답이 올바르지 않습니다.');
  }

  return data;
}

export async function fetchJobStatus(jobId) {
  if (!jobId) {
    throw new Error('조회할 작업 ID가 없습니다.');
  }

  const response = await fetch(`${BASE_PATH}/upload/${jobId}`);

  if (response.status === 404) {
    throw new Error('업로드 작업을 찾을 수 없습니다.');
  }

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  const data = await response.json();
  if (!data || typeof data.status !== 'string') {
    throw new Error('업로드 상태 응답이 올바르지 않습니다.');
  }

  return {
    jobId: data.jobId ?? jobId,
    status: data.status,
    message: data.message ?? '',
    errorReport: data.errorReport ?? null,
    completedAt: data.completedAt ?? null
  };
}
