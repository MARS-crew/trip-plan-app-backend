package mars.tripplanappbackend.place.enums;

/**
 * 저장한 장소 목록 화면과 바텀시트 저장 탭에서 사용하는 필터 유형입니다.
 * ALL은 전체 저장 장소를 의미하고, 나머지는 장소의 유형(placeType)에 맞춰 필터링합니다.
 */
public enum SavedPlaceFilterType {
    ALL(null),
    ATTRACTION(PlaceType.ATTRACTION),
    RESTAURANT(PlaceType.RESTAURANT),
    BEACH(PlaceType.BEACH),
    NATURE(PlaceType.NATURE),
    LANDMARK(PlaceType.LANDMARK),
    ACCOMMODATION(PlaceType.ACCOMMODATION),
    SHOPPING(PlaceType.SHOPPING),
    CULTURE(PlaceType.CULTURE);

    private final PlaceType placeType;

    SavedPlaceFilterType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }
}
