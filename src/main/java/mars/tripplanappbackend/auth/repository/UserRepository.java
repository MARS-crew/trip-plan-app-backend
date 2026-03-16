package mars.tripplanappbackend.auth.repository;

import mars.tripplanappbackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsersId(String usersId);
    Optional<User> findByRefreshToken(String refreshToken);
}