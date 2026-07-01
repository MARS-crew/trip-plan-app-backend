package mars.tripplanappbackend.global.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationNameLocalizationServiceTest {

    @Test
    @DisplayName("manual city localization overrides are applied without external translate calls")
    void localizeCityNameToKoreanAppliesManualOverride() {
        LocationNameLocalizationService service = new LocationNameLocalizationService();

        assertThat(service.localizeCityNameToKorean("Minato City")).isEqualTo("미나토구");
    }
}
