package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 지도 페이지에서 사용하는 일정 위치 응답 DTO입니다.
 * scheduleOrder는 여행 전체 기준 순서이고, pinOrder는 같은 날짜 안에서의 지도 마커 순서입니다.
 */
@Getter
@Builder
@Schema(description = "일정 위치 항목 응답 DTO")
public class MyTripScheduleLocationItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "여행 시작일 기준 일차", example = "2")
    private Integer dayNo;

    @Schema(description = "일정 날짜", example = "2026-04-21")
    private LocalDate scheduleDate;

    @Schema(
            description = "여행 전체 기준 일정 순서. 예: 둘째 날 첫 일정이면 3",
            example = "3"
    )
    private int scheduleOrder;

    @Schema(
            description = "같은 날짜 안에서의 지도 핀 순서. 예: 둘째 날 첫 핀이면 1",
            example = "1",
            nullable = true
    )
    private Integer pinOrder;

    @Schema(description = "일정에 연결된 장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "일정 제목", example = "저녁 식사")
    private String title;

    @Schema(description = "장소명", example = "삿포로 TV 타워", nullable = true)
    private String placeName;

    @Schema(description = "지도 카드에 노출할 실제 주소", example = "오도리니시 1초메, 삿포로", nullable = true)
    private String address;

    @Schema(description = "일정 시작 시간", example = "18:00:00")
    private LocalTime startTime;

    @Schema(description = "일정 종료 시간", example = "19:30:00")
    private LocalTime endTime;

    @Schema(description = "일정 메모", example = "현지 식당 예약 완료", nullable = true)
    private String memo;

    @Schema(description = "장소 소개글", example = "삿포로의 상징적인 전망 명소", nullable = true)
    private String description;

    @Schema(description = "Google Places 기반 대표 이미지 URL", example = "https://lh3.googleusercontent.com/...", nullable = true)
    private String imageUrl;

    @Schema(description = "장소 위도", example = "43.0610", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "장소 경도", example = "141.3564", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "좌표 보유 여부", example = "true")
    private boolean hasLocation;

    @Schema(description = "현재 진행 중인 일정 여부", example = "false")
    private boolean isCurrent;

    @Schema(description = "방문 인증 완료 여부", example = "false")
    private boolean visited;

    @Schema(description = "방문 인증 버튼 노출 가능 여부", example = "true")
    private boolean canAddVisitedPlace;

    /**
     * TripSchedule과 장소 표시용 데이터를 묶어 지도 화면 응답으로 변환합니다.
     * 장소명, 주소, 소개글, 이미지 URL은 외부 API 조회 결과를 우선 사용하고 실패 시 DB 값으로 대체합니다.
     */
    public static MyTripScheduleLocationItemResponseDto from(
            TripSchedule tripSchedule,
            int scheduleOrder,
            Integer pinOrder,
            String placeName,
            String address,
            String description,
            String imageUrl,
            boolean isCurrent,
            boolean visited,
            boolean canAddVisitedPlace
    ) {
        Place place = tripSchedule.getPlace();
        BigDecimal latitude = tripSchedule.resolveLatitude();
        BigDecimal longitude = tripSchedule.resolveLongitude();
        boolean hasLocation = latitude != null && longitude != null;

        return MyTripScheduleLocationItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .dayNo(tripSchedule.getDayNo())
                .scheduleDate(tripSchedule.getScheduleDate())
                .scheduleOrder(scheduleOrder)
                .pinOrder(pinOrder)
                .placeId(place != null ? place.getPlaceId() : null)
                .title(tripSchedule.getTitle())
                .placeName(placeName != null ? placeName : tripSchedule.resolvePlaceName())
                .address(address)
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .memo(tripSchedule.getMemo())
                .description(description != null ? description : place != null ? place.getDescription() : null)
                .imageUrl(imageUrl != null ? imageUrl : place != null ? place.getImageUrl() : null)
                .latitude(latitude)
                .longitude(longitude)
                .hasLocation(hasLocation)
                .isCurrent(isCurrent)
                .visited(visited)
                .canAddVisitedPlace(canAddVisitedPlace)
                .build();
    }
}
