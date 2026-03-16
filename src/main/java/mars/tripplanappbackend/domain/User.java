package mars.tripplanappbackend.domain;

import mars.tripplanappbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import mars.tripplanappbackend.global.enums.Gender;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.user.enums.LoginType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import mars.tripplanappbackend.global.enums.Role;
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
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(length = 15, nullable = false, unique = true, name = "users_id")
    private String usersId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "birth_day", nullable = false)
    private LocalDate birth;

    @Column(name = "country_code", length = 20)
    private String countryCode;

    @Column(name = "social_provider_id", length = 90)
    private String socialProviderId;

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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name = "login_type", columnDefinition = "ENUM('LOCAL','GOOGLE','KAKAO','NAVER')")
    private LoginType loginType = LoginType.LOCAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name = "email_verified", columnDefinition = "ENUM('Y','N')")
    private UseYnEnum emailVerified = N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name= "agree_privacy", columnDefinition = "ENUM('Y','N')")
    private UseYnEnum privacyAgreed = N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name= "agree_marketing", columnDefinition = "ENUM('Y','N')")
    private UseYnEnum marketingAgreed = N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name= "agree_night_marketing", columnDefinition = "ENUM('Y','N')")
    private UseYnEnum nightMarketingAgreed = N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, name = "is_withdrawn", columnDefinition = "ENUM('Y','N')")
    private UseYnEnum withdrawn = N;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "withdrawal_reason_type", length = 40)
    private String withdrawalReasonType;

    @Column(name = "withdrawal_reason_text", length = 70)
    private String withdrawalReasonText;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    public boolean isRefreshTokenExpired() {
        if (this.refreshTokenExpiresAt == null) {
            return true;
        }
        return this.refreshTokenExpiresAt.isBefore(LocalDateTime.now());
    }
}