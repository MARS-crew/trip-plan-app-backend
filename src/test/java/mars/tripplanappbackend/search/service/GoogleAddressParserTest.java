package mars.tripplanappbackend.search.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleAddressParserTest {

    @Test
    @DisplayName("addressComponents가 있으면 컴포넌트 값을 우선 사용한다")
    void parseUsesAddressComponentsFirst() {
        List<GooglePlaceSearchService.GoogleAddressComponentCandidate> components = List.of(
                new GooglePlaceSearchService.GoogleAddressComponentCandidate(
                        "대한민국",
                        "KR",
                        List.of("country", "political")
                ),
                new GooglePlaceSearchService.GoogleAddressComponentCandidate(
                        "제주시",
                        null,
                        List.of("locality", "political")
                ),
                new GooglePlaceSearchService.GoogleAddressComponentCandidate(
                        "제주특별자치도",
                        null,
                        List.of("administrative_area_level_1", "political")
                )
        );

        GoogleAddressParser.ParsedAddress parsed = GoogleAddressParser.parse(
                "대한민국 제주특별자치도 제주시 첨단로 242",
                components
        );

        assertThat(parsed.countryName()).isEqualTo("대한민국");
        assertThat(parsed.cityName()).isEqualTo("제주시");
    }

    @Test
    @DisplayName("한국 주소 fallback: 대한민국 제주특별자치도 제주시")
    void parseKoreanJejuAddressFallback() {
        GoogleAddressParser.ParsedAddress parsed = GoogleAddressParser.parse(
                "대한민국 제주특별자치도 제주시 첨단로 242",
                List.of()
        );

        assertThat(parsed.countryName()).isEqualTo("대한민국");
        assertThat(parsed.cityName()).isEqualTo("제주시");
    }

    @Test
    @DisplayName("한국 주소 fallback: 서울특별시 강남구")
    void parseKoreanSeoulAddressFallback() {
        GoogleAddressParser.ParsedAddress parsed = GoogleAddressParser.parse(
                "서울특별시 강남구 테헤란로 123",
                List.of()
        );

        assertThat(parsed.countryName()).isEqualTo("대한민국");
        assertThat(parsed.cityName()).isEqualTo("서울특별시");
    }

    @Test
    @DisplayName("일반 주소 fallback: 1 Chome ..., Osaka, Japan")
    void parseGenericCommaAddressFallback() {
        GoogleAddressParser.ParsedAddress parsed = GoogleAddressParser.parse(
                "1 Chome 1-2 Otemae, Chuo Ward, Osaka, Japan",
                List.of()
        );

        assertThat(parsed.countryName()).isEqualTo("Japan");
        assertThat(parsed.cityName()).isEqualTo("Osaka");
    }
}
