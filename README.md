# 브루마블 게임 프로젝트 v1

이 프로젝트는 브루마블 보드 게임을 웹소켓 기반으로 구현한 프로젝트입니다. 
v1은 바닐라 자바스크립트(ES6)를 이용한 버전이며, v2는 Vue.js를 이용하여 프론트엔드를 리팩토링할 예정입니다.

## 프로젝트 개요

- **v1**: 바닐라 JavaScript를 이용하여 클라이언트 측 게임 로직을 구현하였으며, Spring WebSocket을 사용해 서버와의 실시간 통신을 처리하였습니다. 서버 측 테스트는 JUnit을 통해 작성되었습니다.
- **v2**: Vue.js를 이용해 프론트엔드 UI와 로직을 개선할 예정입니다.

## 기술 스택

### v1 (완료)
- **프론트엔드**: 바닐라 JavaScript(ES6)
- **백엔드**: Spring WebSocket
- **테스트**: JUnit

### v2 (개발 예정)
- **프론트엔드**: Vue.js
- **백엔드**: Spring WebSocket (기존 유지)
- **테스트**: JUnit (기존 유지)

## 기능

### v1 주요 기능
- 실시간 플레이어 간 통신 (웹소켓을 통해 게임 상태 동기화)
- 턴제 진행
- 주사위 굴리기, 말 이동, 재산 구매 등의 기본 게임 기능

### v2 목표
- Vue.js 기반의 사용자 친화적인 UI 제공
- 컴포넌트 기반의 모듈화된 게임 로직
- 더 나은 상태 관리 및 UX 개선

## 설정 및 실행 방법

### v1
1. **서버 실행**:
    - Spring WebSocket 서버를 실행합니다.

2. **클라이언트 실행**:
    - `game.html` 파일을 브라우저에서 열어 게임을 시작합니다.

3. **테스트**:
    - JUnit 테스트는 `src/test/java` 경로에서 실행할 수 있습니다.

### v2
- 개발 중

## 향후 계획
- 프론트엔드를 Vue.js로 마이그레이션하여 더 나은 게임 경험을 제공할 계획입니다.
- Vuex를 사용한 상태 관리와 WebSocket 통신의 효율적인 처리를 목표로 합니다.
- 추가적인 게임 기능 확장 및 UI 개선.