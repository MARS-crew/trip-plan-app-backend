package mars.tripplanappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }) //data-jpa 때문에 데이터베이스 에러 나서 exclude 처리함
public class TripPlanAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlanAppBackendApplication.class, args);
	}

}
