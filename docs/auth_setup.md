# Google OAuth2 연동 가이드

이 문서는 AI Car Sales 백엔드에서 Google OAuth2 로그인을 활성화하기 위한 설정 절차를 설명합니다.

## 1. Google Cloud Console 설정
1. [Google Cloud Console](https://console.cloud.google.com/)에 접속합니다.
2. 새 프로젝트를 생성하거나 기존 프로젝트를 선택합니다.
3. **API 및 서비스 → OAuth 동의 화면**에서 사용자 유형/범위를 설정 후 저장합니다.
4. **API 및 서비스 → 사용자 인증 정보**에서 `OAuth 2.0 클라이언트 ID`를 생성합니다.
   - 애플리케이션 유형: `웹 애플리케이션`
   - 승인된 리디렉션 URI: `http://localhost:8080/login/oauth2/code/google` (로컬 개발 기준)
5. 발급된 `클라이언트 ID`와 `클라이언트 보안 비밀번호`를 확인합니다.

## 2. 환경 변수 설정
프로젝트 루트에서 다음 환경 변수를 설정합니다.

```bash
export GOOGLE_CLIENT_ID="<발급받은 클라이언트 ID>"
export GOOGLE_CLIENT_SECRET="<발급받은 클라이언트 보안 비밀번호>"
```

Docker Compose 또는 배포 환경에서도 동일한 값을 환경 변수로 주입해야 합니다.

## 3. 애플리케이션 구성
`application.yml`에는 아래와 같이 OAuth2 클라이언트 등록 정보가 포함되어 있습니다.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
            scope:
              - openid
              - profile
              - email
```

환경 변수를 설정한 상태에서 애플리케이션을 기동하면 `/oauth2/authorization/google` 경로로 리디렉트하여 Google 로그인 플로우를 사용할 수 있습니다.

## 4. 동작 확인
1. 애플리케이션을 실행합니다: `./scripts/run_backend.sh`
2. 브라우저에서 `http://localhost:8080/oauth2/authorization/google`에 접속합니다.
3. Google 로그인 화면에서 인증을 완료하면 애플리케이션으로 리디렉트됩니다.
4. 최초 로그인 시 사용자 정보가 `users` 테이블에 `auth_provider = GOOGLE` 으로 저장됩니다.

## 5. 주의사항
- production 환경에서는 HTTPS 리디렉션 URI를 사용해야 합니다.
- 기존 로컬 계정(email/password)과 동일한 이메일로 소셜 로그인 시, 동일 사용자로 병합됩니다.
- 구글 계정에서 이메일 제공을 거부하면 인증에 실패하므로, OAuth 동의 화면에서 이메일 범위가 반드시 포함되어야 합니다.

