package mars.tripplanappbackend.search.repository;

import mars.tripplanappbackend.search.domain.RecentSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalseOrderByCreatedAtDesc(String usersId, Pageable pageable);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalse(String usersId);

    Optional<RecentSearch> findByRecentSearchIdAndUser_UsersIdAndIsDeletedFalse(Long recentSearchId, String usersId);
}
