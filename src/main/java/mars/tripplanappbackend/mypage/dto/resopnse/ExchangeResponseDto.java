package mars.tripplanappbackend.mypage.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeResponseDto {
    @Schema(description = "통화 코드", example = "JPY, 통화 코드")
    private String curUnit;

    @Schema(description = "통화명", example = "일본 엔, 통화명")
    private String curNm;

    @Schema(description = "1 KRW 기준 환율", example = "0.110000, 1 KRW 기준 환율")
    private double dealBasR;

    @Schema(description = "변환된 금액", example = "1100.0, 변환된 금액")
    private double convertedAmount;

    @Schema(description = "기준 날짜", example = "2024-06-01, 환율 기준 날짜")
    private String searchDate;
}
