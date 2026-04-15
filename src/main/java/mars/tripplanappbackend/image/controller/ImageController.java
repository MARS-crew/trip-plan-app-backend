package mars.tripplanappbackend.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.enums.File;
import mars.tripplanappbackend.image.response.PresignedUrlResponseDto;
import mars.tripplanappbackend.image.service.ImageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 엔드포인트")
public class ImageController {
    private final ImageService imageService;

    @GetMapping("/{domain}/upload-url")
    @Operation(summary = "업로드용 Presigned URL 생성", description = "파일 이름을 받아 minio 업로드용 임시 URL을 반환함. 임시 url의 경우 5분이 시간 제한이 있어 5분이 지나면 파일 저장 못함." +
            " domain에는 trip, review 두 개를 기능별로 선택해서 진행. 현재 가능한 파일 유형은 png, jpg, jpeg, webp에 10mb 제한있음")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT,
            ErrorCode.INTERNAL_ERROR, ErrorCode.INVALID_FILE_FORMAT})
    public ApiResponse<PresignedUrlResponseDto> getUploadUrl(
            @PathVariable File domain,
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam String fileName
    ) {
        String usersId = userPrincipal.getUsersId();
        PresignedUrlResponseDto response = imageService.getUploadUrl(usersId, fileName, domain);
        return ApiResponse.ok(response);
    }
}
