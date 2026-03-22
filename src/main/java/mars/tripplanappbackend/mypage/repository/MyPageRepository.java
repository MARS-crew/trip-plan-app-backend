package mars.tripplanappbackend.mypage.repository;

import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MyPageRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsersId(String usersId);
    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByUsersId(String usersId);
}