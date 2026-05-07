package mars.tripplanappbackend.mypage.domain;

import jakarta.persistence.*;
import lombok.*;
import mars.tripplanappbackend.auth.enums.WithdrawalReasonType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdraws")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
public class Withdraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", length = 40, nullable = false)
    private WithdrawalReasonType reasonType;

    @Column(name = "reason_text", length = 70)
    private String reasonText;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;
}


