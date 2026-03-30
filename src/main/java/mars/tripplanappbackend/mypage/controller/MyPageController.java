package mars.tripplanappbackend.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.mypage.dto.request.UpdateProfileRequestDto;
import mars.tripplanappbackend.mypage.dto.resopnse.AgreeResponseDto;
import mars.tripplanappbackend.mypage.dto.resopnse.MyProfileResponseDto;
import mars.tripplanappbackend.mypage.dto.resopnse.UpdateProfileResponseDto;
import mars.tripplanappbackend.mypage.service.MyPageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 엔드포인트")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/me")
    @ApiErrorExceptions({ErrorCode.FORBIDDEN, ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "프로필 조회", description = "프로필 조회 api")
    public ApiResponse<MyProfileResponseDto> getUserInfo(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        MyProfileResponseDto response = myPageService.getMyProfile(usersId);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/me")
    @Operation(summary = "프로필 수정", description = "사용자 프로필 수정, 한 컬럼씩 수정 가능")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.INVALID_TOKEN, ErrorCode.INVALID_INPUT, ErrorCode.PASSWORD_MISMATCH})
    public ApiResponse<UpdateProfileResponseDto> updateProfile(
            @RequestBody @Valid UpdateProfileRequestDto requestDto,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        UpdateProfileResponseDto response = myPageService.updateProfile(usersId, requestDto);
        return ApiResponse.ok(response);
    }

    @GetMapping("/agree")
    @Operation(summary = "알림 설정 조회", description = "푸시 알림 설정 조회 api")
    public ApiResponse<AgreeResponseDto> getNotificationSetting(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        AgreeResponseDto response = myPageService.getAgree(usersId);
        return ApiResponse.ok(response);
    }
}