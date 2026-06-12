package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "방문 기록 저장 요청 DTO")
public class AddVisitedPlaceRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @Schema(
            description = "방문 기록으로 저장할 장소 PK. 생략 시 일정에 연결된 장소나 일정 위치 정보로 자동 복구합니다.",
            example = "8",
            nullable = true
    )
    private Long placeId;

    @NotNull(message = "방문 기록과 연결할 일정 PK는 필수입니다.")
    @Schema(description = "방문 기록과 연결할 일정 PK", example = "7")
    private Long tripScheduleId;

    public static AddVisitedPlaceRequestDto of(Long tripId, String usersId, AddVisitedPlaceRequestDto requestDto) {
        AddVisitedPlaceRequestDto serviceRequestDto = new AddVisitedPlaceRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.placeId = requestDto.placeId;
        serviceRequestDto.tripScheduleId = requestDto.tripScheduleId;
        return serviceRequestDto;
    }
}
