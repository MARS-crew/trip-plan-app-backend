package mars.tripplanappbackend.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 요청입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "DUPLICATE_USER", "이미 존재하는 아이디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다."),
    SAVED_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "SAVED_PLACE_ALREADY_EXISTS", "이미 저장한 장소입니다."),
    SAVED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVED_PLACE_NOT_FOUND", "저장한 장소를 찾을 수 없습니다."),
    WISHLIST_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "WISHLIST_PLACE_ALREADY_EXISTS", "이미 위시리스트에 추가한 장소입니다."),
    VISITED_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "VISITED_PLACE_ALREADY_EXISTS", "이미 방문 기록이 있는 장소입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "리프레시 토큰이 만료되었습니다."),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAIL", "이메일 전송에 실패했습니다."),
    INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_CODE", "인증 코드가 올바르지 않습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_CODE_EXPIRED", "인증 코드가 만료되었습니다."),
    EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_MISMATCH", "이메일이 일치하지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "유효하지 않은 이메일 형식입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    FCM_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "FCM_SEND_FAIL", "푸시 알림 전송에 실패했습니다."),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_FCM_TOKEN", "유효하지 않은 FCM 토큰입니다."),
    INVALID_EMAIL_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_REQUEST", "이메일 요청이 유효하지 않습니다."),
    TRANSLATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TRANSLATION_FAILED", "번역에 실패했습니다."),
    EXCHANGE_RATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EXCHANGE_RATE_FAILED", "환율 정보 조회에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_FILE_FORMAT", "지원하지 않는 파일 형식입니다."),
    VISITED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "VISITED_PLACE_NOT_FOUND", "방문 기록을 찾을 수 없습니다."),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "DUPLICATE_REVIEW", "이미 해당 방문 기록에 작성된 리뷰가 있습니다."),
    PLACE_DATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PLACE_DATE_NOT_FOUND", "요청하신 장소와 날짜 조합의 방문 기록을 찾을 수 없습니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "WITHDRAWN_USER", "탈퇴한 사용자입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
