package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 내 여행 전체 리스트 조회 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 전체 리스트 조회 응답")
public class MyTripListResponseDto {

    @Schema(description = "조회된 여행 카드 개수", example = "3")
    private int tripCount;

    @Schema(description = "내 여행 카드 목록")
    private List<MyTripSummaryResponseDto> trips;

    /**
     * 여행 카드 목록으로 내 여행 전체 리스트 응답 DTO를 생성합니다.
     *
     * @param trips 내 여행 카드 목록
     * @return 내 여행 전체 리스트 조회 응답 DTO
     */
    public static MyTripListResponseDto of(List<MyTripSummaryResponseDto> trips) {
        return MyTripListResponseDto.builder()
                .tripCount(trips.size())
                .trips(trips)
                .build();
    }
}
