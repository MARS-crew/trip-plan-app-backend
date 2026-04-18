package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 수정 API 성공 응답 DTO입니다.
 * 수정 직후 프론트 화면을 즉시 갱신할 수 있도록 일정 상세 정보를 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "일정 수정 응답 DTO")
public class UpdateTripScheduleResponseDto {

    @Schema(description = "일정이 속한 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "수정된 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "여행 시작일 기준 일차", example = "2")
    private int dayNo;

    @Schema(description = "일정 날짜", example = "2026-03-02")
    private LocalDate scheduleDate;

    @Schema(description = "일정명", example = "점심 식사")
    private String title;

    @Schema(description = "일정 시작 시간", example = "11:30")
    private LocalTime startTime;

    @Schema(description = "일정 종료 시간", example = "12:30")
    private LocalTime endTime;

    @Schema(description = "장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "장소명", example = "오도리 공원", nullable = true)
    private String placeName;

    @Schema(description = "장소 주소", example = "Odorinishi, Chuo Ward, Sapporo", nullable = true)
    private String address;

    @Schema(description = "메모", example = "현지 맛집 방문", nullable = true)
    private String memo;

    @Schema(description = "일정 수정 성공 여부", example = "true")
    private boolean updated;

    /**
     * 수정 완료된 TripSchedule 엔티티를 응답 DTO로 변환합니다.
     *
     * @param tripSchedule 수정된 일정 엔티티
     * @return 일정 수정 응답 DTO
     */
    public static UpdateTripScheduleResponseDto from(TripSchedule tripSchedule) {
        return UpdateTripScheduleResponseDto.builder()
                .tripId(tripSchedule.getTrip().getTripId())
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .dayNo(tripSchedule.getDayNo())
                .scheduleDate(tripSchedule.getScheduleDate())
                .title(tripSchedule.getTitle())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                .address(tripSchedule.getAddress())
                .memo(tripSchedule.getMemo())
                .updated(true)
                .build();
    }
}
