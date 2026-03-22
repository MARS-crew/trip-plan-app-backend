package mars.tripplanappbackend.global.config.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰에서 추출한 인증된 사용자 정보를 담는 객체
 *
 * SecurityContext에 저장되며, 컨트롤러에서 @CurrentUser 어노테이션으로 주입받아 사용한다.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private String usersId;
    private String email;
    private String role;
}
