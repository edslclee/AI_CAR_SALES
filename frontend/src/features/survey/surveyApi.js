export async function submitSurvey(payload) {
  // API 연동 스텁
  // 향후 실제 백엔드 `/api/v1/surveys` 호출로 교체 예정
  await new Promise((resolve) => setTimeout(resolve, 600));
  return {
    recommendationId: Math.floor(Math.random() * 100000).toString(),
    submittedAt: new Date().toISOString(),
    payload
  };
}
