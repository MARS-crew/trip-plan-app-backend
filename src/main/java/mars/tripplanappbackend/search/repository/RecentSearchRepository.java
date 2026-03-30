package mars.tripplanappbackend.search.repository;

import mars.tripplanappbackend.search.domain.RecentSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

    List<RecentSearch> findAllByUser_UsersIdAndIsDeletedFalseOrderByCreatedAtDesc(String usersId, Pageable pageable);
}
