package mars.tripplanappbackend.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequestDto {
    @Schema(description = "통화 코드", example = "JPY")
    private String curUnit;

    @Schema(description = "변환할 금액", example = "10000")
    private double amount;

    @Schema(description = "KRW에서 변환하는지 여부", example = "true")
    private boolean fromKrw;

}
