package mars.tripplanappbackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.request.FindIdRequestDto;
import mars.tripplanappbackend.auth.dto.request.LoginRequestDto;
import mars.tripplanappbackend.auth.dto.request.SignupRequestDto;
import mars.tripplanappbackend.auth.dto.request.TokenReissueRequestDto;
import mars.tripplanappbackend.auth.dto.response.*;
import mars.tripplanappbackend.auth.service.AuthService;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "auth 엔드포인트")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR, ErrorCode.DUPLICATE_USER})
    @Operation(summary = "회원가입", description = "회원가입 api")
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

    @GetMapping("/check-id")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT,  ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "아이디 중복 확인", description = "아이디 중복 확인 api")
    public ApiResponse<CheckIdResponseDto> checkId(@RequestParam String usersId) {
        CheckIdResponseDto response = authService.checkUsersIdDuplicate(usersId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/find-id")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "아이디 찾기", description = "아이디 찾기 api (이메일, 닉네임)")
    public ApiResponse<FindIdResponseDto> findId(@Valid @RequestBody FindIdRequestDto requestDto){
        FindIdResponseDto response = authService.findUsersId(requestDto);
        return ApiResponse.ok(response);
    }
}
