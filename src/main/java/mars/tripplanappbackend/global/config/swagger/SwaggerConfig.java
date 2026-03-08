package mars.tripplanappbackend.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import mars.tripplanappbackend.global.enums.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;
import java.util.List;

/**
 * swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";

        // 인증이 필요한 API 호출 시 'Authorize' 버튼을 통해
        // 전역적으로 토큰을 주입할 수 있도록 보안 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        // HTTP Bearer 인증 방식을 따르며, JWT 포맷을 명시하여 테스트 시 헤더에 자동 포함되게 함
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        // 로컬 개발 환경의 포트와 주소를 명시하여 Swagger Try it out 실행 시 실제 서버로 요청이 전달되게 함
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 테스트 서버");

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(localServer));
    }

    private Info apiInfo() {
        return new Info()
                .title("Trip-Plan-App Api")
                .description("Trip-Plan-App swagger")
                .version("1.0.0");
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorExceptions annotation = handlerMethod.getMethodAnnotation(ApiErrorExceptions.class);
            if (annotation != null) {
                generateErrorCodeResponse(operation, annotation.value());
            }
            return operation;
        };
    }

    private void generateErrorCodeResponse(Operation operation, ErrorCode[] errorCodes) {
        ApiResponses responses = operation.getResponses();

        for (ErrorCode errorCode : errorCodes) {
            String status = String.valueOf(errorCode.getStatus().value());

            // 1. 해당 상태 코드에 대한 응답 객체 가져오기 (없으면 생성)
            ApiResponse apiResponse = responses.computeIfAbsent(status, k -> new ApiResponse().description("에러 발생"));

            // 2. Content 객체 초기화 확인
            Content content = apiResponse.getContent();
            if (content == null) {
                content = new Content();
                apiResponse.setContent(content);
            }

            // 3. MediaType(application/json) 초기화 확인
            MediaType mediaType = content.getOrDefault("application/json", new MediaType());
            if (content.get("application/json") == null) {
                content.addMediaType("application/json", mediaType);
            }

            // 4. Example 생성 및 등록
            Example example = new Example();
            // 전역 응답 규격(mars.tripplanappbackend.global.dto.ApiResponse) 적용
            example.setValue(mars.tripplanappbackend.global.dto.ApiResponse.error(errorCode));

            // 드롭다운에 표시될 이름은 에러 코드(예: USER_NOT_FOUND)로 설정
            mediaType.addExamples(errorCode.name(), example);
        }
    }
}