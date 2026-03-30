package mars.tripplanappbackend.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.mypage.dto.resopnse.MyProfileResponseDto;
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

}
