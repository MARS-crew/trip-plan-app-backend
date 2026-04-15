package mars.tripplanappbackend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.enums.UseYnEnum;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnreadNotificationResponse {
    @Schema(description = "안 읽은 알림 존재 여부", example = "false")
    private boolean unread;
}
