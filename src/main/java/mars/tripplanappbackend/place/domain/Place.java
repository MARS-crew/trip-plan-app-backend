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

    // 리뷰 평점 계산 및 총 리뷰 갯수
    public void updateRating(Integer newRating) {
        // 첫 리뷰 달릴 때는 null 값일 수도 있는데, 이 null값으로 에러날까 봐 초기값 선언 
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.ratingAvg == null) this.ratingAvg = BigDecimal.ZERO;
        
        // 리뷰 새로 달릴 때마다 +1
        int newCount = this.reviewCount + 1;
        
        //리뷰 총점 계산
        BigDecimal total = this.ratingAvg
                .multiply(BigDecimal.valueOf(this.reviewCount))
                .add(BigDecimal.valueOf(newRating));
        this.ratingAvg = total.divide(BigDecimal.valueOf(newCount), 1, RoundingMode.HALF_UP);
        this.reviewCount = newCount;
    }
}
