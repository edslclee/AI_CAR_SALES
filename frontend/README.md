# AI Car Sales Frontend

React + Vite 기반의 PWA 스캐폴드입니다. 온보딩 설문, 추천 결과, 즐겨찾기 화면을 위한 라우팅과 기본 스타일이 준비되어 있습니다.

## Prerequisites
- Node.js 18+ (20.x 권장)
- npm 9+

## Install dependencies
```bash
cd frontend
npm install
```
> 현재 네트워크 제한으로 설치가 실패할 경우, 연결 가능한 환경에서 다시 실행해 주세요. 최신 lockfile은 `react-router-dom@^6.25.1`을 포함해야 합니다.

## Available scripts
- `npm run dev` — Vite 개발 서버 실행 (기본 포트 5173). 다른 포트를 쓰려면 `VITE_PORT=5183 npm run dev -- --host`.
- `npm run build` — 프로덕션 번들 빌드 (설치 완료 후 실행).
- `npm run preview` — 빌드 결과를 로컬 미리보기.

## PWA 구성
- `public/manifest.webmanifest`, `public/sw.js` 및 `src/registerSW.js` 에서 기본 PWA 설정을 제공합니다.
- 프로덕션 빌드에서만 서비스 워커가 등록되며, 필요 시 아이콘을 교체하거나 Workbox 기반으로 확장할 수 있습니다.

## 디렉터리 구조
```
frontend/
├── public/              # 정적 에셋, manifest, 서비스 워커
├── src/
│   ├── routes/          # 라우트별 placeholder 컴포넌트
│   ├── App.jsx          # 라우팅/레이아웃
│   └── registerSW.js    # 서비스 워커 등록
└── vite.config.js       # 개발 서버 및 빌드 설정
```
