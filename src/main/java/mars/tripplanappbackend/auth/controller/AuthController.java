package mars.tripplanappbackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.request.SocialLoginRequestDto;
import mars.tripplanappbackend.auth.dto.request.LoginRequestDto;
import mars.tripplanappbackend.auth.dto.request.SignupRequestDto;
import mars.tripplanappbackend.auth.dto.request.TokenReissueRequestDto;
import mars.tripplanappbackend.auth.dto.response.SocialLoginResponseDto;
import mars.tripplanappbackend.auth.dto.response.CheckIdResponseDto;
import mars.tripplanappbackend.auth.dto.response.LoginResponseDto;
import mars.tripplanappbackend.auth.dto.response.SignupResponseDto;
import mars.tripplanappbackend.auth.dto.response.TokenReissueResponseDto;
import mars.tripplanappbackend.auth.service.AuthService;
import mars.tripplanappbackend.auth.service.SocialLoginService;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.mypage.enums.LoginType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "auth 엔드포인트")
public class AuthController {
    private final AuthService authService;
    private final SocialLoginService socialLoginService;

    @PostMapping("/signup")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR, ErrorCode.DUPLICATE_USER})
    @Operation(summary = "회원가입",
            description = "회원가입 api, loginType에는 LOCAL, KAKAO, NAVER, GOOGLE 하나를 작성. " +
                    "소셜 로그인 시도 시 비밀번호 미작성해도 회원가입 가능함. 컬럼을 아예 날리거나 null로 작성")
    public SignupResponseDto signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        return authService.signUp(requestDto);
    }

    @PostMapping("/login")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND,
            ErrorCode.PASSWORD_MISMATCH, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "로그인", description = "로그인 api")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = authService.login(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/reissue")
    @ApiErrorExceptions({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰 재발급 api")
    public ApiResponse<TokenReissueResponseDto> reissue(
            @RequestBody TokenReissueRequestDto request) {
        return ApiResponse.ok(authService.reissue(request));
    }

    @PostMapping("/kakao")
    @ApiErrorExceptions({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "카카오 소셜 로그인",
            description = "카카오 소셜 로그인 api, " +
                    "카카오에게서 발급받은 accessToken을 요청으로 보내면 회원가입/로그인 여부를 판단함")
    public ApiResponse<SocialLoginResponseDto> kakaoLogin(@Valid @RequestBody SocialLoginRequestDto requestDto) {
        return ApiResponse.ok(socialLoginService.socialLogin(LoginType.KAKAO, requestDto.getAccessToken()));
    }

    @GetMapping("/check-id")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT,  ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR, ErrorCode.DUPLICATE_USER})
    @Operation(summary = "아이디 중복 확인", description = "아이디 중복 확인 api")
    public ApiResponse<CheckIdResponseDto> checkId(@RequestParam String usersId) {
        CheckIdResponseDto response = authService.checkUsersIdDuplicate(usersId);
        return ApiResponse.ok(response);
    }
}
