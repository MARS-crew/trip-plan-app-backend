package mars.tripplanappbackend.place.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.SavedPlace;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.domain.PlaceTagMap;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavePlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SharePlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SharePlaceResponseDto;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 장소 조회와 여행지 상세 화면 액션에 필요한 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int MAX_TAG_COUNT = 3;
    private static final String SHARE_URL_TEMPLATE = "https://lets-trip.com/places/%d";
    private static final String SHARE_TITLE_TEMPLATE = "Let's Trip에서 %s을(를) 확인해보세요.";
    private static final String SHARE_DESCRIPTION_TEMPLATE = "%s에 위치한 %s의 상세 정보를 공유합니다.";

    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;

    /**
     * 메인 페이지에 노출할 추천 여행지 목록을 조회합니다.
     * 장소를 평점과 리뷰 수 기준으로 정렬해 조회하고, 각 장소별 대표 태그를 함께 반환합니다.
     *
     * @param requestDto 추천 여행지 조회 요청 DTO
     * @return 추천 여행지 목록 응답 DTO
     */
    public RecommendedPlaceListResponseDto getRecommendedPlaces(RecommendedPlaceRequestDto requestDto) {
        List<Place> places = placeRepository.findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc(
                PageRequest.of(0, requestDto.getLimit())
        );

        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(places);

        List<RecommendedPlaceResponseDto> recommendedPlaces = places.stream()
                .map(place -> RecommendedPlaceResponseDto.from(
                        place,
                        tagsByPlaceId.getOrDefault(place.getPlaceId(), List.of())
                ))
                .toList();

        return RecommendedPlaceListResponseDto.of(recommendedPlaces);
    }

    /**
     * 여행지 상세 화면에서 선택한 장소를 저장 목록에 추가합니다.
     * 사용자와 장소 존재 여부를 먼저 검증하고, 중복 저장 여부까지 확인한 뒤 저장 엔티티를 생성합니다.
     *
     * @param requestDto 저장 항목 추가 요청 DTO
     * @return 저장 항목 추가 응답 DTO
     */
    @Transactional
    public SavePlaceResponseDto savePlace(SavePlaceRequestDto requestDto) {
        validateSavePlaceRequest(requestDto);

        User user = findUser(requestDto.getUsersId());
        Place place = findPlace(requestDto.getPlaceId());

        if (savedPlaceRepository.existsByUser_UserIdAndPlace_PlaceIdAndIsDeletedFalse(user.getUserId(), place.getPlaceId())) {
            throw new BusinessException(ErrorCode.SAVED_PLACE_ALREADY_EXISTS);
        }

        SavedPlace savedPlace = savedPlaceRepository.save(
                SavedPlace.builder()
                        .user(user)
                        .place(place)
                        .build()
        );

        return SavePlaceResponseDto.from(savedPlace);
    }

    /**
     * 여행지 상세 화면에서 사용할 공유 정보를 조회합니다.
     * 인증 사용자와 장소를 검증한 뒤, 프론트가 바로 사용할 수 있는 공유 제목/설명/링크/이미지를 조합합니다.
     *
     * @param requestDto 여행지 공유 요청 DTO
     * @return 여행지 공유 응답 DTO
     */
    public SharePlaceResponseDto sharePlace(SharePlaceRequestDto requestDto) {
        validateSharePlaceRequest(requestDto);

        findUser(requestDto.getUsersId());
        Place place = findPlace(requestDto.getPlaceId());

        return SharePlaceResponseDto.from(
                place,
                createShareTitle(place),
                createShareDescription(place),
                createShareUrl(place.getPlaceId())
        );
    }

    /**
     * 조회된 장소 목록을 기준으로 장소별 대표 태그를 묶어서 반환합니다.
     * 중복 태그는 제거하고 화면에 필요한 최대 3개까지만 유지합니다.
     *
     * @param places 추천 대상 장소 목록
     * @return 장소 PK별 대표 태그 목록
     */
    private Map<Long, List<String>> getTagsByPlaceId(List<Place> places) {
        if (places.isEmpty()) {
            return Collections.emptyMap();
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
     * 저장 요청 파라미터가 유효한지 확인합니다.
     *
     * @param requestDto 저장 요청 DTO
     */
    private void validateSavePlaceRequest(SavePlaceRequestDto requestDto) {
        if (requestDto.getPlaceId() == null || requestDto.getPlaceId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 공유 요청 파라미터가 유효한지 확인합니다.
     *
     * @param requestDto 공유 요청 DTO
     */
    private void validateSharePlaceRequest(SharePlaceRequestDto requestDto) {
        if (requestDto.getPlaceId() == null || requestDto.getPlaceId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 사용자 아이디로 활성 사용자 정보를 조회합니다.
     *
     * @param usersId JWT에서 추출한 사용자 아이디
     * @return 사용자 엔티티
     */
    private User findUser(String usersId) {
        return myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 장소 PK로 삭제되지 않은 장소 정보를 조회합니다.
     *
     * @param placeId 장소 PK
     * @return 장소 엔티티
     */
    private Place findPlace(Long placeId) {
        return placeRepository.findByPlaceIdAndIsDeletedFalse(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    /**
     * 장소명을 기반으로 공유 제목을 생성합니다.
     *
     * @param place 공유 대상 장소
     * @return 공유 제목
     */
    private String createShareTitle(Place place) {
        return SHARE_TITLE_TEMPLATE.formatted(place.getName());
    }

    /**
     * 국가명과 도시명을 조합해 공유 설명을 생성합니다.
     * 도시명이 없으면 국가명만 사용해 자연스러운 설명을 만듭니다.
     *
     * @param place 공유 대상 장소
     * @return 공유 설명
     */
    private String createShareDescription(Place place) {
        String location = place.getCityName() == null || place.getCityName().isBlank()
                ? place.getCountryName()
                : place.getCountryName() + " " + place.getCityName();
        return SHARE_DESCRIPTION_TEMPLATE.formatted(location, place.getName());
    }

    /**
     * 장소 PK를 기준으로 공유 링크를 생성합니다.
     *
     * @param placeId 공유 대상 장소 PK
     * @return 공유 링크
     */
    private String createShareUrl(Long placeId) {
        return SHARE_URL_TEMPLATE.formatted(placeId);
    }
}
