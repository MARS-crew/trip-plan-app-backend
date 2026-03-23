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
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
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
 * 장소 조회 및 상세 화면 액션 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int MAX_TAG_COUNT = 3;

    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;

    /**
     * 메인 페이지에 노출할 추천 여행지 목록을 조회합니다.
     * 장소는 평점과 리뷰 수를 기준으로 정렬해 조회하고, 각 장소의 대표 태그를 함께 반환합니다.
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
     * 사용자와 장소 존재 여부를 검증한 뒤, 이미 저장된 장소가 아니면 저장 항목을 생성합니다.
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
     * 조회된 장소 목록을 기준으로 장소별 대표 태그를 묶어 반환합니다.
     * 중복 태그는 제거하고, 화면에 필요한 최대 3개의 태그만 유지합니다.
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
}
