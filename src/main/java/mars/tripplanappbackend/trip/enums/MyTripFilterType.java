package mars.tripplanappbackend.trip.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 여행 페이지 상단 필터 탭에서 사용하는 여행 조회 유형입니다.
 */
@Schema(description = "내 여행 필터 유형")
public enum MyTripFilterType {

    @Schema(description = "전체 여행 조회")
    ALL,

    @Schema(description = "예정된 여행 탭 조회(여행 예정, 여행 중)")
    UPCOMING,

    @Schema(description = "지난 여행 탭 조회(여행 종료)")
    PAST
}
