package mars.tripplanappbackend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import mars.tripplanappbackend.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "공통 응답 규격")
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "실제 데이터")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @Schema(description = "에러 정보 (실패 시에만 포함)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Error error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new Error(errorCode.getCode(), errorCode.getMessage()))
                .build();
    }

    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new Error(code, message))
                .build();
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "에러 상세 정보")
    public static class Error {
        @Schema(description = "에러 코드", example = "USER_NOT_FOUND")
        private String code;

        @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다")
        private String message;
    }
}