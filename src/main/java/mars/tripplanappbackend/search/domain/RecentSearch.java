package mars.tripplanappbackend.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_search")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RecentSearch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_search_id", nullable = false)
    private Long recentSearchId;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 사용자와 검색어를 기반으로 최근 검색어 엔티티를 생성합니다.
     *
     * @param user 최근 검색어를 저장할 사용자
     * @param keyword 저장할 검색어
     * @return 최근 검색어 엔티티
     */
    public static RecentSearch create(User user, String keyword) {
        return RecentSearch.builder()
                .user(user)
                .keyword(keyword)
                .build();
    }

    /**
     * 최근 검색어를 삭제 상태로 변경합니다.
     */
    public void markDeleted() {
        this.isDeleted = true;
        this.deletedDate = LocalDateTime.now();
    }
}
