package mars.tripplanappbackend.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.trip.enums.TripStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "share_code", length = 100)
    private String shareCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "trip_status", nullable = false, columnDefinition = "ENUM('PLANNED','ONGOING','COMPLETED')")
    private TripStatus tripStatus = TripStatus.PLANNED;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    /**
     * 내 여행 상세 화면에서 수정한 기본 여행 정보를 현재 엔티티에 반영합니다.
     * 여행 추가 화면을 재사용하는 수정 흐름을 기준으로 제목, 이미지, 기간, 상태를 함께 갱신합니다.
     *
     * @param title 수정할 여행 제목
     * @param startDate 수정할 여행 시작일
     * @param endDate 수정할 여행 종료일
     * @param imageUrl 수정할 대표 이미지 URL
     * @param tripStatus 수정된 기간 기준으로 다시 계산한 여행 상태
     */
    public void updateTrip(String title, LocalDate startDate, LocalDate endDate, String imageUrl, TripStatus tripStatus) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.tripStatus = tripStatus;
    }

    /**
     * 여행 삭제 요청이 들어오면 여행 카드를 soft delete 상태로 전환합니다.
     * 상세 화면에서 삭제된 여행은 목록과 상세 조회에서 제외되어야 하므로 삭제 여부와 삭제 시점을 함께 기록합니다.
     */
    public void markDeleted() {
        this.isDeleted = true;
        this.deletedDate = LocalDateTime.now();
    }

    /**
     * 공유 링크 생성 시 확정된 공유 코드를 현재 여행 엔티티에 반영합니다.
     *
     * @param shareCode 현재 여행에 저장할 공유 코드
     */
    public void updateShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    /**
     * 여행 날짜 수정 메서드
     */
    public void updateTripDate(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 여행 제목을 수정합니다.
     *
     * 사용자가 여행 상세 화면에서 제목을 변경할 때 호출되며,
     * 기존 제목을 새로운 값으로 갱신합니다.
     *
     * @param title 변경할 여행 제목
     */
    public void updateTitle(String title) {
        this.title = title;
    }
}
