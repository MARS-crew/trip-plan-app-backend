package mars.tripplanappbackend.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_fcm_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id", nullable = false)
    private Long fcmTokenId;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public void updateToken(String token) {
        this.token = token;
    }
}
