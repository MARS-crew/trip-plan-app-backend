package mars.tripplanappbackend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 업로드한 정적 파일을 브라우저에서 바로 조회할 수 있도록 리소스 핸들러를 등록합니다.
 */
@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.root-dir:uploads}")
    private String uploadRootDir;

    /**
     * 로컬 업로드 디렉터리를 "/uploads/**" URL 경로로 연결합니다.
     *
     * @param registry 스프링 MVC 리소스 핸들러 등록기
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirectory = Paths.get(uploadRootDir).toAbsolutePath().normalize();
        String resourceLocation = uploadDirectory.toUri().toString();

        log.info("Registering upload resource handler: {}", resourceLocation);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
