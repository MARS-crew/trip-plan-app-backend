package mars.tripplanappbackend.search.repository;

import mars.tripplanappbackend.search.domain.SearchCache;
import mars.tripplanappbackend.search.domain.SearchCachePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchCachePlaceRepository extends JpaRepository<SearchCachePlace, Long> {

    List<SearchCachePlace> findAllBySearchCache_SearchCacheIdOrderBySortOrderAsc(Long searchCacheId);

    void deleteAllBySearchCache(SearchCache searchCache);
}
