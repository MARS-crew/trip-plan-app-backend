package mars.tripplanappbackend.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.global.entity.BaseEntity;
import mars.tripplanappbackend.place.domain.Place;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "trip_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TripSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_schedule_id", nullable = false)
    private Long tripScheduleId;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Size(max = 10)
    @Column(name = "title", length = 10, nullable = false)
    private String title;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "memo", length = 100)
    private String memo;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = true)
    private Place place;

    /**
     * 여행 시작일이 변경되었을 때 일정의 일차 번호를 다시 계산해 반영합니다.
     *
     * @param dayNo 일정 날짜가 변경된 여행 기준으로 몇 일차인지 나타내는 값
     */
    public void updateDayNo(int dayNo) {
        this.dayNo = dayNo;
    }

    /**
     * 연결된 여행이 삭제되었을 때 일정도 함께 soft delete 상태로 전환합니다.
     * 여행 상세에서 더 이상 노출되면 안 되므로 삭제 여부와 삭제 시점을 같이 기록합니다.
     */
    public void markDeleted() {
        this.isDeleted = true;
        this.deletedDate = LocalDateTime.now();
    }
}
