package mars.tripplanappbackend.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}