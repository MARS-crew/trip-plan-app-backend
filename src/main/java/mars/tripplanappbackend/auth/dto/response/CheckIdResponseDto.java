package mars.tripplanappbackend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckIdResponseDto {
    private final String usersId;
    private final String message;

    public static CheckIdResponseDto of(String usersId) {
        return new CheckIdResponseDto(usersId, "사용 가능한 아이디입니다.");
    }
}
