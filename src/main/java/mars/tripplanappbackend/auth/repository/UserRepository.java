package mars.tripplanappbackend.auth.repository;

import mars.tripplanappbackend.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsersId(String usersId);
    Optional<UserEntity> findByRefreshToken(String refreshToken);

}