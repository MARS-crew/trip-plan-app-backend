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
 * 내 여행 상세의 지도 페이지에서 일정별 핀과 이동 동선을 그릴 때 사용하는 일정 위치 항목 DTO입니다.
 * 일정 순서와 핀 순서를 함께 내려주어 프론트엔드가 별도 재계산 없이
 * 핀 번호, 경로 연결 순서, 현재 일정 강조 처리를 바로 수행할 수 있도록 설계합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 위치 항목 응답 DTO")
public class MyTripScheduleLocationItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "여행 기준 일차 번호", example = "1")
    private Integer dayNo;

    @Schema(description = "일정 날짜", example = "2026-04-10")
    private LocalDate scheduleDate;

    @Schema(description = "전체 일정 순서", example = "1")
    private int scheduleOrder;

    @Schema(description = "지도 핀 순서, 좌표가 없으면 null", example = "1", nullable = true)
    private Integer pinOrder;

    @Schema(description = "일정에 연결된 장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "일정 제목", example = "간사이 공항 도착")
    private String title;

    @Schema(description = "일정 장소명", example = "간사이 국제공항", nullable = true)
    private String placeName;

    @Schema(description = "지도와 하단 카드에서 사용할 주소", example = "오사카 공항", nullable = true)
    private String address;

    @Schema(description = "시작 시간", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "09:00:00")
    private LocalTime endTime;

    @Schema(description = "일정 메모", example = "입국 심사 후 숙소로 이동", nullable = true)
    private String memo;

    @Schema(description = "장소 위도", example = "34.4349980", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "장소 경도", example = "135.2440030", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "지도에 핀을 찍을 수 있는 좌표 보유 여부", example = "true")
    private boolean hasLocation;

    @Schema(description = "현재 시각 기준 진행 중 일정 여부", example = "false")
    private boolean isCurrent;

    @Schema(description = "이미 방문 인증된 장소 여부", example = "false")
    private boolean visited;

    @Schema(description = "현재 일정 기준 방문 인증 버튼 노출 가능 여부", example = "true")
    private boolean canAddVisitedPlace;

    /**
     * 여행 일정 엔티티를 지도 페이지 전용 위치 항목 DTO로 변환합니다.
     * 주소는 일정에 저장된 주소를 우선 사용하고, 비어 있으면 장소 마스터 주소로 보완합니다.
     *
     * @param tripSchedule 변환할 여행 일정 엔티티
     * @param scheduleOrder 전체 일정 순서
     * @param pinOrder 지도에 핀을 찍는 순서, 좌표가 없으면 null
     * @param isCurrent 현재 진행 중 일정 여부
     * @param visited 방문 인증 완료 여부
     * @param canAddVisitedPlace 방문 인증 버튼 노출 가능 여부
     * @return 지도 페이지에서 바로 사용할 일정 위치 항목 DTO
     */
    public static MyTripScheduleLocationItemResponseDto from(
            TripSchedule tripSchedule,
            int scheduleOrder,
            Integer pinOrder,
            boolean isCurrent,
            boolean visited,
            boolean canAddVisitedPlace
    ) {
        Place place = tripSchedule.getPlace();
        BigDecimal latitude = place != null ? place.getLatitude() : null;
        BigDecimal longitude = place != null ? place.getLongitude() : null;
        boolean hasLocation = latitude != null && longitude != null;

        return MyTripScheduleLocationItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .dayNo(tripSchedule.getDayNo())
                .scheduleDate(tripSchedule.getScheduleDate())
                .scheduleOrder(scheduleOrder)
                .pinOrder(pinOrder)
                .placeId(place != null ? place.getPlaceId() : null)
                .title(tripSchedule.getTitle())
                .placeName(place != null ? place.getName() : null)
                .address(resolveAddress(tripSchedule, place))
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .memo(tripSchedule.getMemo())
                .latitude(latitude)
                .longitude(longitude)
                .hasLocation(hasLocation)
                .isCurrent(isCurrent)
                .visited(visited)
                .canAddVisitedPlace(canAddVisitedPlace)
                .build();
    }

    /**
     * 일정에 직접 입력한 주소가 있으면 우선 사용하고,
     * 없으면 장소 마스터 주소를 대신 사용해 지도 하단 카드 정보가 비지 않도록 보완합니다.
     *
     * @param tripSchedule 주소 정보를 포함한 여행 일정 엔티티
     * @param place 연결된 장소 엔티티
     * @return 화면에 표시할 주소 문자열
     */
    private static String resolveAddress(TripSchedule tripSchedule, Place place) {
        if (hasText(tripSchedule.getAddress())) {
            return tripSchedule.getAddress();
        }

        return place != null ? place.getAddress() : null;
    }

    /**
     * 문자열이 실제 표시 가능한 값인지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return 공백이 아닌 문자열이면 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
