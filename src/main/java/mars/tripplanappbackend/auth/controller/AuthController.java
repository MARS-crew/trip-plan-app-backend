package mars.tripplanappbackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.request.*;
import mars.tripplanappbackend.auth.dto.response.*;
import mars.tripplanappbackend.auth.dto.response.CheckIdResponseDto;
import mars.tripplanappbackend.auth.dto.response.LoginResponseDto;
import mars.tripplanappbackend.auth.dto.response.SignupResponseDto;
import mars.tripplanappbackend.auth.dto.response.TokenReissueResponseDto;
import mars.tripplanappbackend.auth.service.AuthService;
import mars.tripplanappbackend.auth.service.SocialLoginService;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
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
                    "소셜 로그인 시도 시 비밀번호 관련 컬럼 삭제 후 진행, 로컬 로그인 시도 시 loginType을 Local 또는 컬러 삭제, socialProviderId 삭제")
    public SignupResponseDto signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        return authService.signUp(requestDto);
    }

    @PostMapping("/login")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
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

    @PostMapping("/naver")
    @ApiErrorExceptions({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "네이버 소셜 로그인",
            description = "네이버 소셜 로그인 api, " +
                    "네이버에서 발급받은 accessToken을 요청으로 보내면 회원가입/로그인 여부를 판단함")
    public ApiResponse<SocialLoginResponseDto> naverLogin(@Valid @RequestBody SocialLoginRequestDto requestDto) {
        return ApiResponse.ok(socialLoginService.socialLogin(LoginType.NAVER, requestDto.getAccessToken()));
    }

    @PostMapping("/google")
    @ApiErrorExceptions({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN,
            ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "구글 소셜 로그인",
            description = "구글 소셜 로그인 api, " +
                    "구글에서 발급받은 accessToken을 요청으로 보내면 회원가입/로그인 여부를 판단함")
    public ApiResponse<SocialLoginResponseDto> googleLogin(@Valid @RequestBody SocialLoginRequestDto requestDto) {
        return ApiResponse.ok(socialLoginService.socialLogin(LoginType.GOOGLE, requestDto.getAccessToken()));
    }

    @GetMapping("/check-id")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR, ErrorCode.DUPLICATE_USER})
    @Operation(summary = "아이디 중복 확인", description = "아이디 중복 확인 api")
    public ApiResponse<CheckIdResponseDto> checkId(@RequestParam String usersId) {
        CheckIdResponseDto response = authService.checkUsersIdDuplicate(usersId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/find-id")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "아이디 찾기", description = "아이디 찾기 api (이메일, 닉네임)")
    public ApiResponse<FindIdResponseDto> findId(@Valid @RequestBody FindIdRequestDto requestDto) {
        FindIdResponseDto response = authService.findUsersId(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/email-request")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "회원가입용 이메일 전송", description = "이메일 전송 api, gmail만 가능")
    public ApiResponse<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto requestDto) {
        EmailResponseDto response = authService.sendEmail(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/email-verify")
    @ApiErrorExceptions({ErrorCode.INVALID_EMAIL_CODE,
            ErrorCode.EMAIL_CODE_EXPIRED, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "회원가입용 이메일 인증", description = "이메일 인증 api, 이메일로 전송된 인증 번호 6자리 입력")
    public ApiResponse<EmailVerifyResponseDto> emailVerify(@Valid @RequestBody EmailVerifyRequestDto requestDto) {
        EmailVerifyResponseDto response = authService.verifyEmailCode(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "로그아웃",
            description = "로그아웃 api")
    public ApiResponse<Void> logout(
            @CurrentUser UserPrincipal userPrincipal) {
        authService.logout(userPrincipal.getUsersId());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원탈퇴", description = "회원탈퇴 api\n\n" +
            "reasonType (탈퇴 유형)\n" +
            "NOT_ENOUGH_ACCESS : 앱에 잘 접속하지 않아요\n" +
            "LOW_REVIEW_TRUST : 리뷰의 신뢰성이 떨어져요\n" +
            "INAPPROPRIATE_TRIP : 여행지 추천이 적당하지 않아요\n" +
            "OTHER : 기타 (reasonText 필수)\n\n" +
            "reasonText(기타 탈퇴 사유): OTHER 선택 시에만 입력")
    public ApiResponse<Void> withdraw(@CurrentUser UserPrincipal userPrincipal,
                                      @RequestBody WithdrawRequestDto requestDto) {
        authService.withdraw(userPrincipal.getUsersId(), requestDto);
        return ApiResponse.ok(null);
    }

    @PostMapping("/password/email-request")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR, ErrorCode.EMAIL_SEND_FAIL})
    @Operation(summary = "임시 비밀번호 이메일 전송", description = "임시 비밀번호 이메일 전송 api, gmail만 가능")
    public ApiResponse<PasswordEmailResponseDto> findPassword(@Valid @RequestBody PasswordEmailRequestDto requestDto) {
        PasswordEmailResponseDto response = authService.sendPasswordResetEmail(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/password/email-verify")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR, ErrorCode.EMAIL_SEND_FAIL})
    @Operation(summary = "임시 비밀번호 발급용 이메일 인증 및 임시 비밀번호 이메일 전송", description = "임시 비밀번호 이메일 인증 및 이메일 전송, 인증이 완료되면 알아서 이메일 전송")
    public ApiResponse<PasswordResetResponseDto> passwordVerify(@Valid @RequestBody PasswordResetRequestDto requestDto){
        PasswordResetResponseDto response = authService.resetPassword(requestDto);
        return ApiResponse.ok(response);
    }
}
