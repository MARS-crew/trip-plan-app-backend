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
 * 내 여행 추가 요청 DTO입니다.
 * 여행 제목, 여행 이미지, 여행 시작일과 종료일, 인증 사용자 아이디를 함께 전달합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 여행 추가 요청 DTO")
public class CreateTripRequestDto {

    @Schema(hidden = true)
    private String usersId;

    @NotBlank(message = "여행 제목은 필수입니다.")
    @Size(max = 10, message = "여행 제목은 10자 이하로 입력해주세요.")
    @Schema(description = "여행 제목", example = "오사카 여행")
    private String title;

    @Size(max = 500, message = "여행 이미지 URL은 500자 이하로 입력해주세요.")
    @Schema(description = "여행 대표 이미지 URL", example = "https://cdn.lets-trip.com/trips/osaka.jpg", nullable = true)
    private String imageUrl;

    @NotNull(message = "여행 시작일은 필수입니다.")
    @Schema(description = "여행 시작일", example = "2026-04-07")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수입니다.")
    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    /**
     * 컨트롤러에서 전달받은 본문 값과 인증 사용자 아이디를 합쳐 서비스 요청 DTO를 생성합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param requestDto 컨트롤러에서 전달받은 여행 추가 본문 DTO
     * @return 서비스 계층에 전달할 여행 추가 요청 DTO
     */
    public static CreateTripRequestDto of(String usersId, CreateTripRequestDto requestDto) {
        CreateTripRequestDto serviceRequestDto = new CreateTripRequestDto();
        serviceRequestDto.usersId = usersId;
        serviceRequestDto.title = requestDto.title;
        serviceRequestDto.imageUrl = requestDto.imageUrl;
        serviceRequestDto.startDate = requestDto.startDate;
        serviceRequestDto.endDate = requestDto.endDate;
        return serviceRequestDto;
    }
}
