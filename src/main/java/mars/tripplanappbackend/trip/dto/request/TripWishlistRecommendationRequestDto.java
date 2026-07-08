package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "위시리스트 실시간 추천 장소 조회 요청 DTO")
public class TripWishlistRecommendationRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @Schema(description = "현재 zoom 영역의 중심 위도", example = "43.062096")
    private Double latitude;

    @Schema(description = "현재 zoom 영역의 중심 경도", example = "141.354376")
    private Double longitude;

    @Schema(description = "현재 zoom 영역 검색 반경(미터)", example = "1500", defaultValue = "1500")
    private Double radiusMeters;

    @Schema(description = "조회할 추천 장소 개수", example = "1", defaultValue = "1")
    private Integer limit;

    private TripWishlistRecommendationRequestDto() {
    }

    public Long getTripId() {
        return tripId;
    }

    public String getUsersId() {
        return usersId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getRadiusMeters() {
        return radiusMeters;
    }

    public Integer getLimit() {
        return limit;
    }

    public static TripWishlistRecommendationRequestDto of(
            Long tripId,
            String usersId,
            Double latitude,
            Double longitude,
            Double radiusMeters,
            Integer limit
    ) {
        TripWishlistRecommendationRequestDto requestDto = new TripWishlistRecommendationRequestDto();
        requestDto.tripId = tripId;
        requestDto.usersId = usersId;
        requestDto.latitude = latitude;
        requestDto.longitude = longitude;
        requestDto.radiusMeters = radiusMeters;
        requestDto.limit = limit;
        return requestDto;
    }
}
