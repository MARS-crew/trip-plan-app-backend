package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.mypage.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponseDto {
    @Schema(description = "닉네임", example = "dmdkr")
    private String nickname;

    @Schema(description = "이메일", example = "cye452687@gmail.com")
    private String email;

    @Schema(description = "여행 횟수", example = "2")
    private long tripCount;

    @Schema(description = "저장된 장소", example = "3")
    private long savedPlaceCount;

    @Schema(description = "방문한 장소", example = "2")
    private long visitedPlaceCount;
}
