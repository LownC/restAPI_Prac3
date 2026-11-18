# restAPI_Prac3

Spring Boot 로 만든 **글(Post) · 댓글(PostComment) REST API** 연습 프로젝트입니다.
모든 엔드포인트는 **TDD(테스트 먼저 작성 → 실패 확인 → 구현 → 통과)** 순서로 만들었습니다.

## 기술 스택

- Java 25, Spring Boot 4.1.2, Gradle 9.9.1 (Kotlin DSL)
- Spring Web MVC, Spring Data JPA, Bean Validation
- H2 (개발: 파일 DB `./db_dev`, 테스트: 메모리 DB), Lombok
- 테스트: JUnit 5, MockMvc, AssertJ

## 실행

```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 전체 테스트 실행 (27개)
```

H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:./db_dev;MODE=MySQL`, 사용자 `sa`)

## 샘플 데이터

앱 시작 시 글이 하나도 없으면 아래 데이터를 자동으로 만듭니다. (`BaseInitData`)

| 글 | 댓글 |
|---|---|
| 1. 제목 1 / 내용 1 | 댓글 1-1, 댓글 1-2, 댓글 1-3 |
| 2. 제목 2 / 내용 2 | 댓글 2-1, 댓글 2-2 |
| 3. 제목 3 / 내용 3 | (없음) |

## 공통 응답 형식 (`RsData`)

```json
{
  "resultCode": "200-1",
  "msg": "1번 글을 조회하였습니다.",
  "data": { }
}
```

`resultCode` 앞자리가 HTTP 상태코드입니다. (`200`, `201`, `400`, `404`)

| 상황 | resultCode | msg |
|---|---|---|
| 데이터 없음 | `404-1` | 해당 데이터가 존재하지 않습니다. |
| 검증 실패 | `400-1` | `필드-검증코드-메시지` (예: `title-NotBlank-...`) |
| JSON 형식 오류 | `400-1` | 요청 본문이 올바르지 않습니다. |

## API

### 글

| 메서드 | URL | 설명 | 성공 코드 |
|---|---|---|---|
| GET | `/api/v1/posts` | 다건조회 (최신순) | 200-1 |
| GET | `/api/v1/posts/{id}` | 단건조회 | 200-1 |
| POST | `/api/v1/posts` | 작성 `{ "title", "content" }` | 201-1 |
| PUT | `/api/v1/posts/{id}` | 수정 `{ "title", "content" }` | 200-1 |
| DELETE | `/api/v1/posts/{id}` | 삭제 (댓글도 함께 삭제) | 200-1 |

- `title` 2~100자, `content` 2~5000자, 둘 다 필수

### 댓글

| 메서드 | URL | 설명 | 성공 코드 |
|---|---|---|---|
| GET | `/api/v1/posts/{postId}/comments` | 다건조회 (최신순) | 200-1 |
| GET | `/api/v1/posts/{postId}/comments/{id}` | 단건조회 | 200-1 |
| POST | `/api/v1/posts/{postId}/comments` | 작성 `{ "content" }` | 201-1 |
| PUT | `/api/v1/posts/{postId}/comments/{id}` | 수정 `{ "content" }` | 200-1 |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 삭제 | 200-1 |

- `content` 2~1000자, 필수
- 다른 글에 달린 댓글 id 로 요청하면 404

## 커밋 단계 (태그)

각 커밋에는 `001` ~ `016` 태그가 붙어 있습니다. `git checkout 006` 처럼 단계별로 코드를 볼 수 있습니다.

| 태그 | 내용 |
|---|---|
| 001 | Spring Boot 프로젝트 생성 |
| 002 | DB 프로파일(dev/test) + JPA Auditing(BaseEntity) |
| 003 | Post / PostComment 엔티티, PostRepository |
| 004 | 샘플 데이터(글 3, 댓글 5) by TDD |
| 005 | 공통 응답 RsData + 전역 예외 처리 |
| 006 | 글 다건조회 |
| 007 | 글 단건조회 |
| 008 | 글 작성 |
| 009 | 글 수정 |
| 010 | 글 삭제 |
| 011 | 댓글 다건조회 |
| 012 | 댓글 단건조회 |
| 013 | 댓글 작성 |
| 014 | 댓글 수정 |
| 015 | 댓글 삭제 |
| 016 | README |

## 패키지 구조

```
com.back
├── domain.post
│   ├── post         (controller / dto / entity / repository / service)
│   └── postComment  (controller / dto / entity)
└── global
    ├── globalExceptionHandler
    ├── initData
    ├── jpa.entity   (BaseEntity)
    └── rsData
```
