package mars.tripplanappbackend.auth.controller;

import mars.tripplanappbackend.auth.dto.*;
import mars.tripplanappbackend.auth.service.AuthService;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.dto.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller", description = "auth 엔드포인트")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 api")
    public SignupResponseDto signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        return authService.signUp(requestDto);
    }
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 api")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = authService.login(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/reissue")
    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰 재발급 api")
    public ApiResponse<TokenReissueResponseDto> reissue(
            @RequestBody TokenReissueRequestDto request) {
        return ApiResponse.ok(authService.reissue(request));
    }
}
