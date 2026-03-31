package mars.tripplanappbackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.auth.enums.WithdrawalReasonType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestDto {
    @Schema(description = "탈퇴 사유 타입", example = "OTHER")
    private WithdrawalReasonType reasonType;

    @Schema(description = "기타 사유 텍스트 (기타 선택 시에만)", example = "직접 입력한 사유")
    private String reasonText;
}
