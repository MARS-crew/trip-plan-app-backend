package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 여행 상세 화면에서 하루 단위 섹션을 구성하기 위한 응답 DTO입니다.
 * 일정이 없는 날짜도 빈 리스트와 함께 내려주어 프론트에서 일차 영역을 그대로 그릴 수 있도록 합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일차별 일정 섹션")
public class MyTripDailyScheduleResponseDto {

    @Schema(description = "여행 기준 일차 번호", example = "1")
    private int dayNo;

    @Schema(description = "섹션에 해당하는 일정 날짜", example = "2026-04-10")
    private LocalDate scheduleDate;

    @Schema(description = "일차 라벨", example = "1일차")
    private String dayLabel;

    @Schema(description = "날짜 라벨", example = "04.10")
    private String dateLabel;

    @Schema(description = "해당 날짜 일정 개수", example = "2")
    private int scheduleCount;

    @Schema(description = "해당 날짜 일정 목록")
    private List<MyTripScheduleItemResponseDto> schedules;

    /**
     * 하루 단위 일정 목록으로 일차별 섹션 응답 DTO를 생성합니다.
     *
     * @param dayNo 여행 시작일 기준 일차 번호
     * @param scheduleDate 섹션에 대응되는 일정 날짜
     * @param schedules 해당 날짜 일정 목록
     * @return 일차별 일정 섹션 응답 DTO
     */
    public static MyTripDailyScheduleResponseDto of(
            int dayNo,
            LocalDate scheduleDate,
            List<MyTripScheduleItemResponseDto> schedules
    ) {
        return MyTripDailyScheduleResponseDto.builder()
                .dayNo(dayNo)
                .scheduleDate(scheduleDate)
                .dayLabel(dayNo + "일차")
                .dateLabel(String.format("%02d.%02d", scheduleDate.getMonthValue(), scheduleDate.getDayOfMonth()))
                .scheduleCount(schedules.size())
                .schedules(schedules)
                .build();
    }
}
