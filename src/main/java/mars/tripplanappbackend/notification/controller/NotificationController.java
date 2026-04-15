package mars.tripplanappbackend.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.config.auth.CurrentUser;
import mars.tripplanappbackend.global.config.auth.UserPrincipal;
import mars.tripplanappbackend.global.config.swagger.ApiErrorExceptions;
import mars.tripplanappbackend.global.dto.ApiResponse;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.notification.dto.request.FcmTokenRequest;
import mars.tripplanappbackend.notification.service.FcmTokenService;
import mars.tripplanappbackend.notification.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 엔드포인트")
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;

    @PostMapping("/token")
    @ApiErrorExceptions({ErrorCode.INVALID_INPUT, ErrorCode.INTERNAL_ERROR,
    ErrorCode.INVALID_FCM_TOKEN, ErrorCode.FCM_SEND_FAIL})
    @Operation(summary = "FCM 토큰 저장", description = "로그인 후 FCM 토큰 저장 api")
    public ApiResponse<Void> saveFcmToken(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        String usersId = userPrincipal.getUsersId();
        fcmTokenService.saveToken(usersId, request);
        return ApiResponse.ok(null);
    }
}
