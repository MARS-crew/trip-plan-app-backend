package mars.tripplanappbackend.search.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 검색 페이지에 고정 노출되는 카테고리 타입입니다.
 */
@Getter
@RequiredArgsConstructor
public enum SearchCategory {

    ACCOMMODATION("ACCOMMODATION", "숙박", 1),
    RESTAURANT("RESTAURANT", "음식점", 2),
    NATURE("NATURE", "자연", 3),
    BEACH("BEACH", "해변", 4),
    SHOPPING("SHOPPING", "쇼핑", 5),
    CULTURE("CULTURE", "문화", 6),
    LANDMARK("LANDMARK", "관광명소", 7);

    private final String code;
    private final String name;
    private final int sortOrder;
}
