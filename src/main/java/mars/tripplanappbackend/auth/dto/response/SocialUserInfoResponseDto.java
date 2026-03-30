package mars.tripplanappbackend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfoResponseDto {
    private String socialProviderId;
    private String nickname;
    private String email;
    private String name;
    private String gender;
    private String birthYear;
    private String birthday;
}
