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

    @Transactional
    public void sendNotification(User user, Trip trip, TripSchedule tripSchedule,
                                 NotificationType type, String title, String content) {
        sendNotification(user, trip, tripSchedule, type, title, content, null);
    }

    @Transactional
    public void sendNotification(User user, Trip trip, TripSchedule tripSchedule,
                                 NotificationType type, String title, String content,
                                 Integer weatherStatusCode) {

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
                .weatherStatusCode(weatherStatusCode)
                .isRead(UseYnEnum.N)
                .sendAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        notificationRepository.save(notification);
    }

    public void sendFcm(User user, Trip trip, TripSchedule tripSchedule,
                        NotificationType type, String title, String content) {
        sendFcm(user, trip, tripSchedule, type, title, content, null);
    }

    public void sendFcm(User user, Trip trip, TripSchedule tripSchedule,
                        NotificationType type, String title, String content,
                        Integer weatherStatusCode) {

        userFcmTokenRepository.findByUser(user).ifPresent(fcmToken -> {
            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());

            if (trip != null) {
                data.put("tripId", String.valueOf(trip.getTripId()));
            }
            if (tripSchedule != null) {
                data.put("tripScheduleId", String.valueOf(tripSchedule.getTripScheduleId()));
            }
            if (weatherStatusCode != null) {
                data.put("weatherStatusCode", String.valueOf(weatherStatusCode));
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

    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(8, 0));
    }

    @Transactional
    public void sendScheduleNotification(User user, Trip trip, TripSchedule schedule,
                                         String title, String content) {
        sendNotification(user, trip, schedule, NotificationType.SCHEDULE, title, content);
    }

    @Transactional
    public void sendWeatherNotification(User user, Trip trip,
                                        String title, String content) {
        sendWeatherNotification(user, trip, title, content, null);
    }

    @Transactional
    public void sendWeatherNotification(User user, Trip trip,
                                        String title, String content,
                                        Integer weatherStatusCode) {
        sendNotification(user, trip, null, NotificationType.WEATHER, title, content, weatherStatusCode);
    }

    @Transactional
    public List<NotificationResponse> getNotifications(String usersId) {
        User user = myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notifications = notificationRepository
                .findByUserAndIsDeletedFalseOrderBySendAtDesc(user);

        notifications.forEach(Notification::markAsRead);

        return notifications.stream()
                .map(NotificationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasUnreadNotification(String usersId) {
        User user = myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return !notificationRepository.existsByUserAndIsReadAndIsDeletedFalse(user, UseYnEnum.N);
    }
}
