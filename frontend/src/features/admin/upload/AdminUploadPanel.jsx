import { useMemo } from 'react';
import { useAdminUpload } from './useAdminUpload.js';
import './adminUpload.css';

const STATUS_LABEL = {
  idle: '대기 중',
  uploading: '업로드 중',
  processing: '검증 중',
  completed: '완료',
  failed: '실패',
  error: '오류'
};

export default function AdminUploadPanel() {
  const {
    selectedFile,
    status,
    message,
    logs,
    error,
    jobId,
    isBusy,
    handleFileSelect,
    handleUpload,
    reset
  } = useAdminUpload();

  const statusLabel = STATUS_LABEL[status] || '미확인';

  const helperText = useMemo(() => {
    switch (status) {
      case 'idle':
        return '최신 CSV 카탈로그를 업로드하면 자동으로 유효성 검증이 시작됩니다.';
      case 'uploading':
        return '파일을 업로드하는 중입니다. 잠시만 기다려 주세요.';
      case 'processing':
        return '검증이 진행되고 있습니다. 완료까지 수 초 정도 소요됩니다.';
      case 'completed':
        return '업로드가 완료되었습니다. 결과를 확인하고 필요 시 다시 업로드하세요.';
      case 'failed':
      case 'error':
        return '오류 내용을 확인한 뒤 CSV를 수정하거나 다시 시도해 주세요.';
      default:
        return '';
    }
  }, [status]);

  const changeHandler = (event) => {
    const file = event.target.files?.[0] ?? null;
    handleFileSelect(file);
  };

  return (
    <section className="card admin-upload">
      <header className="admin-upload__header">
        <h2>관리자 CSV 업로드</h2>
        <p className="hint">카탈로그 변경 사항을 반영하려면 최신 CSV 파일을 업로드하세요.</p>
      </header>

      <div className="admin-upload__content">
        <div className="admin-upload__field">
          <label htmlFor="csv-upload">업로드 파일</label>
          <input
            id="csv-upload"
            type="file"
            accept=".csv"
            onChange={changeHandler}
            disabled={isBusy}
          />
          {selectedFile ? (
            <p className="hint">
              선택된 파일: <strong>{selectedFile.name}</strong>
            </p>
          ) : (
            <p className="hint">.csv 확장자의 UTF-8 인코딩 파일만 지원합니다.</p>
          )}
        </div>

        <div className="admin-upload__actions">
          <button
            type="button"
            className="button primary"
            onClick={handleUpload}
            disabled={!selectedFile || isBusy}
          >
            업로드 시작
          </button>
          <button type="button" className="button" onClick={reset} disabled={isBusy && status !== 'error'}>
            초기화
          </button>
        </div>

        <div className={`status-pill status-pill--${status}`}>
          <span className="status-label">상태</span>
          <strong>{statusLabel}</strong>
          {jobId ? <span className="status-job">Job ID: {jobId}</span> : null}
        </div>
        <p className="admin-upload__message">{message || helperText}</p>

        {error ? <div className="error-box">{error}</div> : null}

        <section className="log-viewer">
          <header className="log-viewer__header">
            <h3>처리 로그</h3>
            <span className="log-count">{logs.length}건</span>
          </header>
          {logs.length === 0 ? (
            <p className="hint">아직 표시할 로그가 없습니다. 업로드를 시작하면 실시간으로 표시됩니다.</p>
          ) : (
            <ol className="log-viewer__list">
              {logs.map((entry, index) => (
                <li key={`${index}-${entry}`}>{entry}</li>
              ))}
            </ol>
          )}
        </section>
      </div>
    </section>
  );
}
