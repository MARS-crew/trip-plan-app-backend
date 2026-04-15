package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 내 여행지 상세 지도 검색 결과 조회 응답 DTO입니다.
 * 검색어 기준 결과 목록뿐 아니라, 빈 결과 상태 문구까지 함께 내려
 * 프론트가 지도/리스트 UI 상태를 바로 렌더링할 수 있도록 구성합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행지 상세 지도 검색 결과 조회 응답 DTO")
public class MyTripMapSearchResponseDto {

    private static final String EMPTY_SEARCH_RESULT_MESSAGE = "검색 결과가 없습니다.";

    @Schema(description = "조회한 여행 PK", example = "5")
    private Long tripId;

    @Schema(description = "조회한 여행 제목", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "검색어(앞뒤 공백 제거값)", example = "오사카성", nullable = true)
    private String keyword;

    @Schema(description = "실제 검색 수행 여부(keyword 존재 시 true)", example = "true")
    private boolean searched;

    @Schema(description = "검색 결과 개수", example = "3")
    private int resultCount;

    @Schema(description = "검색 수행 후 결과가 비었는지 여부", example = "false")
    private boolean emptyResult;

    @Schema(description = "검색 결과가 비었을 때 표시할 안내 문구", example = "검색 결과가 없습니다.", nullable = true)
    private String emptyMessage;

    @Schema(description = "지도 핀/리스트 카드 렌더링용 장소 후보 목록")
    private List<MyTripMapSearchItemResponseDto> searchResults;

    /**
     * 지도 검색 결과 응답 DTO를 생성합니다.
     * 검색어가 없으면 searched=false와 빈 배열을 반환하고,
     * 검색을 수행했는데 결과가 없으면 empty 상태 필드를 함께 세팅합니다.
     *
     * @param tripId 조회한 여행 PK
     * @param tripTitle 조회한 여행 제목
     * @param keyword 정규화된 검색어(null 가능)
     * @param searched 검색 수행 여부
     * @param searchResults 검색 결과 목록
     * @return 지도 검색 결과 응답 DTO
     */
    public static MyTripMapSearchResponseDto of(
            Long tripId,
            String tripTitle,
            String keyword,
            boolean searched,
            List<MyTripMapSearchItemResponseDto> searchResults
    ) {
        boolean emptyResult = searched && searchResults.isEmpty();

        return MyTripMapSearchResponseDto.builder()
                .tripId(tripId)
                .tripTitle(tripTitle)
                .keyword(keyword)
                .searched(searched)
                .resultCount(searchResults.size())
                .emptyResult(emptyResult)
                .emptyMessage(emptyResult ? EMPTY_SEARCH_RESULT_MESSAGE : null)
                .searchResults(searchResults)
                .build();
    }
}

