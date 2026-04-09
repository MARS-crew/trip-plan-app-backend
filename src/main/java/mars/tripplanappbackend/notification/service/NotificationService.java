package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.notification.domain.Notification;
import mars.tripplanappbackend.notification.dto.response.NotificationResponse;
import mars.tripplanappbackend.notification.enums.NotificationType;
import mars.tripplanappbackend.notification.repository.NotificationRepository;
import mars.tripplanappbackend.notification.repository.UserFcmTokenRepository;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final FcmService fcmService;
    private final MyPageRepository myPageRepository;

    /**
     * 알림 전송 공통 로직
     *
     * @param user 수신 대상 사용자
     * @param trip 연관된 여행 정보
     * @param tripSchedule 연관된 일정 정보
     * @param type 알림 타입
     * @param title 알림 제목
     * @param content 알림 내용
     */
    @Transactional
    public void sendNotification(User user, Trip trip, TripSchedule tripSchedule,
                                 NotificationType type, String title, String content) {

        if (user.getMarketingAgreed() == UseYnEnum.N) {
            log.info("알림 수신 미동의 유저 - 전송 중단: {}", user.getUsersId());
            return;
        }

        if (isNightTime() && user.getNightMarketingAgreed() == UseYnEnum.N) {
            log.info("야간 알림 미동의 유저 - 전송 중단: {}", user.getUsersId());
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .trip(trip)
                .tripSchedule(tripSchedule)
                .type(type)
                .title(title)
                .content(content)
                .isRead(UseYnEnum.N)
                .sendAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        notificationRepository.save(notification);

        sendFcm(user, trip, tripSchedule, type, title, content);
    }

    /**
     * FCM 전송 처리
     *
     * 사용자 토큰이 존재하는 경우에만 FCM 전송을 수행
     */
    private void sendFcm(User user, Trip trip, TripSchedule tripSchedule,
                         NotificationType type, String title, String content) {

        userFcmTokenRepository.findByUser(user).ifPresent(fcmToken -> {

            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());

            if (trip != null) {
                data.put("tripId", String.valueOf(trip.getTripId()));
            }
            if (tripSchedule != null) {
                data.put("tripScheduleId", String.valueOf(tripSchedule.getTripScheduleId()));
            }

            try {
                fcmService.sendPushNotification(fcmToken.getToken(), title, content, data);
                log.info("FCM 전송 요청 완료 - user: {}, type: {}", user.getUsersId(), type);

            } catch (Exception e) {
                log.error("FCM 전송 실패 (서비스는 계속 진행) - user: {}, error: {}",
                        user.getUsersId(), e.getMessage());
            }
        });
    }

    /**
     * 현재 시간이 야간(21:00 ~ 08:00)인지 확인
     */
    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(8, 0));
    }

    /**
     * 일정 알림
     */
    @Transactional
    public void sendScheduleNotification(User user, Trip trip, TripSchedule schedule,
                                         String title, String content) {
        sendNotification(user, trip, schedule, NotificationType.SCHEDULE, title, content);
    }

    /**
     * 날씨 알림
     */
    @Transactional
    public void sendWeatherNotification(User user, Trip trip,
                                        String title, String content) {
        sendNotification(user, trip, null, NotificationType.WEATHER, title, content);
    }

    /**
     * 알림 조회
     *
     * @param usersId JWT에서 추출한 사용자 ID
     * @return 알림 목록
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String usersId) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return notificationRepository.findByUserAndIsDeletedFalseOrderBySendAtDesc(user)
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }
}