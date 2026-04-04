package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 내 여행 일정 날짜 선택 드롭다운에 사용할 날짜 옵션 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 일정 날짜 옵션")
public class MyTripScheduleDateOptionResponseDto {

    @Schema(description = "일차 번호", example = "1")
    private int dayNo;

    @Schema(description = "일정 날짜", example = "2026-02-15")
    private LocalDate scheduleDate;

    @Schema(description = "드롭다운 표시 라벨", example = "2월 15일")
    private String displayLabel;

    /**
     * 일정 날짜와 일차 번호로 날짜 옵션 응답 DTO를 생성합니다.
     *
     * @param dayNo 여행 기준 일차 번호
     * @param scheduleDate 일정 날짜
     * @return 내 여행 일정 날짜 옵션 응답 DTO
     */
    public static MyTripScheduleDateOptionResponseDto of(int dayNo, LocalDate scheduleDate) {
        return MyTripScheduleDateOptionResponseDto.builder()
                .dayNo(dayNo)
                .scheduleDate(scheduleDate)
                .displayLabel(scheduleDate.getMonthValue() + "월 " + scheduleDate.getDayOfMonth() + "일")
                .build();
    }
}
