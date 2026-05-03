package mars.tripplanappbackend.search.repository;

import mars.tripplanappbackend.search.domain.SearchCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchCacheRepository extends JpaRepository<SearchCache, Long> {

    Optional<SearchCache> findByKeyword(String keyword);

    List<SearchCache> findAllByLastSearchedAtBefore(LocalDateTime cutoff);
}
