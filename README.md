# trip-plan-app-backend

## 🇰🇷 프로젝트 정보

본 프로젝트는 2026년 마스외전의 여행 계획 AI 프로젝트입니다.

### 실행 방법

```
1. git clone
2. ./gradlew clean build
3. java -jar build/libs/tripplanappbackend-0.0.1-SNAPSHOT.jar
```

> **주의**: DB가 반드시 켜져 있어야 합니다. 켜져 있는데도 불구하고 에러가 난다면 DB ID/PW가 일치하는지 확인해 보세요.

### 권장 개발 환경

- Java 17
- Spring Boot 4.x
- Gradle
- MySql 8.0

### 프로젝트 구조

```
src/
  main/
     java/mars/tripplanappbackend/
	common/
		config/ 어플리케이션 환경 설정
		    auth/config/ Security 설정 및 JWT 관련 설정
		    filter/ 요청 로깅 등 로깅 필터 설정
		    swagger/ 스웨거 설정
		dto/ 공통 Response 객체
		entity/ 생성/수정 시간 자동 기록
		enums/ 공통 에러 코드
		exception/ 전역 예외 관리
```




### 추가 팁

- http://localhost:8080/swagger-ui.html 로 스웨거를 확인해 볼 수 있습니다.
- 새로운 에러 상황이 발생하면 common/enums/ErrorCode.java에 에러 코드를 먼저 등록한 뒤 사용하세요.
- 새로운 Entity를 만들 때 BaseEntity를 상속 받으세요.

### 자주 발생하는 문제

- Java 버전 충돌: `JAVA_HOME`이 올바른지 확인하고, `java -version`을 통해 17인지 체크하세요.
- Connection Refused 에러 발생 시 DB가 켜져 있는지 확인하세요.

