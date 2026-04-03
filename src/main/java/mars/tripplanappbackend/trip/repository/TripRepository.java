package mars.tripplanappbackend.trip.repository;


import mars.tripplanappbackend.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByUser_UserIdAndIsDeletedFalse(Long userId);

    List<Trip> findAllByUser_UsersIdAndIsDeletedFalse(String usersId);

    Optional<Trip> findFirstByUser_UserIdAndIsDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
            Long userId,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    );

    Optional<Trip> findFirstByUser_UserIdAndIsDeletedFalseAndStartDateAfterOrderByStartDateAsc(
            Long userId,
            java.time.LocalDate startDate
    );

    long countByUser_UserIdAndIsDeletedFalse(Long userId);
}
