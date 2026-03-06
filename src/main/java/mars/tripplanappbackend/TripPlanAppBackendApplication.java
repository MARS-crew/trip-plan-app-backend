package mars.tripplanappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TripPlanAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlanAppBackendApplication.class, args);
	}

}
