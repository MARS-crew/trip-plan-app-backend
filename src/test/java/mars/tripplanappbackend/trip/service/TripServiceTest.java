package mars.tripplanappbackend.trip.service;

import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.enums.TripStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TripServiceTest {

    private final TripService tripService = new TripService(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );

    @Test
    @DisplayName("예정 여행의 진행률을 등록일부터 여행 시작일까지 기준으로 계산한다")
    void calculateNearbyTripProgressRateForPlannedTrip() throws Exception {
        Trip trip = Trip.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 28))
                .build();

        Integer progressRate = invokeCalculateNearbyTripProgressRate(
                trip,
                TripStatus.PLANNED,
                LocalDate.of(2026, 3, 18),
                LocalDate.of(2026, 3, 9)
        );

        assertThat(progressRate).isEqualTo(64);
    }

    @Test
    @DisplayName("등록일과 여행 시작일이 같아도 진행률 계산에서 0으로 나누지 않는다")
    void calculateNearbyTripProgressRateAvoidsDivideByZero() throws Exception {
        Trip trip = Trip.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 23))
                .build();

        Integer progressRate = invokeCalculateNearbyTripProgressRate(
                trip,
                TripStatus.PLANNED,
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 23)
        );

        assertThat(progressRate).isEqualTo(0);
    }

    @Test
    @DisplayName("예정 여행이 아니면 진행률을 내려주지 않는다")
    void calculateNearbyTripProgressRateReturnsNullForNonPlannedTrip() throws Exception {
        Trip trip = Trip.builder()
                .startDate(LocalDate.of(2026, 3, 23))
                .endDate(LocalDate.of(2026, 3, 28))
                .build();

        Integer progressRate = invokeCalculateNearbyTripProgressRate(
                trip,
                TripStatus.ONGOING,
                LocalDate.of(2026, 3, 24),
                LocalDate.of(2026, 3, 9)
        );

        assertThat(progressRate).isNull();
    }

    private Integer invokeCalculateNearbyTripProgressRate(
            Trip trip,
            TripStatus tripStatus,
            LocalDate today,
            LocalDate registeredAt
    ) throws Exception {
        Method method = TripService.class.getDeclaredMethod(
                "calculateNearbyTripProgressRate",
                Trip.class,
                TripStatus.class,
                LocalDate.class,
                LocalDate.class
        );
        method.setAccessible(true);

        return (Integer) method.invoke(tripService, trip, tripStatus, today, registeredAt);
    }
}
