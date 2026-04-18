package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.notification.domain.UserFcmToken;
import mars.tripplanappbackend.notification.dto.request.FcmTokenRequest;
import mars.tripplanappbackend.notification.repository.UserFcmTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final MyPageRepository myPageRepository;

    /**
     * 토큰 저장
     *
     * @param usersId JWT에서 추출한 사용자 ID
     * @param request FCM 토큰
     */
    @Transactional
    public void saveToken(String usersId, FcmTokenRequest request) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userFcmTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        fcmToken -> fcmToken.updateToken(request.getToken()),
                        () -> userFcmTokenRepository.save(
                                UserFcmToken.builder()
                                        .user(user)
                                        .token(request.getToken())
                                        .build()
                        )
                );
    }
}
