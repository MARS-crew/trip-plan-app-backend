package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.WishlistPlace;
import mars.tripplanappbackend.trip.enums.WishlistSourceType;

import java.time.LocalDate;

/**
 * 위시리스트 장소 추가 결과를 응답할 때 사용하는 DTO입니다.
 * 어떤 여행의 어떤 날짜 카드에서 어떤 장소가 위시리스트에 추가되었는지
 * 화면에서 바로 활용할 수 있도록 필요한 정보를 함께 내려줍니다.
 */
@Getter
@Builder
@Schema(description = "위시리스트 장소 추가 응답 DTO")
public class AddWishlistPlaceResponseDto {

    @Schema(description = "추가된 위시리스트 PK", example = "11")
    private Long wishlistPlaceId;

    @Schema(description = "위시리스트가 추가된 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "추가된 장소 PK", example = "7")
    private Long placeId;

    @Schema(description = "추가된 장소명", example = "삿포로 시계탑")
    private String placeName;

    @Schema(description = "선택한 날짜 카드 기준 일정 날짜", example = "2026-04-20")
    private LocalDate scheduleDate;

    @Schema(description = "선택한 날짜가 여행 기준 몇 일차인지 나타내는 값", example = "1")
    private int dayNo;

    @Schema(description = "위시리스트 추가 경로 구분값", example = "RECOMMEND")
    private WishlistSourceType sourceType;

    @Schema(description = "위시리스트 추가 성공 여부", example = "true")
    private boolean added;

    /**
     * 저장된 위시리스트 엔티티와 화면 문맥 정보를 조합해 응답 DTO를 생성합니다.
     *
     * @param wishlistPlace 저장이 완료된 위시리스트 엔티티
     * @param scheduleDate 사용자가 선택한 날짜 카드 기준 일정 날짜
     * @param dayNo 여행 시작일 기준 일차 번호
     * @return 위시리스트 장소 추가 응답 DTO
     */
    public static AddWishlistPlaceResponseDto from(WishlistPlace wishlistPlace, LocalDate scheduleDate, int dayNo) {
        return AddWishlistPlaceResponseDto.builder()
                .wishlistPlaceId(wishlistPlace.getWishlistPlaceId())
                .tripId(wishlistPlace.getTrip().getTripId())
                .placeId(wishlistPlace.getPlace().getPlaceId())
                .placeName(wishlistPlace.getPlace().getName())
                .scheduleDate(scheduleDate)
                .dayNo(dayNo)
                .sourceType(wishlistPlace.getSourceType())
                .added(true)
                .build();
    }
}
