package mars.tripplanappbackend.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.notification.repository.UserFcmTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {
    private final FirebaseMessaging firebaseMessaging;
    private final UserFcmTokenRepository userFcmTokenRepository;

    /**
     * fcm 알림 전송
     *
     * 토큰에 대한 예외 처리 - 유효하지 않은 토큰은 DB에서 삭제
     * @param token FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (앱 내부 처리용)
     */
    public void sendPushNotification(String token, String title, String body, Map<String, String> data) {

        Message message = buildMessage(token, title, body, data);

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM 전송 성공 - token: {}, response: {}", token, response);

        } catch (FirebaseMessagingException e) {
            handleFcmError(e, token);
        }
    }

    /**
     * FCM 메시지 생성
     *
     * @param token FCM 디바이스 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터
     * @return Firebase 전송용 Message 객체
     */
    private Message buildMessage(String token, String title, String body, Map<String, String> data) {

        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }

    /**
     * FCM 에러 처리 (핵심)
     *
     * @param e FirebaseMessagingException
     * @param token FCM 디바이스 토큰
     *
     * 유효하지 않은 토큰(UNREGISTERED, INVALID_ARGUMENT)은 DB에서 삭제
     */
    private void handleFcmError(FirebaseMessagingException e, String token) {

        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        if ((errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT)) {

            log.warn("유효하지 않은 FCM 토큰 - 삭제 처리: {}", token);

            userFcmTokenRepository.deleteByToken(token);
            return;
        }

        log.error("FCM 전송 실패 - code: {}, message: {}", errorCode, e.getMessage());
    }
}