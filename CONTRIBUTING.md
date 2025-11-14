# 🧩 SOAR_BE Contribution Guide

## 1. Branch Strategy
SOAR_BE는 Git Flow 기반 간단한 브랜치 전략을 사용합니다.

### 기본 브랜치
- `main` : 배포 가능한 안정 버전
- `develop` : 개발 브랜치

### 기능 개발 브랜치 (feature)
기능별로 분리하여 개발합니다.
```
feature/{issue-number}-{short-description}
예) feature/12-auth-jwt
예) feature/21-excel-product-upload
```


### 기타 브랜치
- `hotfix/*` : 운영 환경 긴급 수정
- `refactor/*` : 리팩토링
- `chore/*` : 설정, 문서, 환경 변경

---

## 2. Commit Message Convention
AngularJS Commit Message 규칙을 기반으로 합니다.

### 기본 형식
`<type>(scope): message`


### Type 예시
- `feat` : 새로운 기능
- `fix` : 버그 수정
- `refactor` : 코드 리팩토링
- `docs` : 문서 수정
- `test` : 테스트 코드 추가/수정
- `chore` : 설정 변경, 환경 구성 등
- `style` : 포맷팅, 세미콜론 등 코드 변경 없음

### 예시
```
feat(auth): implement jwt login
fix(product): excel parsing error
refactor(api): simplify dto conversion
chore(config): update swagger settings
```


---

## 3. Pull Request 규칙

### PR 제목 규칙
```
[#이슈번호] 작업 내용 요약
예) [#12] JWT 로그인/회원가입 기능 구현
```


### PR 생성 시 체크리스트
- [ ] 관련 이슈 연결 (`Resolves #12`)
- [ ] API 동작 테스트 완료
- [ ] 주요 변경 사항 PR 본문에 설명
- [ ] 리뷰 가능한 단위로 PR을 분리

---

## 4. Issue 작성 규칙
이슈는 한 가지 목적만 포함하도록 작성합니다.

카테고리 예시:
- `feature` : 기능 개발
- `bug` : 오류 제보
- `refactor` : 코드 개선
- `docs` : 문서 작업

이슈 템플릿은 `.github/ISSUE_TEMPLATE` 참고
---

