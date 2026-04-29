package mars.tripplanappbackend.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.request.EmailRequestDto;
import mars.tripplanappbackend.auth.dto.request.EmailVerifyRequestDto;
import mars.tripplanappbackend.auth.dto.response.EmailResponseDto;
import mars.tripplanappbackend.auth.dto.response.EmailVerifyResponseDto;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.mypage.dto.request.ExchangeRequestDto;
import mars.tripplanappbackend.mypage.dto.request.PapagoRequestDto;
import mars.tripplanappbackend.mypage.dto.request.UpdateAgreeRequestDto;
import mars.tripplanappbackend.mypage.dto.request.UpdateProfileRequestDto;
import mars.tripplanappbackend.mypage.dto.resopnse.*;
import mars.tripplanappbackend.mypage.service.ExchangeService;
import mars.tripplanappbackend.mypage.service.MyPageService;
import mars.tripplanappbackend.mypage.service.PapagoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 엔드포인트")
public class MyPageController {

    private final MyPageService myPageService;
    private final PapagoService papagoService;
    private final ExchangeService exchangeService;

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
    @ApiErrorExceptions({ErrorCode.FORBIDDEN, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<AgreeResponseDto> getNotificationSetting(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        AgreeResponseDto response = myPageService.getAgree(usersId);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/agree")
    @Operation(summary = "알림 설정 수정", description = "푸시 알림 설정 수정 api")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR, ErrorCode.INVALID_INPUT})
    public ApiResponse<AgreeResponseDto> updateNotificationSetting(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody UpdateAgreeRequestDto requestDto
    ) {
        String usersId = userPrincipal.getUsersId();
        AgreeResponseDto response = myPageService.updateAgree(usersId, requestDto);
        return ApiResponse.ok(response);
    }

    @GetMapping("/setting")
    @Operation(summary = "계정 설정 조회", description = "계정 설정 조회 api")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<SettingResponseDto> getSetting(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        SettingResponseDto response = myPageService.getSetting(usersId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회", description = "마이페이지 조회 api")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<MyPageResponseDto> getMyPage(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        MyPageResponseDto response = myPageService.getMyPage(usersId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/papago")
    @Operation(summary = "마이페이지 기본 어휘 번역", description = "기본 어휘 번역, en(영어), ja(일본어), zh-CN(중국어(간체)), zh-TW(중국어(번체)), vi(베트남어), " +
            "\n th(태국어), id(인도네시아어), fr(프랑스어), es(스페인어), ru(러시아어), de(독일어), it(이탈리아어) 로만 넣어야 됨")
    public ApiResponse<List<PapagoResponseDto>> translate(
            @CurrentUser UserPrincipal userPrincipal, @RequestBody PapagoRequestDto requestDto
    ) {
        List<PapagoResponseDto> response = papagoService.translatePhrases(
                userPrincipal.getUsersId(), requestDto.getTargetLang()
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/exchange")
    @Operation(summary = "환율 계산", description = "통화 코드와 금액을 입력하면 환율 계산 결과 반환 (fromKrw: true = KRW→현지통화, false = 현지통화→KRW)")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_INPUT,
            ErrorCode.EXCHANGE_RATE_FAILED, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<ExchangeResponseDto> getExchangeRate(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody ExchangeRequestDto requestDto
    ) {
        ExchangeResponseDto response = exchangeService.getExchangeRate(
                userPrincipal.getUsersId(), requestDto
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/visited")
    @Operation(summary = "내 방문 장소 목록 조회",
            description = "본인의 방문 장소 목록을 최신 방문순으로 조회")
    @ApiErrorExceptions({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_REFRESH_TOKEN, ErrorCode.INTERNAL_ERROR})
    public ApiResponse<List<VisitedPlaceResponseDto>> getMyVisitedPlaces(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        String usersId = userPrincipal.getUsersId();
        List<VisitedPlaceResponseDto> response = myPageService.getMyVisitedPlaces(usersId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/email-request")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.USER_NOT_FOUND, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "이메일 전송", description = "이메일 전송 api, gmail만 가능")
    public ApiResponse<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto requestDto) {
        EmailResponseDto response = myPageService.sendEmail(requestDto);
        return ApiResponse.ok(response);
    }

    @PostMapping("/email-verify")
    @ApiErrorExceptions({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_EMAIL_CODE,
            ErrorCode.EMAIL_CODE_EXPIRED, ErrorCode.INTERNAL_ERROR})
    @Operation(summary = "이메일 인증", description = "이메일 인증 api, 이메일로 전송된 인증 번호 6자리 입력, " +
            "해당 이메일로 가입된 유저가 없으면 user_not_found")
    public ApiResponse<EmailVerifyResponseDto> emailVerify(@Valid @RequestBody EmailVerifyRequestDto requestDto) {
        EmailVerifyResponseDto response = myPageService.verifyEmailCode(requestDto);
        return ApiResponse.ok(response);
    }
}