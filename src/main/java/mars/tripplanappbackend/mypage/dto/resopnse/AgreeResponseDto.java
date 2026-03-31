package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.mypage.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgreeResponseDto {
    @Schema(description = "푸시 알림", example = "Y")
    private UseYnEnum marketingAgreed;

    @Schema(description = "야간 푸시 알림", example = "Y")
    private UseYnEnum nightMarketingAgreed;

    public AgreeResponseDto(User user) {
        this.marketingAgreed=user.getMarketingAgreed();
        this.nightMarketingAgreed=user.getNightMarketingAgreed();
    }


}