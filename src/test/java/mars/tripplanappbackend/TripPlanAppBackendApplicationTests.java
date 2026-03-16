package mars.tripplanappbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import mars.tripplanappbackend.global.config.DotenvTestInitializer;

@SpringBootTest
@ContextConfiguration(initializers = DotenvTestInitializer.class)
class TripPlanAppBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
