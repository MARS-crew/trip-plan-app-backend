package mars.tripplanappbackend.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 여행 추가 화면에서 대표 이미지를 업로드할 때 사용하는 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "여행 이미지 업로드 요청 DTO")
public class UploadTripImageRequestDto {

    @Schema(hidden = true)
    private String usersId;

    @Schema(description = "업로드할 여행 대표 이미지 파일", type = "string", format = "binary")
    private MultipartFile imageFile;

    /**
     * 컨트롤러에서 받은 인증 사용자 정보와 업로드 파일을 서비스 요청 DTO로 변환합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param imageFile 업로드할 여행 대표 이미지 파일
     * @return 서비스 계층에 전달할 여행 이미지 업로드 요청 DTO
     */
    public static UploadTripImageRequestDto of(String usersId, MultipartFile imageFile) {
        UploadTripImageRequestDto requestDto = new UploadTripImageRequestDto();
        requestDto.usersId = usersId;
        requestDto.imageFile = imageFile;
        return requestDto;
    }
}
