# Android 프로젝트 개발 가이드라인

## 브랜치 흐름

| Branch | Description |
| --- | --- |
| main | 최종본 |
| develop | 기능 통합용 |

### 브랜치 작업 프로세스

1. 초기 프로젝트 설정 시 main → develop 브랜치 생성
2. 각 기능마다 새로운 브랜치 생성
   - 브랜치 이름: `feature_기능명`
3. 기능 구현 후 develop으로 PR 생성
4. 리뷰 및 테스트 후 main으로 병합
5. main으로 병합 후 해당 기능 브랜치 삭제

---

## 네이밍 규칙

### Class / Interface
- PascalCase 사용
- 예시: `CommunityClass`, `AIChat`, `QuestionForToday`

### Method
- camelCase 사용
- 예시: `getPetId()`, `getUserId()`

### Variable
- camelCase 사용
- 예시: `userName`, `petName`, `petList`

### XML ID
- snake_case 사용
- 접두사 규칙 준수 (tv_, iv_, btn_ 등)
- 예시: `tv_today_answer`, `iv_login_logo`

---

## 코드 스타일

- **연산자 및 콤마 뒤 공백**: 가독성을 위해 공백 추가
- **주석**:
  - 메소드 상단에 간결하게 기능 작성
  - 복잡한 로직에 간결하게 작성
- **들여쓰기**: 4 spaces

---

## 이슈 & PR 규칙

### 이슈 규칙

**제목 형식**: `[커밋 메시지] : where / what`
- 예시: `[Feat] 로그인 페이지 / 로그인 기능 구현`

**내용**:
- 기능 설명, 작업 내용 정리
- 개발 중 이슈, 이슈 해결
- 팀원 리뷰 요청

### PR 규칙

**제목 형식**: `커밋 메시지: PR 설명 (#이슈번호)`
- 예시: `Feat: 로그인 기능 구현 (#10)`

**내용**:
- 이슈 번호
- 작업 내용 정리
- 개발 중 이슈, 개발 화면 사진, 이슈 해결
- 팀원 리뷰 요청

### 커밋 메시지 규칙

| 타입 | 설명 |
| --- | --- |
| Feature | 새로운 기능 구현 |
| Delete | 쓸모없는 코드나 파일 삭제 |
| Chore | 버전 코드 수정, 타입 및 변수명 변경 등의 작은 작업 |
| UI | UI 작업 |
| Fix | 버그 및 오류 해결 |
| Hotfix | issue나 QA에서 문의된 급한 버그 및 오류 해결 |
| Move | 프로젝트 내 파일이나 코드의 이동 |
| Rename | 파일 이름 변경 |
| Refactor | 전면 수정 |
| Docs | README 등의 문서 개정 |

---

## 안드로이드 스튜디오 기본 설정

### 버전
- Android Studio: 현재 최신 버전으로 통일

### SDK 버전
- minSDK: 24
- targetSDK: 36

### 기능 테스트
- 실제 기기 또는 Emulator 사용

---

## Git 작업 방식

### 1. feature 브랜치 생성

```bash
git checkout develop
git pull origin develop  # 최신 상태 받아오기
git checkout -b feature/login  # 예: 로그인 기능
```

### 2. 각자 작업 후 커밋

```bash
git add .
git commit -m "feat: 로그인 화면 UI 구현"
git push origin feature/login
```

### 3. 이슈 & PR 생성
- feature/login → develop으로 PR 생성
- 팀원들 코드 리뷰
- 승인 후 merge

### 4. develop 최신화

```bash
git checkout develop
git pull origin develop  # 타 팀원들의 작업 반영
```

### 5. 새 기능 작업 시 1번부터 반복

---

## 주의사항

> 💡 **작업 시작 전 항상 `git pull origin develop`** → 최신 코드 받기

> 💡 **충돌 발생 시**: feature 브랜치에서 develop을 merge해서 로컬에서 해결 후 push

---
