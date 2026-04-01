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
            select r.keyword as keyword, count(r.recentSearchId) as searchCount
            from RecentSearch r
            where r.isDeleted = false
            group by r.keyword
            order by count(r.recentSearchId) desc, max(r.createdAt) desc, r.keyword asc
            """)
    List<PopularSearchKeywordProjection> findPopularSearchKeywords(Pageable pageable);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalseOrderByCreatedAtDesc(String usersId, Pageable pageable);

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalse(String usersId);

    Optional<RecentSearch> findByRecentSearchIdAndUser_UsersIdAndIsDeletedFalse(Long recentSearchId, String usersId);
}
