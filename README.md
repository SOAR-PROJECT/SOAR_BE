# SOAR_BE – 네이버 쇼핑 순위 모니터링 & AI 개선 제안 서비스 (Backend)

## 프로젝트 개요
SOAR_BE는 네이버 스마트스토어 판매자를 위한 **순위 모니터링 및 AI 기반 상품명 개선 제안 서비스의 백엔드 서버**입니다.  
사용자의 스토어 정보, 상품 정보, 순위 히스토리, AI 분석 요청과 같은 모든 핵심 비즈니스 로직을 담당하며, 안정적인 데이터 저장과 API 제공을 목표로 합니다.

---

## 🔧 핵심 기능

### ✔ 인증
- JWT 기반 로그인 / 회원가입
- 회원 이메일 중복 검사
- 스토어 미등록 사용자에 대한 흐름 제어

### ✔ 스토어 관리
- 사용자 스토어 등록 및 조회
- 최초 1회 등록 후 자동 연결

### ✔ 상품 관리
- Excel 기반 초기 상품 대량 업로드
- 개별 상품 등록, 수정, 삭제
- 상품 검색 및 페이지네이션
- 상품 상세 정보 제공 (키워드, 관리번호, 순위정보 포함)

### ✔ 순위 관리
- 네이버 API 기반으로 수집한 최신 순위 저장
- 최근 30일 히스토리 조회
- 대시보드용 통계 API 제공
    - 총 상품 수
    - 상승/하락/보합 개수
    - 최근 등록 상품 목록

### ✔ AI 분석/제안
- LLM 기반 상품명 개선 제안 API
- 하락 상품 상태 판별 후 개선 가능 여부 반환
- 제안 결과 저장 및 재요청

---

## 🧩 기술 스택

### Backend
- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security (JWT)
- Spring Data JPA
- MySQL 8
- Hibernate Validator
- Lombok

### DevOps / Tools
- IntelliJ IDEA
- MySQL Workbench
- GitHub
- (추후) GitHub Actions, Docker, CI/CD

---

## 📁 프로젝트 구조

```angular2html
src/
├── main/java/com/soar_be/
│ ├── auth/ # 인증/인가
│ │ ├── controller/
│ │ ├── dto/
│ │ └── service/
│ ├── config/ # Security, CORS, Swagger 설정
│ ├── domain/ # Entity, Repository, Domain Service
│ ├── global/ # 글로벌 예외/응답/유틸
│ └── SoarBeApplication
└── main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
```



---

## 🔀 데이터 흐름

1. 회원가입 → JWT 발급
2. 최초 1회 스토어 등록
3. Excel 업로드로 초기 상품 등록
4. 프론트가 순위 수집 → 백엔드 저장
5. 대시보드에서 통계 조회
6. 상품 상세 → 30일 히스토리 제공
7. 하락 상품 → AI 제안 요청
8. 생성된 제안 저장 및 결과 반환

---

## ⚙ 실행 방법

### ⚡ 사전 요구사항
- JDK 21
- MySQL
- env 

### 📌 개발 서버 실행
`./gradlew bootRun`


### 📌 배포용 빌드
`./gradlew build`

---

## 🚀 향후 개선사항

### Phase 2
-[ ] 순위 급변 알림
-[ ] 경쟁 상품 분석 API
-[ ] 키워드 최적화 AI 추천
-[ ] 자동 스케줄러 기반 순위 수집

### Phase 3
-[ ] 멀티 스토어 관리
-[ ] 팀 협업 기능
-[ ] 고급 리포트 생성 API

---

## 👥 팀
백엔드 개발 / 기획 / PM : 다현 한
