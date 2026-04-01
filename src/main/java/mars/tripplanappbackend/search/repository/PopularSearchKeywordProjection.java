package mars.tripplanappbackend.search.repository;

/**
 * 인기 검색어 집계 결과를 조회하기 위한 projection 인터페이스입니다.
 */
public interface PopularSearchKeywordProjection {

    String getKeyword();

    Long getSearchCount();
}
