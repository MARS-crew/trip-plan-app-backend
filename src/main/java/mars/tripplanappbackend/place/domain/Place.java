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
import java.math.RoundingMode;
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

    @Column(name = "google_rating_avg", precision = 2, scale = 1)
    private BigDecimal googleRatingAvg;

    @Column(name = "google_review_count")
    private Integer googleReviewCount;

    @Column(name = "opening_hours", length = 255)
    private String openingHours;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(
            name = "place_type",
            nullable = false,
            columnDefinition = "ENUM('ATTRACTION','RESTAURANT','BEACH','NATURE','LANDMARK','ACCOMMODATION','SHOPPING','CULTURE')"
    )
    private PlaceType placeType = PlaceType.ATTRACTION;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    /**
     * Google Places 동기화 결과를 기준으로 장소 핵심 필드를 갱신합니다.
     */
    public void updateFromGoogle(
            String googlePlaceId,
            String name,
            String countryName,
            String cityName,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            PlaceType placeType,
            String description,
            String openingHours,
            String imageUrl,
            BigDecimal googleRatingAvg,
            Integer googleReviewCount
    ) {
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.countryName = countryName;
        this.cityName = cityName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeType = PlaceType.normalizeForAppCategory(placeType);
        this.description = description;
        this.openingHours = openingHours;
        this.imageUrl = imageUrl;
        this.googleRatingAvg = googleRatingAvg;
        this.googleReviewCount = googleReviewCount;
        normalizeReviewStats();
    }

    /**
     * 검색 캐시 재조회 시 비어 있던 대표 이미지 URL만 별도로 보강합니다.
     *
     * @param imageUrl Google Place Photo API로 다시 조회한 대표 이미지 URL
     */
    public void updateImageUrlFromGoogle(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * 일정 위치/상세 조회에서 신뢰 가능한 Google Place ID를 확보했을 때
     * 핵심 식별 정보만 보강합니다.
     */
    public void backfillGoogleReference(
            String googlePlaceId,
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            String imageUrl
    ) {
        if (hasText(googlePlaceId)) {
            this.googlePlaceId = googlePlaceId.trim();
        }
        if (hasText(name)) {
            this.name = name.trim();
        }
        if (hasText(address)) {
            this.address = address.trim();
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (hasText(description)) {
            this.description = description.trim();
        }
        if (hasText(imageUrl)) {
            this.imageUrl = imageUrl.trim();
        }
    }

    /**
     * 신규 리뷰가 등록될 때 평균 평점과 리뷰 수를 재계산합니다.
     */
    public void updateRating(Integer newRating) {
        normalizeReviewStats();

        int newCount = this.reviewCount + 1;
        BigDecimal total = this.ratingAvg
                .multiply(BigDecimal.valueOf(this.reviewCount))
                .add(BigDecimal.valueOf(newRating));

        this.ratingAvg = total.divide(BigDecimal.valueOf(newCount), 1, RoundingMode.HALF_UP);
        this.reviewCount = newCount;
    }

    private void normalizeReviewStats() {
        if (this.reviewCount == null || this.reviewCount < 0) {
            this.reviewCount = 0;
        }
        if (this.reviewCount == 0 || this.ratingAvg == null) {
            this.ratingAvg = BigDecimal.ZERO;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
