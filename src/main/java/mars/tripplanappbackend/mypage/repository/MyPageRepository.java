package mars.tripplanappbackend.mypage.repository;

import aj.org.objectweb.asm.commons.Remapper;
import jakarta.validation.constraints.NotBlank;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MyPageRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsersId(String usersId);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByLoginTypeAndSocialProviderId(LoginType loginType, String socialProviderId);

    boolean existsByUsersId(String usersId);

    Optional<User> findByNicknameAndEmail(String email, String nickname);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsersIdAndEmail(String email, String UsersId);

    boolean existsByEmail(String Email);
}

