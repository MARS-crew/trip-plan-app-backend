package mars.tripplanappbackend.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mars.tripplanappbackend.place.domain.Place;

/**
 * 여행지 상세 화면의 공유 시트에서 사용할 공유 정보 응답 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "여행지 공유 응답 DTO")
public class SharePlaceResponseDto {

    @Schema(description = "공유 대상 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "공유 대상 장소명", example = "해운대 해수욕장")
    private String placeName;

    @Schema(description = "공유 제목", example = "Let's Trip에서 해운대 해수욕장을 확인해보세요.")
    private String shareTitle;

    @Schema(description = "공유 설명", example = "대한민국 부산에 위치한 해운대 해수욕장의 상세 정보를 공유합니다.")
    private String shareDescription;

    @Schema(description = "공유 링크", example = "https://lets-trip.com/places/7")
    private String shareUrl;

    @Schema(description = "공유 썸네일 이미지 URL", example = "https://cdn.lets-trip.com/place/haeundae.jpg")
    private String imageUrl;

    /**
     * 장소 엔티티와 공유 메타데이터를 공유 응답 DTO로 변환합니다.
     *
     * @param place 공유 대상 장소 엔티티
     * @param shareTitle 공유 제목
     * @param shareDescription 공유 설명
     * @param shareUrl 공유 링크
     * @return 공유 응답 DTO
     */
    public static SharePlaceResponseDto from(
            Place place,
            String shareTitle,
            String shareDescription,
            String shareUrl
    ) {
        return SharePlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getName())
                .shareTitle(shareTitle)
                .shareDescription(shareDescription)
                .shareUrl(shareUrl)
                .imageUrl(place.getImageUrl())
                .build();
    }
}
