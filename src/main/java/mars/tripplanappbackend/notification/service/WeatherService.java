package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.notification.enums.WeatherStatusCode;
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
            WeatherStatusCode weatherStatusCode = WeatherStatusCode.fromOpenWeatherMain(weatherMain);

            return new WeatherInfo(
                    (int) Math.round(minTemp),
                    (int) Math.round(maxTemp),
                    weatherStatusCode.getLabel(),
                    weatherStatusCode.getCode()
            );

        } catch (Exception e) {
            log.error("날씨 API 호출 실패: {}", e.getMessage());
            return new WeatherInfo(0, 0, "알 수 없음", null);
        }
    }

    public record WeatherInfo(int minTemp, int maxTemp, String weatherStatus, Integer weatherStatusCode) {}
}
