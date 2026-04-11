package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.trip.domain.Trip;

import java.time.LocalDate;

/**
 * 내 여행 상세 화면 공유 시트에서 사용하는 여행 공유 응답 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "내 여행 공유 응답 DTO")
public class ShareTripResponseDto {

    @Schema(description = "공유 대상 여행 PK", example = "1")
    private Long tripId;

    @Schema(description = "공유 대상 여행명", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "공유 제목", example = "Let's Trip에서 오사카 여행 일정을 확인해보세요.")
    private String shareTitle;

    @Schema(description = "공유 설명", example = "2026.04.10부터 2026.04.14까지의 오사카 여행 일정을 공유합니다.")
    private String shareDescription;

    @Schema(description = "공유 링크", example = "https://lets-trip.com/trips/share/abc123def456")
    private String shareUrl;

    @Schema(description = "공유 썸네일 이미지 URL", example = "https://cdn.lets-trip.com/trips/osaka.jpg", nullable = true)
    private String imageUrl;

    /**
     * 여행 엔티티와 공유 메타데이터를 여행 공유 응답 DTO로 변환합니다.
     *
     * @param trip 공유 대상 여행 엔티티
     * @param shareTitle 공유 제목
     * @param shareDescription 공유 설명
     * @param shareUrl 공유 링크
     * @return 내 여행 공유 응답 DTO
     */
    public static ShareTripResponseDto from(
            Trip trip,
            String shareTitle,
            String shareDescription,
            String shareUrl
    ) {
        return ShareTripResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .shareTitle(shareTitle)
                .shareDescription(shareDescription)
                .shareUrl(shareUrl)
                .imageUrl(trip.getImageUrl())
                .build();
    }
}
