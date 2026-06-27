package mars.tripplanappbackend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.notification.domain.Notification;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    @Schema(description = "알림 제목", example = "일정 안내")
    private String title;

    @Schema(description = "알림 내용", example = "10분뒤 도톤보리 구경 일정입니다.")
    private String content;

    @Schema(description = "날씨 상태값 (맑음: 1, 비: 2, 흐림: 3, 눈: 4)", example = "1")
    private Integer weatherStatusCode;

    @Schema(description = "알림 전송 시간", example = "2026-04-09T01:41:01.447Z")
    private LocalDateTime sendAt;

    @Schema(description = "읽음 여부", example = "Y")
    private UseYnEnum isRead;

    @Schema(description = "알림 조회 시간", example = "2026-04-09T01:41:01.447Z")
    private LocalDateTime readAt;

    public NotificationResponse(Notification notification) {
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.weatherStatusCode = notification.getWeatherStatusCode();
        this.sendAt = notification.getSendAt();
        this.isRead = notification.getIsRead();
        this.readAt = notification.getReadAt();
    }
}
