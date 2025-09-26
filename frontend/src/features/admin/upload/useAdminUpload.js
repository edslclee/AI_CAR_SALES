import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchJobStatus, uploadCsv } from './adminUploadApi.js';
import { interpretStatus, formatStatusLog } from './statusMapper.js';

const POLL_INTERVAL_MS = 2000;

export function useAdminUpload() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [status, setStatus] = useState('idle');
  const [message, setMessage] = useState('');
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState(null);
  const [jobId, setJobId] = useState(null);
  const pollTimerRef = useRef(null);

  const isBusy = status === 'uploading' || status === 'processing';

  const clearPollTimer = useCallback(() => {
    if (pollTimerRef.current) {
      clearTimeout(pollTimerRef.current);
      pollTimerRef.current = null;
    }
  }, []);

  const appendLog = useCallback((entry) => {
    if (!entry) {
      return;
    }
    setLogs((prev) => {
      if (prev.length > 0 && prev[prev.length - 1] === entry) {
        return prev;
      }
      return [...prev, entry];
    });
  }, []);

  const reset = useCallback(() => {
    clearPollTimer();
    setSelectedFile(null);
    setStatus('idle');
    setMessage('');
    setLogs([]);
    setError(null);
    setJobId(null);
  }, [clearPollTimer]);

  const handleFileSelect = useCallback((file) => {
    setSelectedFile(file || null);
    setError(null);
  }, []);

  const pollStatus = useCallback(async (nextJobId) => {
    try {
      const rawStatus = await fetchJobStatus(nextJobId);
      const interpreted = interpretStatus(rawStatus);

      setStatus(interpreted.status);
      setMessage(interpreted.message);
      setError(interpreted.error);
      appendLog(formatStatusLog(rawStatus));

      if (interpreted.status === 'processing') {
        pollTimerRef.current = setTimeout(() => {
          pollStatus(nextJobId);
        }, POLL_INTERVAL_MS);
      } else {
        clearPollTimer();
      }
    } catch (pollError) {
      clearPollTimer();
      setStatus('error');
      setError(pollError.message || '상태 조회 중 오류가 발생했습니다.');
      appendLog(`상태 조회 실패: ${pollError.message || '알 수 없는 오류'}`);
    }
  }, [appendLog, clearPollTimer]);

  const handleUpload = useCallback(async () => {
    if (!selectedFile) {
      setError('업로드할 CSV 파일을 선택하세요.');
      return;
    }

    try {
      setError(null);
      setLogs([]);
      setMessage('업로드 준비 중');
      setStatus('uploading');
      appendLog(`업로드 요청 전송 준비: ${selectedFile.name}`);

      const { jobId: newJobId } = await uploadCsv(selectedFile);
      setJobId(newJobId);
      appendLog(`업로드 작업이 대기열에 등록되었습니다. Job ID=${newJobId}`);
      setStatus('processing');
      setMessage('검증이 시작되었습니다. 잠시만 기다려 주세요.');
      pollStatus(newJobId);
    } catch (uploadError) {
      clearPollTimer();
      setStatus('error');
      setError(uploadError.message || '업로드 중 오류가 발생했습니다.');
      appendLog(`업로드 실패: ${uploadError.message || '알 수 없는 오류'}`);
    }
  }, [appendLog, clearPollTimer, pollStatus, selectedFile]);

  useEffect(() => () => clearPollTimer(), [clearPollTimer]);

  return {
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
  };
}
