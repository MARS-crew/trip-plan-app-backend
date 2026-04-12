package mars.tripplanappbackend.place.enums;

import lombok.Getter;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;

import java.util.Arrays;

/**
 * 저장한 장소 화면에서 사용하는 카테고리 필터 유형입니다.
 * 프론트 화면에서 전달하는 한글 라벨과 API에서 사용하는 영문 enum 값을 모두 허용합니다.
 */
@Getter
public enum SavedPlaceFilterType {
    ALL(null, "전체"),
    ATTRACTION(PlaceType.ATTRACTION, "관광지"),
    RESTAURANT(PlaceType.RESTAURANT, "맛집"),
    BEACH(PlaceType.BEACH, "해변"),
    NATURE(PlaceType.NATURE, "자연"),
    LANDMARK(PlaceType.LANDMARK, "랜드마크"),
    ACCOMMODATION(PlaceType.ACCOMMODATION, "숙소"),
    SHOPPING(PlaceType.SHOPPING, "쇼핑"),
    CULTURE(PlaceType.CULTURE, "문화");

    private final PlaceType placeType;
    private final String displayName;

    SavedPlaceFilterType(PlaceType placeType, String displayName) {
        this.placeType = placeType;
        this.displayName = displayName;
    }

    /**
     * 화면 카테고리 라벨 또는 enum 문자열을 저장한 장소 필터 enum으로 변환합니다.
     *
     * @param value 화면 또는 API에서 전달한 필터 값
     * @return 저장한 장소 필터 enum
     */
    public static SavedPlaceFilterType from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        return Arrays.stream(values())
                .filter(filterType -> filterType.matches(value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
    }

    private boolean matches(String value) {
        return name().equalsIgnoreCase(value) || displayName.equals(value);
    }
}
