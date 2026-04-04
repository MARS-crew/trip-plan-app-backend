package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 내 여행 일정 날짜별 조회 응답 DTO입니다.
 * 선택 날짜와 드롭다운 날짜 옵션, 해당 날짜 일정 목록을 함께 반환합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 날짜별 조회 응답")
public class MyTripScheduleByDateResponseDto {

    @Schema(description = "여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "선택된 일정 날짜", example = "2026-02-15")
    private LocalDate selectedDate;

    @Schema(description = "선택된 날짜의 일차 번호", example = "1")
    private int selectedDayNo;

    @Schema(description = "화면 표시용 일차 라벨", example = "1일차 (2월 15일)")
    private String selectedDayLabel;

    @Schema(description = "선택 날짜 일정 개수", example = "5")
    private int scheduleCount;

    @Schema(description = "날짜 선택 드롭다운 목록")
    private List<MyTripScheduleDateOptionResponseDto> dateOptions;

    @Schema(description = "선택 날짜 일정 목록")
    private List<MyTripScheduleItemResponseDto> schedules;

    /**
     * 날짜별 일정 조회 결과로 응답 DTO를 생성합니다.
     *
     * @param tripId 여행 PK
     * @param tripTitle 여행 제목
     * @param selectedDate 선택된 일정 날짜
     * @param selectedDayNo 선택된 날짜의 일차 번호
     * @param dateOptions 날짜 선택 드롭다운 목록
     * @param schedules 선택된 날짜 일정 목록
     * @return 내 여행 일정 날짜별 조회 응답 DTO
     */
    public static MyTripScheduleByDateResponseDto of(
            Long tripId,
            String tripTitle,
            LocalDate selectedDate,
            int selectedDayNo,
            List<MyTripScheduleDateOptionResponseDto> dateOptions,
            List<MyTripScheduleItemResponseDto> schedules
    ) {
        return MyTripScheduleByDateResponseDto.builder()
                .tripId(tripId)
                .tripTitle(tripTitle)
                .selectedDate(selectedDate)
                .selectedDayNo(selectedDayNo)
                .selectedDayLabel(selectedDayNo + "일차 (" + selectedDate.getMonthValue() + "월 " + selectedDate.getDayOfMonth() + "일)")
                .scheduleCount(schedules.size())
                .dateOptions(dateOptions)
                .schedules(schedules)
                .build();
    }
}
