package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 여행 상세의 "일정 수정" 화면에서 전달되는 요청 DTO입니다.
 * <p>
 * 컨트롤러에서는 경로 변수(tripId, tripScheduleId), 인증 사용자(usersId), 본문 입력값을 하나로 묶어
 * 서비스 계층으로 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "일정 수정 요청 DTO")
public class UpdateTripScheduleRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private Long tripScheduleId;

    @Schema(hidden = true)
    private String usersId;

    @NotBlank(message = "일정명은 필수입니다.")
    @Size(max = 10, message = "일정명은 10자 이내여야 합니다.")
    @Schema(description = "일정명(10자 이내)", example = "점심 식사")
    private String title;

    @NotNull(message = "일정 날짜는 필수입니다.")
    @Schema(description = "일정 날짜(여행 기간 내)", example = "2026-03-02")
    private LocalDate scheduleDate;

    @NotNull(message = "시작 시간은 필수입니다.")
    @Schema(description = "일정 시작 시간", example = "11:30")
    private LocalTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    @Schema(description = "일정 종료 시간", example = "12:30")
    private LocalTime endTime;

    @Schema(description = "장소 PK(지도 선택 시 전달)", example = "7", nullable = true)
    private Long placeId;

    @Size(max = 100, message = "메모는 100자 이내여야 합니다.")
    @Schema(description = "메모(100자 이내)", example = "현지 맛집 방문", nullable = true)
    private String memo;

    /**
     * 컨트롤러 입력값을 서비스 계층 전용 DTO로 조합합니다.
     *
     * @param tripId 여행 PK
     * @param tripScheduleId 수정할 일정 PK
     * @param usersId 로그인 사용자 계정 ID
     * @param requestDto 컨트롤러에서 전달받은 본문 DTO
     * @return 서비스 계층에서 사용할 일정 수정 요청 DTO
     */
    public static UpdateTripScheduleRequestDto of(
            Long tripId,
            Long tripScheduleId,
            String usersId,
            UpdateTripScheduleRequestDto requestDto
    ) {
        UpdateTripScheduleRequestDto serviceRequestDto = new UpdateTripScheduleRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.tripScheduleId = tripScheduleId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.title = requestDto.title;
        serviceRequestDto.scheduleDate = requestDto.scheduleDate;
        serviceRequestDto.startTime = requestDto.startTime;
        serviceRequestDto.endTime = requestDto.endTime;
        serviceRequestDto.placeId = requestDto.placeId;
        serviceRequestDto.memo = requestDto.memo;
        return serviceRequestDto;
    }
}
