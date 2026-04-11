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
    WISHLIST_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "WISHLIST_PLACE_ALREADY_EXISTS", "이미 위시리스트에 추가한 장소입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "리프레시 토큰이 만료되었습니다."),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAIL", "이메일 전송에 실패했습니다."),
    INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_CODE", "인증 코드가 올바르지 않습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_CODE_EXPIRED", "인증 코드가 만료되었습니다."),
    INVALID_EMAIL_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_REQUEST", "이메일 요청이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
