package mars.tripplanappbackend.search.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class GooglePlaceSearchServiceTest {

    @Test
    @DisplayName("Text Search FieldMask에서 평점, 리뷰, 가격 필드는 요청하지 않는다")
    void textSearchFieldMaskExcludesUnsupportedReviewAndPriceFields() throws Exception {
        String fieldMask = getStaticStringField("GOOGLE_PLACES_TEXT_SEARCH_FIELD_MASK");

        assertThat(fieldMask)
                .doesNotContain("places.rating")
                .doesNotContain("places.reviews")
                .doesNotContain("places.userRatingCount")
                .doesNotContain("places.priceLevel");
    }

    @Test
    @DisplayName("Place Details FieldMask에서 평점, 리뷰, 가격 필드는 요청하지 않는다")
    void detailsFieldMaskExcludesUnsupportedReviewAndPriceFields() throws Exception {
        String fieldMask = getStaticStringField("GOOGLE_PLACES_DETAILS_FIELD_MASK");

        assertThat(fieldMask)
                .doesNotContain("rating")
                .doesNotContain("reviews")
                .doesNotContain("userRatingCount")
                .doesNotContain("priceLevel");
    }

    private String getStaticStringField(String fieldName) throws Exception {
        Field field = GooglePlaceSearchService.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return (String) field.get(null);
    }
}
