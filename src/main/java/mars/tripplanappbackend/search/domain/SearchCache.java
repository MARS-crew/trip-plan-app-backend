package mars.tripplanappbackend.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 검색어 단위의 결과 캐시 메타데이터를 저장하는 엔티티입니다.
 * 동일한 검색어가 다시 들어오면 Google Places를 다시 호출하지 않고
 * 이 캐시를 기준으로 DB에 저장된 검색 결과를 재사용할 수 있습니다.
 */
@Entity
@Table(name = "search_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SearchCache extends BaseEntity {

    /**
     * 검색 캐시 PK입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_cache_id", nullable = false)
    private Long searchCacheId;

    /**
     * 정규화된 검색어입니다.
     * 공백과 대소문자를 정리한 값을 저장해 같은 의미의 검색을 하나로 묶습니다.
     */
    @Column(name = "keyword", length = 150, nullable = false, unique = true)
    private String keyword;

    /**
     * 이 검색어에 대해 캐시에 연결된 결과 개수입니다.
     * 실제 매핑 개수와 다르면 캐시 재생성이 필요하다고 판단하는 기준으로 사용됩니다.
     */
    @Builder.Default
    @Column(name = "result_count", nullable = false)
    private Integer resultCount = 0;

    /**
     * 사용자가 마지막으로 이 검색어를 조회한 시각입니다.
     * 오래된 검색 캐시를 주기적으로 정리할 때 기준값으로 사용합니다.
     */
    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    /**
     * 새로운 검색 캐시를 생성합니다.
     *
     * @param keyword 정규화된 검색어
     * @param resultCount 검색 결과 개수
     * @return 생성된 검색 캐시 엔티티
     */
    public static SearchCache create(String keyword, int resultCount) {
        return SearchCache.builder()
                .keyword(keyword)
                .resultCount(resultCount)
                .lastSearchedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 같은 검색어가 다시 조회되었음을 표시합니다.
     * 결과 자체는 유지하고 마지막 조회 시각만 최신으로 갱신합니다.
     */
    public void markSearched() {
        this.lastSearchedAt = LocalDateTime.now();
    }

    /**
     * 검색 결과를 새로 저장한 뒤 캐시 메타데이터를 갱신합니다.
     *
     * @param resultCount 새 검색 결과 개수
     */
    public void updateResults(int resultCount) {
        this.resultCount = resultCount;
        this.lastSearchedAt = LocalDateTime.now();
    }
}
