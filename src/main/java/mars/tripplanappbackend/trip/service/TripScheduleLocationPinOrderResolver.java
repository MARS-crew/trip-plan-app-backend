package mars.tripplanappbackend.trip.service;

import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TripScheduleLocationPinOrderResolver {

    private TripScheduleLocationPinOrderResolver() {
    }

    static List<Integer> resolve(List<TripSchedule> tripSchedules) {
        List<Integer> pinOrders = new ArrayList<>(tripSchedules.size());
        LocalDate currentScheduleDate = null;
        int pinOrder = 0;

        for (TripSchedule tripSchedule : tripSchedules) {
            if (!Objects.equals(currentScheduleDate, tripSchedule.getScheduleDate())) {
                currentScheduleDate = tripSchedule.getScheduleDate();
                pinOrder = 0;
            }

            if (hasLocation(tripSchedule)) {
                pinOrders.add(++pinOrder);
                continue;
            }

            pinOrders.add(null);
        }

        return pinOrders;
    }

    private static boolean hasLocation(TripSchedule tripSchedule) {
        return tripSchedule.getPlace() != null
                && tripSchedule.getPlace().getLatitude() != null
                && tripSchedule.getPlace().getLongitude() != null;
    }
}
