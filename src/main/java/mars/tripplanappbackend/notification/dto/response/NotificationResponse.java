package mars.tripplanappbackend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.notification.domain.Notification;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    @Schema(description = "알림 타이틀", example = "일정 안내")
    private String title;

    @Schema(description = "알림 내용", example = "10분 뒤 도톤보리 구경 일정입니다.")
    private String content;

    @Schema(description = "fcm에서 보낸 시간", example = "2026-04-09T01:41:01.447Z")
    private LocalDateTime sendAt;

    public NotificationResponse(Notification notification) {
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.sendAt = notification.getSendAt();
    }
}
