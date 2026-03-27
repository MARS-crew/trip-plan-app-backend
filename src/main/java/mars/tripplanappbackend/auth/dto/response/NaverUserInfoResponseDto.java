package mars.tripplanappbackend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponseDto {

    private NaverAccount response;

    @Getter
    @NoArgsConstructor
    public static class NaverAccount {
        private String id;
        private String email;
        private String name;
        private String nickname;
        private String gender;

        @JsonProperty("birthyear")
        private String birthYear;

        private String birthday;
    }
}
