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
    Optional<User> findByUsersIdAndIsDeletedFalse(String usersId);
    Optional<User> findByRefreshTokenAndIsDeletedFalse(String refreshToken);
    Optional<User> findByLoginTypeAndSocialProviderIdAndIsDeletedFalse(LoginType loginType, String socialProviderId);
    Optional<User> findByNicknameAndEmailAndIsDeletedFalse(String nickname, String email);
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByUsersIdAndEmailAndIsDeletedFalse(String usersId, String email);

    boolean existsByUsersIdAndIsDeletedFalse(String usersId);

    boolean existsByEmailAndIsDeletedFalse(String email);
}

