package mars.tripplanappbackend.notification.repository;

import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.notification.domain.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByUser(User user);

    void deleteByToken(String token);
}
