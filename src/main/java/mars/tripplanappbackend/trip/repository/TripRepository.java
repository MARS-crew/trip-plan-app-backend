package mars.tripplanappbackend.trip.repository;


import mars.tripplanappbackend.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByUser_UserIdAndIsDeletedFalse(Long userId);
}
