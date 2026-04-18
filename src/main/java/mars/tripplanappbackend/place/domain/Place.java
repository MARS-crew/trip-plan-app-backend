package mars.tripplanappbackend.place.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.place.enums.PlaceType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Column(name = "google_place_id", length = 120)
    private String googlePlaceId;

    @Column(name = "country_name", length = 70, nullable = false)
    private String countryName;

    @Column(name = "city_name", length = 90)
    private String cityName;

    @Column(name = "address", length = 255)
    private String address;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "rating_avg", nullable = false, precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "opening_hours", length = 255)
    private String openingHours;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "place_type", nullable = false,
            columnDefinition = "ENUM('ATTRACTION','RESTAURANT','BEACH','NATURE','LANDMARK','ACCOMMODATION','SHOPPING','CULTURE')")
    private PlaceType placeType = PlaceType.ATTRACTION;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    /**
     * Google Places 응답값으로 장소 핵심 정보를 갱신합니다.
     * 지도 검색 결과 재호출 시 최신 평점/주소/좌표를 반영하기 위해 사용합니다.
     */
    public void updateFromGoogle(
            String googlePlaceId,
            String name,
            String countryName,
            String cityName,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal ratingAvg,
            Integer reviewCount
    ) {
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.countryName = countryName;
        this.cityName = cityName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ratingAvg = ratingAvg;
        this.reviewCount = reviewCount;
    }
}
