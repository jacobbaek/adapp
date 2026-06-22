# Tetris (adapp)

Android용 테트리스 게임 앱입니다.

## 📲 스마트폰에 설치하기

### 방법 1: GitHub Releases에서 APK 다운로드 (권장)

1. [Releases 페이지](../../releases)에서 최신 버전의 APK 파일을 다운로드합니다.
2. 스마트폰에서 **출처를 알 수 없는 앱 설치**를 허용합니다.
   - Android 8.0+: 설정 → 앱 → 특별한 앱 액세스 → 출처를 알 수 없는 앱 설치
   - Android 7.x: 설정 → 보안 → 알 수 없는 소스
3. 다운로드한 APK 파일을 열어 설치합니다.

> **요구 사항**: Android 7.0 (API 24) 이상

### 방법 2: 직접 빌드

```bash
./gradlew assembleDebug
# APK 위치: app/build/outputs/apk/debug/app-debug.apk
```

## 릴리즈 배포하기

버전 태그를 푸시하면 GitHub Actions가 자동으로 APK를 빌드하고 릴리즈를 생성합니다.

```bash
git tag v1.0.0
git push origin v1.0.0
```

## 게임 조작법

| 버튼 | 동작 |
|------|------|
| ◀ / ▶ | 좌/우 이동 |
| ▼ | 소프트 드롭 |
| ↻ Rotate | 회전 |
| ⬇ Drop | 하드 드롭 |
| ⏸ Pause | 일시 정지 / 재개 |