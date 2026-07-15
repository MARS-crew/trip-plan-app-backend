package mars.tripplanappbackend.search.service;

import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GooglePlaceSearchServiceTest {

    @Test
    @DisplayName("Places API 키가 없으면 내부 오류가 아닌 공급자 사용 불가 오류를 반환한다")
    void missingApiKeyReturnsProviderUnavailable() {
        GooglePlaceSearchService service = new GooglePlaceSearchService();

        assertThatThrownBy(() -> service.searchPlaces("해변", 20))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GOOGLE_PLACES_UNAVAILABLE);
    }

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
