import { describe, expect, it } from 'vitest';
import { interpretStatus, formatStatusLog } from './statusMapper.js';

describe('interpretStatus', () => {
  it('maps SUCCEEDED to completed with message', () => {
    const result = interpretStatus({ status: 'SUCCEEDED', message: 'created=2, updated=1' });
    expect(result.status).toBe('completed');
    expect(result.message).toBe('업로드 완료 - created=2, updated=1');
    expect(result.error).toBeNull();
  });

  it('maps FAILED to failed and surfaces error report', () => {
    const result = interpretStatus({ status: 'FAILED', message: '검증 오류', errorReport: 'price 범위 위반' });
    expect(result.status).toBe('failed');
    expect(result.message).toContain('업로드 실패');
    expect(result.error).toBe('price 범위 위반');
  });

  it('falls back when payload is invalid', () => {
    const result = interpretStatus(null);
    expect(result.status).toBe('error');
    expect(result.error).toBe('상태 응답 형식이 올바르지 않습니다.');
  });
});

describe('formatStatusLog', () => {
  it('serializes status payload into a readable string', () => {
    const log = formatStatusLog({ status: 'SUCCEEDED', message: '완료', completedAt: '2025-09-26T02:11:00Z' });
    expect(log).toContain('상태=SUCCEEDED');
    expect(log).toContain('message=완료');
    expect(log).toContain('completedAt=2025-09-26T02:11:00Z');
  });

  it('returns empty string for invalid payload', () => {
    expect(formatStatusLog()).toBe('');
  });
});
