package mars.tripplanappbackend.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTestRequest {

    @Schema(description = "FCM registration token issued by the client app", example = "fcm-token-value")
    @NotBlank(message = "FCM token is required.")
    private String token;

    @Schema(description = "Push notification title", example = "FCM test")
    @NotBlank(message = "Title is required.")
    private String title;

    @Schema(description = "Push notification body", example = "This is a test push notification.")
    @NotBlank(message = "Body is required.")
    private String body;
}
