package mars.tripplanappbackend.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherStatusCode {
    CLEAR(1, "맑음"),
    RAIN(2, "비"),
    CLOUDY(3, "흐림"),
    SNOW(4, "눈");

    private final int code;
    private final String label;

    public static WeatherStatusCode fromOpenWeatherMain(String weatherMain) {
        return switch (weatherMain) {
            case "Clear" -> CLEAR;
            case "Rain", "Drizzle", "Thunderstorm" -> RAIN;
            case "Snow" -> SNOW;
            default -> CLOUDY;
        };
    }
}
