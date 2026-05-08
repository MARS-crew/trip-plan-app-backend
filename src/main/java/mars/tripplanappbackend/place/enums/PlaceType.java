package mars.tripplanappbackend.place.enums;

public enum PlaceType {
    ATTRACTION,
    RESTAURANT,
    BEACH,
    NATURE,
    LANDMARK,
    ACCOMMODATION,
    SHOPPING,
    CULTURE;

    public PlaceType toAppCategory() {
        return switch (this) {
            case BEACH -> NATURE;
            case LANDMARK -> ATTRACTION;
            default -> this;
        };
    }

    public static PlaceType normalizeForAppCategory(PlaceType placeType) {
        if (placeType == null) {
            return ATTRACTION;
        }
        return placeType.toAppCategory();
    }
}
