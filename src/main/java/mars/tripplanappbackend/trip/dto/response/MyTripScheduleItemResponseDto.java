package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalTime;

/**
 * 내 여행 일정 날짜별 조회에서 반환하는 일정 항목 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 항목")
public class MyTripScheduleItemResponseDto {

    @Schema(description = "여행 일정 PK", example = "21")
    private Long tripScheduleId;

    @Schema(description = "일정 제목", example = "공항 도착")
    private String title;

    @Schema(description = "일정 장소명", example = "간사이 국제 공항", nullable = true)
    private String placeName;

    @Schema(description = "일정 주소", example = "오사카 공항")
    private String address;

    @Schema(description = "시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:00:00")
    private LocalTime endTime;

    @Schema(description = "일정 메모", example = "공항 도착 후 지하철 탑승", nullable = true)
    private String memo;

    /**
     * 여행 일정 엔티티를 내 여행 일정 항목 응답 DTO로 변환합니다.
     *
     * @param tripSchedule 여행 일정 엔티티
     * @return 내 여행 일정 항목 응답 DTO
     */
    public static MyTripScheduleItemResponseDto from(TripSchedule tripSchedule) {
        return MyTripScheduleItemResponseDto.builder()
                .tripScheduleId(tripSchedule.getTripScheduleId())
                .title(tripSchedule.getTitle())
                .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                .address(tripSchedule.getAddress())
                .startTime(tripSchedule.getStartTime())
                .endTime(tripSchedule.getEndTime())
                .memo(tripSchedule.getMemo())
                .build();
    }
}
