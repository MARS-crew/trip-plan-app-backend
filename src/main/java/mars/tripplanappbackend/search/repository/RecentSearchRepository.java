package mars.tripplanappbackend.search.repository;

import mars.tripplanappbackend.search.domain.RecentSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

    @Query("""
            select r.keyword as keyword, sum(r.searchCount) as searchCount
            from RecentSearch r
            group by r.keyword
            order by sum(r.searchCount) desc, max(r.updatedAt) desc, r.keyword asc
            """)
    List<PopularSearchKeywordProjection> findPopularSearchKeywords(Pageable pageable);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalseOrderByUpdatedAtDesc(String usersId, Pageable pageable);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalseOrderByUpdatedAtDesc(String usersId);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalse(String usersId);

    List<RecentSearch> findAllByUser_UsersIdAndKeywordOrderByUpdatedAtDesc(String usersId, String keyword);

    Optional<RecentSearch> findByRecentSearchIdAndUser_UsersIdAndIsDeletedFalse(Long recentSearchId, String usersId);
}
