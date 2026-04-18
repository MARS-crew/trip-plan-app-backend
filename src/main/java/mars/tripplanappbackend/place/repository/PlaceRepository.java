package mars.tripplanappbackend.place.repository;

import mars.tripplanappbackend.place.domain.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc(Pageable pageable);

    @Query("""
            select distinct p
            from Place p
            left join PlaceTagMap ptm on ptm.place = p and ptm.isDeleted = false
            left join PlaceTag pt on pt = ptm.placeTag and pt.isDeleted = false
            where p.isDeleted = false
              and (
                    lower(p.name) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.countryName, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.cityName, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.address, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(pt.tagName, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    List<Place> searchByKeyword(@Param("keyword") String keyword, Sort sort);

    Optional<Place> findByGooglePlaceIdAndIsDeletedFalse(String googlePlaceId);

    Optional<Place> findFirstByNameAndAddressAndIsDeletedFalse(String name, String address);

    List<Place> findAllByPlaceIdInAndIsDeletedFalse(List<Long> placeIds, Sort sort);

    Optional<Place> findByPlaceIdAndIsDeletedFalse(Long placeId);

    List<Place> findAllByCityNameAndPlaceIdNotAndIsDeletedFalse(String cityName, Long placeId);

    List<Place> findAllByCountryNameAndPlaceIdNotAndIsDeletedFalse(String countryName, Long placeId);

    List<Place> findAllByPlaceIdNotAndIsDeletedFalse(Long placeId);
}
