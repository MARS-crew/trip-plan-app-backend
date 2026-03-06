package mars.tripplanappbackend.auth.entity;

import mars.tripplanappbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import mars.tripplanappbackend.global.enums.Gender;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static mars.tripplanappbackend.global.enums.UseYnEnum.N;

@Entity
@Table(name= "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(length = 15, nullable = false, unique = true, name="users_id")
    private String usersId;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    public void updateRefreshToken(String token, LocalDateTime expiresAt) {
        this.refreshToken = token;
        this.refreshTokenExpiresAt = expiresAt;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name= "privacy_agreed")
    private UseYnEnum privacyAgreed = N;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name= "marketing_agreed")
    private UseYnEnum marketingAgreed = N;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name= "night_marketing_agreed")
    private UseYnEnum nightMarketingAgreed = N;

    public boolean isRefreshTokenExpired() {
        if (this.refreshTokenExpiresAt == null) {
            return true;
        }
        return this.refreshTokenExpiresAt.isBefore(LocalDateTime.now());
    }
}