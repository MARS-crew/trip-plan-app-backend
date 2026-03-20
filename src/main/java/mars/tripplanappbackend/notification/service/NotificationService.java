package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
}
