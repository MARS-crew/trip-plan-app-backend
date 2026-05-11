package mars.tripplanappbackend.image.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.File;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponseDto{
    @Schema(description = "파일을 업로드할 실제 URL (PUT 요청용)", example = "https://minio.../reviews/1/uuid_123.png?X-Amz-...")
    private String uploadUrl;

    @Schema(description = "서버 DB에 저장할 파일 경로 (Object Key)", example = "reviews/1/uuid_123.png")
    private String storagePath;

    @Schema(description = "파일 도메인", example = "TRIP")
    private File domain;
 }