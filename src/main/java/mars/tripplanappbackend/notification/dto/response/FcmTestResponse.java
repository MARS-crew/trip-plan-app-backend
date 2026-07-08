package mars.tripplanappbackend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTestResponse {

    @Schema(description = "Firebase message id returned after a successful send")
    private String messageId;

    @Schema(description = "Push notification title")
    private String title;

    @Schema(description = "Push notification body")
    private String body;
}
