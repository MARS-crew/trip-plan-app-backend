package mars.tripplanappbackend.notification.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherStatusCodeTest {

    @Test
    void openWeatherMainToCodeIsMapped() {
        assertEquals(WeatherStatusCode.CLEAR, WeatherStatusCode.fromOpenWeatherMain("Clear"));
        assertEquals(WeatherStatusCode.RAIN, WeatherStatusCode.fromOpenWeatherMain("Rain"));
        assertEquals(WeatherStatusCode.RAIN, WeatherStatusCode.fromOpenWeatherMain("Drizzle"));
        assertEquals(WeatherStatusCode.RAIN, WeatherStatusCode.fromOpenWeatherMain("Thunderstorm"));
        assertEquals(WeatherStatusCode.SNOW, WeatherStatusCode.fromOpenWeatherMain("Snow"));
        assertEquals(WeatherStatusCode.CLOUDY, WeatherStatusCode.fromOpenWeatherMain("Clouds"));
    }
}
