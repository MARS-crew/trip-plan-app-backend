package mars.tripplanappbackend.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import mars.tripplanappbackend.place.domain.Place;

/**
 * 하나의 검색 캐시에 어떤 장소들이 어떤 순서로 포함되는지 저장하는 매핑 엔티티입니다.
 * 검색 결과 카드 순서를 그대로 재현하기 위해 sortOrder를 함께 관리합니다.
 */
@Entity
@Table(name = "search_cache_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SearchCachePlace {

    /**
     * 검색 캐시-장소 매핑 PK입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_cache_place_id", nullable = false)
    private Long searchCachePlaceId;

    /**
     * 이 매핑이 속한 검색 캐시입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_cache_id", nullable = false)
    private SearchCache searchCache;

    /**
     * 검색 결과에 포함된 장소 엔티티입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    /**
     * 검색 결과 목록에서의 정렬 순서입니다.
     * 0부터 시작하며, 화면 응답 순서를 유지하는 데 사용합니다.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 검색 캐시와 장소를 연결하는 매핑 엔티티를 생성합니다.
     *
     * @param searchCache 대상 검색 캐시
     * @param place 결과에 포함할 장소
     * @param sortOrder 검색 결과 순서
     * @return 생성된 매핑 엔티티
     */
    public static SearchCachePlace create(SearchCache searchCache, Place place, int sortOrder) {
        return SearchCachePlace.builder()
                .searchCache(searchCache)
                .place(place)
                .sortOrder(sortOrder)
                .build();
    }
}
