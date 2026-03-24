package mars.tripplanappbackend.global.config.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰에서 추출한 인증 사용자 정보를 담는 객체입니다.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private String usersId;
    private String email;
    private String role;
}
