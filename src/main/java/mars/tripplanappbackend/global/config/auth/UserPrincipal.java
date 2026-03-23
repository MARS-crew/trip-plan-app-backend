package mars.tripplanappbackend.global.config.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컨트롤러에서 사용할 현재 사용자 인증 정보 객체입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPrincipal {

    private String usersId;

    /**
     * 인증 주체 문자열을 컨트롤러용 사용자 객체로 변환합니다.
     *
     * @param usersId JWT subject에 저장된 사용자 아이디
     * @return 컨트롤러에서 사용할 사용자 인증 정보
     */
    public static UserPrincipal from(String usersId) {
        return UserPrincipal.builder()
                .usersId(usersId)
                .build();
    }
}
