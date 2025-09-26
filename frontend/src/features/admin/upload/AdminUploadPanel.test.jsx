import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import AdminUploadPanel from './AdminUploadPanel.jsx';

vi.mock('./useAdminUpload.js', () => {
  const noop = vi.fn();
  return {
    useAdminUpload: () => ({
      selectedFile: null,
      status: 'idle',
      message: '',
      logs: [],
      error: null,
      jobId: null,
      isBusy: false,
      handleFileSelect: noop,
      handleUpload: noop,
      reset: noop
    })
  };
});

describe('AdminUploadPanel', () => {
  it('renders upload controls and helper text', () => {
    render(<AdminUploadPanel />);

    expect(screen.getByText('관리자 CSV 업로드')).toBeInTheDocument();
    expect(screen.getByLabelText('업로드 파일')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '업로드 시작' })).toBeDisabled();
    expect(
      screen.getByText('최신 CSV 카탈로그를 업로드하면 자동으로 유효성 검증이 시작됩니다.')
    ).toBeInTheDocument();
  });
});
