package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.math.BigDecimal;

/**
 * 내 여행 상세 화면의 길찾기 액션 응답 DTO입니다.
 * 일정에 연결된 목적지 이름/주소/좌표와 외부 구글 길찾기 앱으로 연결할 URL을 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 상세 길찾기 응답 DTO")
public class MyTripScheduleRouteResponseDto {

    @Schema(description = "여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "목적지 장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "목적지 이름", example = "오사카성")
    private String destinationName;

    @Schema(description = "목적지 주소", example = "1-1 Osakajo, Chuo Ward, Osaka", nullable = true)
    private String destinationAddress;

    @Schema(description = "목적지 위도", example = "34.6873150", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "목적지 경도", example = "135.5262010", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "좌표 사용 가능 여부", example = "true")
    private boolean hasCoordinate;

    @Schema(description = "구글 길찾기 연결 URL", example = "https://www.google.com/maps/dir/?api=1&destination=34.6873150%2C135.5262010")
    private String googleDirectionsUrl;

    /**
     * 길찾기 대상 일정 엔티티와 계산된 길찾기 정보를 바탕으로 응답 DTO를 생성합니다.
     *
     * @param tripSchedule 길찾기 대상 일정 엔티티
     * @param destinationAddress 길찾기 기준 목적지 주소
     * @param googleDirectionsUrl 구글 길찾기 연결 URL
     * @return 내 여행 상세 길찾기 응답 DTO
     */
    public static MyTripScheduleRouteResponseDto of(
            TripSchedule tripSchedule,
            String destinationAddress,
            String googleDirectionsUrl
    ) {
        BigDecimal latitude = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getLatitude() : null;
        BigDecimal longitude = tripSchedule.getPlace() != null ? tripSchedule.getPlace().getLongitude() : null;

        return MyTripScheduleRouteResponseDto.builder()
                .tripId(tripSchedule.getTrip().getTripId())
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                .destinationName(resolveDestinationName(tripSchedule))
                .destinationAddress(destinationAddress)
                .latitude(latitude)
                .longitude(longitude)
                .hasCoordinate(latitude != null && longitude != null)
                .googleDirectionsUrl(googleDirectionsUrl)
                .build();
    }

    /**
     * 목적지 이름은 연결된 장소명이 있으면 우선 사용하고, 없으면 일정 제목으로 대체합니다.
     *
     * @param tripSchedule 길찾기 대상 일정 엔티티
     * @return 화면과 외부 앱에서 표시할 목적지 이름
     */
    private static String resolveDestinationName(TripSchedule tripSchedule) {
        if (tripSchedule.getPlace() != null && tripSchedule.getPlace().getName() != null) {
            return tripSchedule.getPlace().getName();
        }
        return tripSchedule.getTitle();
    }
}
