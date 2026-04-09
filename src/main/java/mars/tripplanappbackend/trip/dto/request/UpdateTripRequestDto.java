package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 내 여행 상세 화면에서 여행 기본 정보를 수정할 때 사용하는 요청 DTO입니다.
 * 여행 추가 화면을 재사용하는 기획을 기준으로 여행 제목, 대표 이미지, 시작일, 종료일을 함께 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 일정 수정 요청 DTO")
public class UpdateTripRequestDto {

    @Schema(hidden = true)
    private Long tripId;

    @Schema(hidden = true)
    private String usersId;

    @NotBlank(message = "여행 제목은 필수입니다.")
    @Size(max = 10, message = "여행 제목은 10자 이하로 입력해주세요.")
    @Schema(description = "여행 제목", example = "오사카 여행")
    private String title;

    @Size(max = 500, message = "여행 이미지 URL은 500자 이하로 입력해주세요.")
    @Schema(
            description = "여행 대표 이미지 URL",
            example = "https://cdn.lets-trip.com/trips/osaka.jpg",
            nullable = true
    )
    private String imageUrl;

    @NotNull(message = "여행 시작일은 필수입니다.")
    @Schema(description = "여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수입니다.")
    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    /**
     * 컨트롤러에서 전달받은 경로 변수, 로그인 사용자 정보, 본문 데이터를 하나의 서비스용 DTO로 합칩니다.
     *
     * @param tripId 수정 대상 여행 PK
     * @param usersId 현재 로그인한 사용자 아이디
     * @param requestDto 컨트롤러에서 전달받은 본문 DTO
     * @return 서비스 계층에서 사용할 여행 수정 요청 DTO
     */
    public static UpdateTripRequestDto of(Long tripId, String usersId, UpdateTripRequestDto requestDto) {
        UpdateTripRequestDto serviceRequestDto = new UpdateTripRequestDto();
        serviceRequestDto.tripId = tripId;
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.title = requestDto.title;
        serviceRequestDto.imageUrl = requestDto.imageUrl;
        serviceRequestDto.startDate = requestDto.startDate;
        serviceRequestDto.endDate = requestDto.endDate;
        return serviceRequestDto;
    }
}
