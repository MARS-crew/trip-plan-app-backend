package mars.tripplanappbackend.notification.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.notification.enums.NotificationType;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.TripScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final TripScheduleRepository tripScheduleRepository;
    private final TripRepository tripRepository;
    private final WeatherService weatherService;

    /**
     * 일정 10분 전 알림
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendSchedule10MinBefore() {
        LocalTime targetTime = LocalTime.now().plusMinutes(10).withSecond(0).withNano(0);
        LocalDate targetDate = LocalDate.now();

        List<TripSchedule> schedules = tripScheduleRepository.findAllByScheduleDateAndStartTime(targetDate, targetTime);

        for (TripSchedule schedule : schedules) {
            String content = String.format("10분뒤 %s 일정입니다.", schedule.getTitle());

            notificationService.sendNotification(
                    schedule.getTrip().getUser(),
                    schedule.getTrip(),
                    schedule,
                    NotificationType.SCHEDULE,
                    "일정 안내",
                    content
            );
        }
    }

    /**
     * 오전 7시 날씨 알림
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailyWeatherNotification() {

        LocalDate today = LocalDate.now();
        List<Trip> todayTrips = tripRepository.findAllByStartDateAndIsDeletedFalse(today);

        for (Trip trip : todayTrips) {

            Optional<TripSchedule> optionalSchedule =
                    tripScheduleRepository.findTop1ByTripAndScheduleDateOrderByStartTime(trip, today);

            if (optionalSchedule.isEmpty()) continue;

            TripSchedule schedule = optionalSchedule.get();

            Place place = schedule.getPlace();
            if (place == null) continue;

            WeatherService.WeatherInfo weather =
                    weatherService.getWeather(place.getLatitude(), place.getLongitude());

            String content = String.format(
                    "오늘 %s의 날씨는 최저온도 %d도, 최고온도 %d도의 %s 날씨입니다.",
                    place.getCityName(),
                    weather.minTemp(),
                    weather.maxTemp(),
                    weather.weatherStatus()
            );

            notificationService.sendNotification(
                    trip.getUser(),
                    trip,
                    null,
                    NotificationType.WEATHER,
                    "날씨 안내",
                    content
            );
        }
    }
}