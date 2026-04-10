package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 내 일정 수정 API 호출 후 화면에서 바로 갱신할 수 있는 여행 요약 정보를 담는 응답 DTO입니다.
 * 수정 이후 카드와 상세 화면에서 공통으로 사용할 수 있도록 상태, 일정 개수, 여행 일수까지 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "내 일정 수정 응답 DTO")
public class UpdateTripResponseDto {

    @Schema(description = "수정된 여행 PK", example = "1")
    private Long tripId;

    @Schema(description = "수정된 여행 제목", example = "오사카 여행")
    private String title;

    @Schema(
            description = "수정된 여행 대표 이미지 URL",
            example = "https://cdn.lets-trip.com/trips/osaka.jpg",
            nullable = true
    )
    private String imageUrl;

    @Schema(description = "여행 상태 코드", example = "PLANNED")
    private TripStatus tripStatus;

    @Schema(description = "여행 상태 한글 라벨", example = "예정")
    private String tripStatusLabel;

    @Schema(description = "수정된 여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @Schema(description = "수정된 여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "수정 후 유지되는 일정 개수", example = "3")
    private int scheduleCount;

    @Schema(description = "수정된 여행 일수", example = "5")
    private long tripDayCount;

    /**
     * 수정된 여행 엔티티와 계산된 부가 정보를 조합해 응답 DTO를 생성합니다.
     *
     * @param trip 수정이 반영된 여행 엔티티
     * @param tripStatus 현재 날짜 기준으로 다시 계산한 여행 상태
     * @param scheduleCount 수정 후에도 남아 있는 일정 개수
     * @return 내 일정 수정 응답 DTO
     */
    public static UpdateTripResponseDto from(Trip trip, TripStatus tripStatus, int scheduleCount) {
        return UpdateTripResponseDto.builder()
                .tripId(trip.getTripId())
                .title(trip.getTitle())
                .imageUrl(trip.getImageUrl())
                .tripStatus(tripStatus)
                .tripStatusLabel(resolveTripStatusLabel(tripStatus))
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .scheduleCount(scheduleCount)
                .tripDayCount(ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1)
                .build();
    }

    /**
     * 여행 상태 코드를 화면 표시용 한글 문구로 변환합니다.
     *
     * @param tripStatus 현재 날짜 기준으로 계산된 여행 상태
     * @return 화면에 표시할 여행 상태 라벨
     */
    private static String resolveTripStatusLabel(TripStatus tripStatus) {
        return switch (tripStatus) {
            case PLANNED -> "예정";
            case ONGOING -> "여행 중";
            case COMPLETED -> "종료";
        };
    }
}
