## Trip-Plan Coding Convention



### 1. 공통 원칙

- **가독성 우선**: 성능 최적화보다 코드의 이해 가능성과 유지보수성을 우선합니다.
- **일관성 유지**: 기존 코드 스타일과 다를 경우, 기존 스타일을 우선합니다.
- **단일 책임 원칙(SRP)**: 클래스·메서드는 하나의 책임에 집중하도록 설계합니다.

---

### 2. 프로젝트 구조

**한 도메인은 하나의 책임을 갖도록 구성합니다.**

#### (1) 전체 디렉터리 구조

```text
src/main/java/{base-package}/
├── global/
│   ├── config/
│   ├── exception/
│   ├── security/
│   ├── common/
│   └── ...
│
├── user/
├── auth/
├── post/
└── {domain}/                  # 예: party, order 등
    ├── controller/
    ├── service/
    ├── repository/
    ├── domain/
    ├── dto/
    │   ├── request/
    │   └── response/
    └── enums/
```

#### (2) 도메인별 폴더 역할

| 폴더 | 설명 |
|------|------|
| `controller` | HTTP 요청/응답, DTO 변환, 인증/인가 위임 |
| `service` | 비즈니스 로직, 트랜잭션 경계 |
| `repository` | JPA 등 영속성 처리 |
| `domain` | 엔티티 및 도메인 모델 |
| `dto` | 요청/응답 DTO, 요청/응답/호출자별 패키지 분리 |
| `enums` | 도메인 관련 Enum |

#### (3) 패키지명 규칙

- 전부 소문자, 언더스코어 금지.
- 예: `springboot.boilerplate.user.controller`, `springboot.boilerplate.global.config`.

---

### 3. Class Method

#### (1) Method Name

| 종류 | 설명 | 이름 규칙 |
|------|------|-----------|
| 생성(Create) | 데이터 생성 | `save` |
| 수정(Update) | 데이터 수정 | `update` / `update + Column` (예: `updatePassword`) |
| 삭제(Delete) | 데이터 삭제 | `delete` (하드), `softDelete` (소프트) |
| 단일 조회 | 단건 조회 | `findOne` / 조건 포함 시 `findOneByEmail` 등 |
| 리스트 조회 | 다건 조회 | `findAll` / 호출자 구분 시 `findAllByAdmin` |


#### (2) Method 구조

- **접근 지시자**, **매개변수**, **반환 타입**, **주석**을 명시합니다.
- `public` 메서드를 위에, `private` 메서드를 아래에 배치합니다.
- 비슷한 기능의 메서드는 가까운 위치에 모읍니다.
- **Controller / Service의 public 메서드는 DTO를 인자/반환 타입으로 사용**하고, Entity를 직접 노출하지 않습니다.

```java
/**
 * 유저를 저장합니다.
 */
public UserResponseDto save(UserSaveRequestDto dto) {
    // ...
}

/**
 * 유저를 ID 기반으로 단일 조회합니다.
 */
public UserResponseDto findOne(Long userId) {
    // ...
}

/**
 * 유저 리스트를 조회합니다.
 */
public List<UserResponseDto> findAll(UserSearchRequestDto dto) {
    // ...
}

/**
 * 유저의 저장 가능 여부를 검사합니다.
 */
private void validateUserExistence(String email) {
    // ...
}
```

---

### 4. Controller

#### (1) Controller 선언 규칙

- 이름은 **Domain + Controller** 형태로 작성합니다.
  - 예: `UserController`, `AdminUserController`, `UserV2Controller`.

#### (2) Controller 구조 규칙

- 사용 주체(일반 유저 / 관리자 등)에 따라 Controller를 분리합니다.
- Spring Security 설정을 Controller 단위로 공통 처리하여 중복 애노테이션을 줄입니다.

```java
// User Controller
@Api(tags = "User")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
}

// Admin User Controller
@Api(tags = "Admin User")
@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
}
```

---

### 5. API 선언 규칙

#### (1) HTTP Method 사용

| Method | Description | Body 여부 |
|--------|-------------|-----------|
| GET | 조회 | X |
| POST | 생성 / 이벤트 트리거 | O |
| DELETE | 삭제 | X |
| PUT | 전체 수정 | O |
| PATCH | 부분 수정 | O |

#### (2) API Payload 구성

API Payload는 **Header 영역**(URL, Params, Query)과 **Body 영역**으로 구분합니다.

##### Header

- **URL(Path)**: RESTful하게 **명사** 사용  
  - X: `POST /api/v1/food/save`  
  - O: `POST /api/v1/food`

- **URL Params(PathVariable)**  
  - 예: `GET /api/v1/food/{foodId}`, `DELETE /api/v1/food/{foodId}`  
  - `id` 대신 의미 있는 이름 사용 (`foodId`, `userId` 등).

```java
@GetMapping("/{foodId}")
public ResponseEntity<FoodResponseDto> findOne(@PathVariable("foodId") Long foodId) {
    // ...
}
```

- **URL Query Params**
  - 단순 값: `@RequestParam` 사용.
  - 검색/페이징 등 복잡한 경우: Query DTO로 묶어 사용.

```java
@GetMapping("/search")
public ResponseEntity<List<FoodResponseDto>> searchFoods(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize
) {
    // ...
}

@GetMapping
public ResponseEntity<PagedResponse<FoodResponseDto>> getFoods(
        @Valid FoodSearchRequestDto dto
) {
    // ...
}
```

---

### 6. DTO 규칙

#### (1) URL Query Params DTO

- Query Params는 **DTO로 구성**해 사용합니다.
- 필드에 **타입, Optional 여부, 설명, 예시값**을 명시합니다.
- 공통 페이징은 `RequestPagingDto`를 상속해 사용합니다.

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPagingDto {

    @Schema(example = "0", description = "페이지 번호")
    @Min(0)
    private Integer page = 0;

    @Schema(example = "10", description = "페이지 크기")
    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    public int getOffset() {
        return page * pageSize;
    }
}
```

**Query Params 타입 처리**

| 타입 | 처리 방식 |
|------|-----------|
| Number | `@RequestParam Integer age` 등 기본 타입 바인딩 사용 |
| Boolean | 직접 boolean보다는 Enum(`UseYnEnum`) 등으로 처리 권장 |
| Enum | `@RequestParam Status status` 또는 DTO 필드에 Enum 사용 |
| List | `@RequestParam List<Long> foodIdList` 또는 리스트 필드를 가진 DTO 파라미터 |

#### (2) Body (Request / Response DTO)

- Body가 있을 경우 URL Params, Query Params 사용은 최소화합니다.
- Request / Response를 명확히 구분합니다.
  - `UserSaveRequestDto`, `UserUpdateRequestDto`, `UserResponseDto` 등.
- **Request DTO**
  - 필요 시 `toEntity()`, `toCommand()` 등의 인스턴스 변환 메서드 사용.
- **Response DTO**
  - 도메인/엔티티에서 응답 객체를 만들 때 **정적 팩토리 메서드(from/of)** 사용을 기본으로 합니다.

```java
/**
 * 유저 단건 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;
    private String email;
    private String name;

    /**
     * Entity → Response DTO 변환용 정적 팩토리 메서드
     */
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
```

---

### 7. 네이밍 컨벤션

- **클래스명**: PascalCase (`UserService`, `UserController`).
- **메서드명**: camelCase (`findUser`, `updatePassword`).
- **변수명**: camelCase, 축약형 지양 (`user`, `userList`, `accessToken`).
- **상수명**: `UPPER_SNAKE_CASE` (`DEFAULT_PAGE_SIZE`).
- **패키지명**: 소문자, 의미 기반 (`user`, `auth`, `global` 등).

---

### 8.주석 규칙

- “무엇을 했다”보다 **“왜 이렇게 했다”**를 설명합니다.
- 코드만 봐도 알 수 있는 내용(단순 로직 설명)은 주석으로 쓰지 않습니다.
- 공식 설명(Javadoc)은 외부 공개 메서드/복잡한 로직 위주로 사용합니다.

```java

// 외부 시스템에서 null 이름이 들어올 수 있어, 기본값 "게스트"로 치환
String name = Optional.ofNullable(user.getName())
        .orElse("게스트");

/**
 * 유저 저장
 *
 * 외부 시스템에서 중복 생성 요청이 들어올 수 있기 때문에,
 * 동일 이메일이 이미 존재하면 새로 만들지 않고 예외를 던짐
 *
 * @param dto 유저 저장 요청 정보
 * @return 저장된 유저 도메인 객체
 * @throws DuplicateEmailException 동일 이메일을 가진 유저가 이미 존재하는 경우
 */
public UserResponseDto saveUser(UserCreateRequestDto dto) {
    // ...
}
```

---

### 9. Spring 레이어 별 규칙

- **Controller**
  - `@RestController` + DTO 입출력.
  - 비즈니스 로직은 Service로 위임.
  - RESTful URL/HTTP Method 사용.

- **Service**
  - 비즈니스 로직의 중심.
  - 트랜잭션 관리 (`@Transactional`) 담당.
  - 한 메서드는 한 유스케이스만 담당.

- **Repository**
  - Spring Data JPA 중심.
  - 간단한 정적 쿼리는 메서드 이름/`@Query`를 사용하고, 복잡한 동적 조회는 `QueryDSL`을 사용합니다.

- **Entity / Domain**
  - 비즈니스 규칙을 최대한 도메인에 포함.
  - Setter는 가급적 사용하지 않고, 의미 있는 도메인 메서드(`changePassword`, `updateProfile`, `activate` 등)를 제공합니다.

#### Domain(Entity) 구성 컨벤션

- 컬럼명, 타입, 길이, nullable 여부, 코멘트를 최대한 명시합니다.
- DB 컬럼명은 **스네이크 케이스**, 필드명은 **카멜 케이스**를 사용합니다.
- `@Table(name = "tb_user")`, `@Column(name = "user_id", ...)`처럼 이름을 명시합니다.
- Enum은 `@Enumerated(EnumType.STRING)`으로 매핑합니다.

```java
@Entity
@Table(name = "tb_user")
public class User extends BaseTimeEntity {

    // 유저 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 이메일
    @Column(name = "email", length = 120, nullable = false)
    private String email;

    // 비밀번호
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    // 유저 활성/비활성 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    // 도메인 메서드 예시
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
```

---

### 10. 의존성 주입 & Bean 설계

- 생성자 주입 사용 (필수), Lombok `@RequiredArgsConstructor` 선호.
- 필드 주입 금지.
- 트랜잭션: Service 계층에서 `@Transactional`, 조회 전용은 `readOnly = true`.
- 공통 설정은 `global.config` 패키지에 위치.

---

### 11. 예외 처리 & 응답 규격

- 비즈니스별 커스텀 예외 정의 (`UserNotFoundException` 등).
- 전역 예외 처리: `@RestControllerAdvice` + `@ExceptionHandler`.
- 공통 에러 응답 포맷 유지: `code`, `message`, `status`, `timestamp`, `path` 등.
- 요청 DTO 검증: `@Valid` + Bean Validation(`@NotNull`, `@NotBlank`, `@Size`, `@Email` 등).

---

### 12. Lombok 사용 규칙

- 허용: `@Getter`, `@Setter`(엔티티는 최소화), `@RequiredArgsConstructor`, `@Builder`, `@Slf4j`.
- 지양: `@Data` (엔티티/DTO에 무분별 사용 금지).

