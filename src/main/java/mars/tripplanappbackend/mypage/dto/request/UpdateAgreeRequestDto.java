package mars.tripplanappbackend.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgreeRequestDto {

    @Schema(description = "푸시 알림", example = "Y")
    private UseYnEnum marketingAgreed;

    @Schema(description = "야간 푸시 알림", example = "Y")
    private UseYnEnum nightMarketingAgreed;
}