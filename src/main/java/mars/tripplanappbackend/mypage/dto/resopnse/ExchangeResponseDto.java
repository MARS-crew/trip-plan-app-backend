package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeResponseDto {
    @Schema(description = "통화 코드", example = "JPY")
    private String curUnit;

    @Schema(description = "통화명", example = "일본 엔")
    private String curNm;

    @Schema(description = "매매 기준율", example = "0.110000")
    private double dealBasR;

    @Schema(description = "변환된 금액", example = "1100.0")
    private double convertedAmount;
}
