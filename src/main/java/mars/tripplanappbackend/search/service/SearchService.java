package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.domain.PlaceTagMap;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.search.domain.RecentSearch;
import mars.tripplanappbackend.search.dto.request.DeleteAllRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.DeleteRecentSearchRequestDto;
import mars.tripplanappbackend.search.dto.request.PopularSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.RecentSearchListRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchCategoryRequestDto;
import mars.tripplanappbackend.search.dto.request.SearchResultListRequestDto;
import mars.tripplanappbackend.search.dto.response.DeleteAllRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.DeleteRecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.PopularSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.PopularSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.RecentSearchListResponseDto;
import mars.tripplanappbackend.search.dto.response.RecentSearchResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchCategoryResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchResultListResponseDto;
import mars.tripplanappbackend.search.dto.response.SearchResultResponseDto;
import mars.tripplanappbackend.search.enums.SearchCategory;
import mars.tripplanappbackend.search.repository.PopularSearchKeywordProjection;
import mars.tripplanappbackend.search.repository.RecentSearchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 검색 페이지에서 사용하는 카테고리, 검색 결과, 인기 검색어, 최근 검색어 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final int MAX_TAG_COUNT = 3;
    private static final int MAX_RECENT_SEARCH_COUNT = 5;

    private final MyPageRepository myPageRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final RecentSearchRepository recentSearchRepository;

    /**
     * 검색 페이지 상단에 고정 노출되는 카테고리 목록을 정렬 순서대로 조회합니다.
     *
     * @param requestDto 검색 카테고리 조회 요청 DTO
     * @return 검색 카테고리 목록 응답 DTO
     */
    public SearchCategoryListResponseDto getSearchCategories(SearchCategoryRequestDto requestDto) {
        List<SearchCategoryResponseDto> categories = Arrays.stream(SearchCategory.values())
                .sorted(Comparator.comparingInt(SearchCategory::getSortOrder))
                .map(SearchCategoryResponseDto::from)
                .toList();

        return SearchCategoryListResponseDto.of(categories);
    }

    /**
     * 검색어를 기준으로 장소명, 지역명, 주소, 태그가 일치하는 검색 결과 리스트를 조회합니다.
     * 인증 사용자가 검색한 경우에는 최근 검색어 목록도 함께 갱신합니다.
     *
     * @param requestDto 검색 결과 리스트 조회 요청 DTO
     * @return 검색 결과 리스트 응답 DTO
     */
    @Transactional
    public SearchResultListResponseDto getSearchResults(SearchResultListRequestDto requestDto) {
        String keyword = normalizeKeyword(requestDto.getKeyword());

        List<Place> places = placeRepository.searchByKeyword(keyword);
        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(places);

        List<SearchResultResponseDto> searchResults = places.stream()
                .map(place -> SearchResultResponseDto.from(
                        place,
                        tagsByPlaceId.getOrDefault(place.getPlaceId(), List.of())
                ))
                .toList();

        saveRecentSearchIfAuthenticated(requestDto.getUsersId(), keyword);

        return SearchResultListResponseDto.of(keyword, searchResults);
    }

    /**
     * 최근 검색 데이터 기준으로 가장 많이 검색된 키워드 상위 5건을 조회합니다.
     * 동일 검색 횟수일 경우 최근에 다시 검색된 키워드를 우선 노출합니다.
     *
     * @param requestDto 인기 검색어 조회 요청 DTO
     * @return 인기 검색어 목록 응답 DTO
     */
    public PopularSearchListResponseDto getPopularSearches(PopularSearchListRequestDto requestDto) {
        List<PopularSearchKeywordProjection> popularKeywords = recentSearchRepository.findPopularSearchKeywords(
                PageRequest.of(0, requestDto.getLimit())
        );

        List<PopularSearchResponseDto> popularSearches = IntStream.range(0, popularKeywords.size())
                .mapToObj(index -> {
                    PopularSearchKeywordProjection projection = popularKeywords.get(index);
                    return PopularSearchResponseDto.of(
                            index + 1,
                            projection.getKeyword(),
                            projection.getSearchCount()
                    );
                })
                .toList();

        return PopularSearchListResponseDto.of(popularSearches);
    }

    /**
     * 검색 페이지에 노출할 최근 검색어 최대 5건을 최신순으로 조회합니다.
     *
     * @param requestDto 최근 검색어 목록 조회 요청 DTO
     * @return 최근 검색어 목록 응답 DTO
     */
    public RecentSearchListResponseDto getRecentSearches(RecentSearchListRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        List<RecentSearch> recentSearches = recentSearchRepository
                .findAllByUser_UsersIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        requestDto.getUsersId(),
                        PageRequest.of(0, requestDto.getLimit())
                );

        List<RecentSearchResponseDto> responseDtos = recentSearches.stream()
                .map(RecentSearchResponseDto::from)
                .toList();

        return RecentSearchListResponseDto.of(responseDtos);
    }

    /**
     * 검색 페이지 최근 검색어 목록에서 선택한 항목 한 건을 soft delete 처리합니다.
     *
     * @param requestDto 최근 검색어 삭제 요청 DTO
     * @return 최근 검색어 삭제 응답 DTO
     */
    @Transactional
    public DeleteRecentSearchResponseDto deleteRecentSearch(DeleteRecentSearchRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        RecentSearch recentSearch = recentSearchRepository
                .findByRecentSearchIdAndUser_UsersIdAndIsDeletedFalse(
                        requestDto.getRecentSearchId(),
                        requestDto.getUsersId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        recentSearch.markDeleted();
        return DeleteRecentSearchResponseDto.from(recentSearch);
    }

    /**
     * 검색 페이지 최근 검색어 목록에 남아 있는 항목을 현재 로그인 사용자 기준으로 전체 삭제합니다.
     *
     * @param requestDto 최근 검색어 전체 삭제 요청 DTO
     * @return 최근 검색어 전체 삭제 응답 DTO
     */
    @Transactional
    public DeleteAllRecentSearchResponseDto deleteAllRecentSearches(DeleteAllRecentSearchRequestDto requestDto) {
        validateAuthenticatedUser(requestDto.getUsersId());

        List<RecentSearch> recentSearches =
                recentSearchRepository.findAllByUser_UsersIdAndIsDeletedFalse(requestDto.getUsersId());

        recentSearches.forEach(RecentSearch::markDeleted);
        return DeleteAllRecentSearchResponseDto.from(recentSearches.size());
    }

    /**
     * 검색 결과 목록에 사용할 태그를 장소 PK 기준으로 묶어 반환합니다.
     *
     * @param places 검색된 장소 엔티티 목록
     * @return 장소 PK를 키로 가지는 태그 목록 맵
     */
    private Map<Long, List<String>> getTagsByPlaceId(List<Place> places) {
        if (places.isEmpty()) {
            return Map.of();
        }

        List<Long> placeIds = places.stream()
                .map(Place::getPlaceId)
                .toList();

        List<PlaceTagMap> placeTagMaps = placeTagMapRepository.findAllByPlace_PlaceIdInAndIsDeletedFalse(placeIds);

        return placeTagMaps.stream()
                .collect(Collectors.groupingBy(
                        placeTagMap -> placeTagMap.getPlace().getPlaceId(),
                        Collectors.mapping(
                                placeTagMap -> placeTagMap.getPlaceTag().getTagName(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        tags -> tags.stream()
                                                .distinct()
                                                .limit(MAX_TAG_COUNT)
                                                .toList()
                                )
                        )
                ));
    }

    /**
     * 로그인 사용자가 검색한 경우 최근 검색어를 저장하고, 기존 중복 검색어와 5개 초과 데이터는 soft delete 처리합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param keyword 저장할 검색어
     */
    private void saveRecentSearchIfAuthenticated(String usersId, String keyword) {
        User authenticatedUser = findAuthenticatedUser(usersId);
        if (authenticatedUser == null) {
            return;
        }

        List<RecentSearch> duplicatedSearches =
                recentSearchRepository.findAllByUser_UsersIdAndKeywordAndIsDeletedFalse(usersId, keyword);
        duplicatedSearches.forEach(RecentSearch::markDeleted);

        recentSearchRepository.save(RecentSearch.create(authenticatedUser, keyword));

        List<RecentSearch> recentSearches =
                recentSearchRepository.findAllByUser_UsersIdAndIsDeletedFalseOrderByCreatedAtDesc(usersId);

        if (recentSearches.size() <= MAX_RECENT_SEARCH_COUNT) {
            return;
        }

        recentSearches.stream()
                .skip(MAX_RECENT_SEARCH_COUNT)
                .forEach(RecentSearch::markDeleted);
    }

    /**
     * 현재 인증된 사용자 아이디가 있으면 사용자 엔티티를 조회하고, 없으면 null을 반환합니다.
     * 공개 검색 API에서는 비로그인 상태도 허용하므로 예외를 발생시키지 않습니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @return 조회된 사용자 엔티티, 없으면 null
     */
    private User findAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            return null;
        }

        return myPageRepository.findByUsersId(usersId).orElse(null);
    }

    /**
     * 현재 인증된 사용자 아이디가 실제 사용자 테이블에 존재하는지 검증합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     */
    private void validateAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 검색어 입력값을 검증하고 앞뒤 공백을 제거한 값을 반환합니다.
     *
     * @param keyword 검색어 입력값
     * @return 공백 정리된 검색어
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return keyword.trim();
    }
}
