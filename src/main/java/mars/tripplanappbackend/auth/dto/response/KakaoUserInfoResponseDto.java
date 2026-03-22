package mars.tripplanappbackend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 사용자 정보 조회 API(/v2/user/me) 응답 맵핑용 DTO
 * * 카카오 서버가 반환하는 JSON의 계층 구조와 필드명을 그대로 반영하며,
 * 서비스 로직에서 사용하기 전 '임시 저장소' 역할을 한다.
 */

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponseDto {
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private String name;
        private String gender;
        private String birthday;
        @JsonProperty("birthyear")
        private String birthYear;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
        }
    }
}
