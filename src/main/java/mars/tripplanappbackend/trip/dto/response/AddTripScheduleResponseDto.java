package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 추가 API 성공 응답 DTO입니다.
 * 생성 직후 상세 화면이 바로 갱신될 수 있도록 일정 핵심 정보를 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "일정 추가 응답 DTO")
public class AddTripScheduleResponseDto {

    @Schema(description = "일정이 추가된 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "생성된 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "여행 시작일 기준 일차", example = "2")
    private int dayNo;

    @Schema(description = "일정 날짜", example = "2026-03-02")
    private LocalDate scheduleDate;

    @Schema(description = "일정명", example = "점심 식사")
    private String title;

    @Schema(description = "시작 시간", example = "11:30")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "12:30")
    private LocalTime endTime;

    @Schema(description = "장소 PK", example = "7", nullable = true)
    private Long placeId;

    @Schema(description = "장소명", example = "도톤보리", nullable = true)
    private String placeName;

    @Schema(description = "장소 주소", example = "1 Chome Dotonbori, Chuo Ward, Osaka", nullable = true)
    private String address;

    @Schema(description = "메모", example = "웨이팅 20분 예상", nullable = true)
    private String memo;

    @Schema(description = "일정 추가 성공 여부", example = "true")
    private boolean added;

    /**
     * 저장된 TripSchedule 엔티티를 일정 추가 응답 DTO로 변환합니다.
     *
     * @param tripSchedule 저장 완료된 일정 엔티티
     * @return 일정 추가 응답 DTO
     */
    public static AddTripScheduleResponseDto from(TripSchedule tripSchedule) {
        return AddTripScheduleResponseDto.builder()
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
                .added(true)
                .build();
    }
}
