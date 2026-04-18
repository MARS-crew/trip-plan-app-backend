package mars.tripplanappbackend.review.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.mypage.domain.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 500)
    @Column(name = "content", length = 500, nullable = false)
    private String content;

    @Column(name = "visited_date", nullable = false)
    private LocalDate visitedDate;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visited_place_id", nullable = false)
    private VisitedPlace visitedPlace;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();
}
