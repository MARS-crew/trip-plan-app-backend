package mars.tripplanappbackend.global.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class firebaseConfig {

        @Value("${spring.firebase.service-account.path}")
        private String SERVICE_ACCOUNT_PATH;


        @Bean
        public FirebaseApp firebaseApp() {
            try {
                if (!FirebaseApp.getApps().isEmpty()) {
                    return FirebaseApp.getInstance();
                }

                InputStream serviceAccount;

                if (SERVICE_ACCOUNT_PATH.startsWith("/") || SERVICE_ACCOUNT_PATH.startsWith("file:")) {
                    log.info("Loading Firebase key from FileSystem: {}", SERVICE_ACCOUNT_PATH);
                    serviceAccount = new FileSystemResource(SERVICE_ACCOUNT_PATH).getInputStream();
                } else {
                    log.info("Loading Firebase key from ClassPath: {}", SERVICE_ACCOUNT_PATH);
                    serviceAccount = new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream();
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();;

                log.info("Successfully initialized firebase app");
                return FirebaseApp.initializeApp(options);

            } catch (IOException e) {
                log.error("Fail to initialize firebase app: {}", e.getMessage());
                return null;
            }
        }

        @Bean
        public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
            return FirebaseMessaging.getInstance(firebaseApp);
        }
}
