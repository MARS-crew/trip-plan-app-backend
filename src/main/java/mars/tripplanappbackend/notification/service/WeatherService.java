package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create("https://api.openweathermap.org");

    /**
     * 위도/경도를 기반으로 날씨 정보 조회
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return WeatherInfo (최저/최고 온도, 날씨 상태)
     *
     * 외부 API(OpenWeather) 호출 실패 시 기본값을 반환
     */
    public WeatherInfo getWeather(BigDecimal latitude, BigDecimal longitude) {
        try {
            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/3.0/onecall")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("exclude", "minutely,hourly,alerts")
                            .queryParam("units", "metric")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .blockOptional()
                    .orElseThrow(() -> new RuntimeException("날씨 API 응답 없음"));

            Map<String, Object> daily = (Map<String, Object>) ((List<?>) response.get("daily")).get(0);
            Map<String, Object> temp = (Map<String, Object>) daily.get("temp");
            List<Map<String, Object>> weather = (List<Map<String, Object>>) daily.get("weather");

            double minTemp = ((Number) temp.get("min")).doubleValue();
            double maxTemp = ((Number) temp.get("max")).doubleValue();
            String weatherMain = (String) weather.get(0).get("main");

            return new WeatherInfo(
                    (int) Math.round(minTemp),
                    (int) Math.round(maxTemp),
                    convertWeatherStatus(weatherMain)
            );

        } catch (Exception e) {
            log.error("날씨 API 호출 실패: {}", e.getMessage());
            return new WeatherInfo(0, 0, "알 수 없음");
        }
    }

    /**
     * OpenWeather 날씨 상태를 한국어로 변환
     */
    private String convertWeatherStatus(String weatherMain) {
        return switch (weatherMain) {
            case "Clear" -> "맑음";
            case "Rain", "Drizzle", "Thunderstorm" -> "비옴";
            case "Snow" -> "눈";
            default -> "흐림";
        };
    }

    public record WeatherInfo(int minTemp, int maxTemp, String weatherStatus) {}
}
