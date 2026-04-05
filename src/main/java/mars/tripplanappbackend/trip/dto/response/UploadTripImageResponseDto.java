package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 여행 추가 화면의 이미지 업로드 결과를 반환하는 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "여행 이미지 업로드 응답 DTO")
public class UploadTripImageResponseDto {

    @Schema(description = "업로드된 여행 이미지 접근 URL", example = "/uploads/trips/2e1e3f8a-aaaa-bbbb-cccc-1234567890ab.jpg")
    private String imageUrl;

    @Schema(description = "업로드한 원본 파일명", example = "osaka-trip.jpg")
    private String originalFileName;

    /**
     * 업로드가 완료된 이미지 URL과 원본 파일명으로 응답 DTO를 생성합니다.
     *
     * @param imageUrl 업로드된 파일에 접근할 수 있는 URL
     * @param originalFileName 사용자가 업로드한 원본 파일명
     * @return 여행 이미지 업로드 응답 DTO
     */
    public static UploadTripImageResponseDto of(String imageUrl, String originalFileName) {
        return UploadTripImageResponseDto.builder()
                .imageUrl(imageUrl)
                .originalFileName(originalFileName)
                .build();
    }
}
