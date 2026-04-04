package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 내 여행 추가 결과 응답 DTO입니다.
 * 생성된 여행 카드에 필요한 핵심 정보를 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 추가 응답 DTO")
public class CreateTripResponseDto {

    @Schema(description = "생성된 여행 PK", example = "21")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String title;

    @Schema(description = "여행 대표 이미지 URL", example = "https://cdn.lets-trip.com/trips/osaka.jpg", nullable = true)
    private String imageUrl;

    @Schema(description = "여행 상태 코드", example = "PLANNED")
    private TripStatus tripStatus;

    @Schema(description = "여행 상태 한글명", example = "예정")
    private String tripStatusLabel;

    @Schema(description = "여행 시작일", example = "2026-04-07")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "생성 직후 일정 개수", example = "0")
    private int scheduleCount;

    @Schema(description = "여행 일수", example = "8")
    private long tripDayCount;

    /**
     * 생성된 여행 엔티티와 계산된 상태값으로 응답 DTO를 생성합니다.
     *
     * @param trip 생성된 여행 엔티티
     * @param tripStatus 현재 날짜 기준으로 계산한 여행 상태
     * @return 내 여행 추가 응답 DTO
     */
    public static CreateTripResponseDto from(Trip trip, TripStatus tripStatus) {
        return CreateTripResponseDto.builder()
                .tripId(trip.getTripId())
                .title(trip.getTitle())
                .imageUrl(trip.getImageUrl())
                .tripStatus(tripStatus)
                .tripStatusLabel(resolveTripStatusLabel(tripStatus))
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .scheduleCount(0)
                .tripDayCount(ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1)
                .build();
    }

    /**
     * 여행 상태 코드를 화면 표시용 한글 문구로 변환합니다.
     *
     * @param tripStatus 현재 여행 상태 코드
     * @return 화면에 표시할 여행 상태 문구
     */
    private static String resolveTripStatusLabel(TripStatus tripStatus) {
        return switch (tripStatus) {
            case PLANNED -> "예정";
            case ONGOING -> "여행 중";
            case COMPLETED -> "종료";
        };
    }
}
