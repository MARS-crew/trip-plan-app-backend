package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 가까운 여행일정의 개별 일정 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "가까운 여행일정 항목")
public class NearbyTripScheduleItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "11")
    private Long tripScheduleId;

    @Schema(description = "일정 날짜", example = "2026-03-25")
    private LocalDate scheduleDate;

    @Schema(description = "시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "11:30:00")
    private LocalTime endTime;

    @Schema(description = "일정 제목", example = "해운대 산책")
    private String title;

    @Schema(description = "장소명", example = "해운대 해수욕장")
    private String placeName;

    @Schema(description = "주소", example = "부산 해운대구 우동")
    private String address;

    @Schema(description = "메모", example = "오전에는 산책하고 점심은 근처 맛집 방문")
    private String memo;

    /**
     * 여행 일정 엔티티를 개별 일정 응답 DTO로 변환한다.
     *
     * @param tripSchedule 여행 일정 엔티티
     * @return 가까운 여행일정 항목 DTO
     */
    public static NearbyTripScheduleItemResponseDto from(TripSchedule tripSchedule) {
        return NearbyTripScheduleItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .scheduleDate(tripSchedule.getScheduleDate())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                .address(tripSchedule.getAddress())
                .memo(tripSchedule.getMemo())
                .build();
    }
}
