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
import mars.tripplanappbackend.place.dto.request.DeleteSavedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.NearbyRecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.RecommendedPlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavePlaceRequestDto;
import mars.tripplanappbackend.place.dto.request.SavedPlaceCategoryListRequestDto;
import mars.tripplanappbackend.place.dto.request.SavedPlaceListRequestDto;
import mars.tripplanappbackend.place.dto.request.SharePlaceRequestDto;
import mars.tripplanappbackend.place.dto.response.NearbyRecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.NearbyRecommendedPlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceReviewPreviewResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavePlaceResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceCategoryListResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceCategoryResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceItemResponseDto;
import mars.tripplanappbackend.place.dto.response.SavedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.SharePlaceResponseDto;
import mars.tripplanappbackend.place.enums.SavedPlaceFilterType;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.review.domain.ReviewImage;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 메인 페이지와 여행지 상세 페이지에서 사용하는 장소 API의 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int MAX_TAG_COUNT = 3;
    private static final int MAX_NEARBY_RECOMMENDED_PLACE_COUNT = 3;
    private static final long MAX_NEARBY_DISTANCE_METERS = 1_000L;
    private static final String SHARE_URL_TEMPLATE = "https://lets-trip.com/places/%d";
    private static final String SHARE_TITLE_TEMPLATE = "Let's Trip에서 %s을(를) 확인해보세요.";
    private static final String SHARE_DESCRIPTION_TEMPLATE = "%s에 위치한 %s의 상세 정보를 공유합니다.";

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    /**
     * 평점과 리뷰 수를 기준으로 메인 페이지 추천 여행지 목록을 조회합니다.
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
     * 저장 탭과 여행 추가 바텀시트의 저장한 장소 탭에서 사용하는 저장한 장소 목록을 조회합니다.
     * 전체 저장 개수와 현재 필터 기준 카드 목록을 함께 반환해 저장 화면과 바텀시트가 같은 응답을 재사용할 수 있도록 합니다.
     *
     * @param requestDto 로그인 사용자 아이디와 필터 유형을 담은 요청 DTO
     * @return 저장한 장소 목록 응답 DTO
     */
    public SavedPlaceListResponseDto getSavedPlaces(SavedPlaceListRequestDto requestDto) {
        validateSavedPlaceListRequest(requestDto);
        validateAuthenticatedUser(requestDto.getUsersId());

        long savedPlaceCount =
                savedPlaceRepository.countByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalse(requestDto.getUsersId());

        List<SavedPlace> savedPlaces = findSavedPlacesByFilter(requestDto.getUsersId(), requestDto.getFilterType());
        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(
                savedPlaces.stream()
                        .map(SavedPlace::getPlace)
                        .toList()
        );

        List<SavedPlaceItemResponseDto> savedPlaceCards = savedPlaces.stream()
                .map(savedPlace -> SavedPlaceItemResponseDto.from(
                        savedPlace,
                        tagsByPlaceId.getOrDefault(savedPlace.getPlace().getPlaceId(), List.of())
                ))
                .toList();

        return SavedPlaceListResponseDto.of(requestDto.getFilterType(), savedPlaceCount, savedPlaceCards);
    }

    /**
     * 저장한 장소 카테고리 목록을 조회합니다.
     * 저장된 장소 조회 API와 별도로 탭 렌더링용 카테고리 메타 정보를 제공합니다.
     *
     * @param requestDto 카테고리 목록 조회 요청 DTO
     * @return 저장한 장소 카테고리 목록 응답 DTO
     */
    public SavedPlaceCategoryListResponseDto getSavedPlaceCategories(SavedPlaceCategoryListRequestDto requestDto) {
        validateSavedPlaceCategoryListRequest(requestDto);
        validateAuthenticatedUser(requestDto.getUsersId());

        long totalSavedPlaceCount =
                savedPlaceRepository.countByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalse(requestDto.getUsersId());

        List<SavedPlaceCategoryResponseDto> categories = Arrays.stream(SavedPlaceFilterType.values())
                .map(filterType -> SavedPlaceCategoryResponseDto.of(
                        filterType,
                        countSavedPlacesByFilterType(requestDto.getUsersId(), filterType, totalSavedPlaceCount)
                ))
                .toList();

        return SavedPlaceCategoryListResponseDto.of(totalSavedPlaceCount, categories);
    }

    /**
     * 여행지 상세 페이지에서 선택한 장소를 저장 목록에 추가합니다.
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

        if (savedPlaceRepository.existsByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(
                requestDto.getUsersId(),
                requestDto.getPlaceId()
        )) {
            throw new BusinessException(ErrorCode.SAVED_PLACE_ALREADY_EXISTS);
        }

        SavedPlace savedPlace = savedPlaceRepository.save(
                SavedPlace.builder()
                        .user(user)
                        .place(place)
                        .build()
        );

        return SavePlaceResponseDto.saved(savedPlace);
    }

    /**
     * 여행지 상세 페이지에서 공유 시트에 필요한 공유 메타데이터를 생성합니다.
     * 사용자 인증과 장소 존재 여부를 검증한 뒤, 공유 제목/설명/링크를 조합해 반환합니다.
     *
     * @param requestDto 공유 요청 DTO
     * @return 여행지 공유 응답 DTO
     */
    /**
     * 저장한 장소 카드나 장소 상세 화면에서 북마크를 다시 눌러 저장을 취소합니다.
     * placeId 기준으로 사용자의 저장 이력을 찾은 뒤 soft delete 처리하여 응답 구조를 저장 API와 동일하게 유지합니다.
     *
     * @param requestDto 저장 취소 요청 DTO
     * @return 저장 취소 결과 응답 DTO
     */
    @Transactional
    public SavePlaceResponseDto deleteSavedPlace(DeleteSavedPlaceRequestDto requestDto) {
        validateDeleteSavedPlaceRequest(requestDto);

        findUser(requestDto.getUsersId());
        findPlace(requestDto.getPlaceId());

        SavedPlace savedPlace = findSavedPlace(requestDto.getUsersId(), requestDto.getPlaceId());
        savedPlace.markDeleted();

        return SavePlaceResponseDto.unsaved(savedPlace);
    }

    /**
     * Builds the share metadata consumed by the place detail screen.
     *
     * @param requestDto share request DTO
     * @return place share response DTO
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
     * 여행지 상세 페이지에 노출할 주변 추천 장소 목록을 조회합니다.
     * 주변 장소는 같은 도시를 우선으로 찾고, 없으면 같은 국가, 그것도 부족하면
     * 현재 장소를 제외한 전체 장소에서 후보를 조회합니다.
     *
     * @param requestDto 주변 추천 장소 조회 요청 DTO
     * @return 주변 추천 장소 목록 응답 DTO
     */
    public NearbyRecommendedPlaceListResponseDto getNearbyRecommendedPlaces(
            NearbyRecommendedPlaceRequestDto requestDto
    ) {
        validateAuthenticatedUser(requestDto.getUsersId());

        Place targetPlace = placeRepository.findByPlaceIdAndIsDeletedFalse(requestDto.getPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        List<Place> sortedCandidates = findNearbyCandidates(targetPlace).stream()
                .sorted(buildNearbyPlaceComparator(targetPlace))
                .toList();

        List<Place> selectedCandidates = sortedCandidates.stream()
                .filter(place -> isWithinNearbyDistance(targetPlace, place))
                .limit(MAX_NEARBY_RECOMMENDED_PLACE_COUNT)
                .toList();

        if (selectedCandidates.isEmpty()) {
            selectedCandidates = sortedCandidates.stream()
                    .limit(MAX_NEARBY_RECOMMENDED_PLACE_COUNT)
                    .toList();
        }

        List<NearbyRecommendedPlaceResponseDto> nearbyRecommendedPlaces = selectedCandidates.stream()
                .map(place -> NearbyRecommendedPlaceResponseDto.from(place, calculateDistanceMeters(targetPlace, place)))
                .toList();

        return NearbyRecommendedPlaceListResponseDto.of(nearbyRecommendedPlaces);
    }

    /**
     * 여행지 상세 페이지에 필요한 장소 상세 응답 데이터를 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @param usersId 인증 사용자 아이디
     * @return 여행지 상세 응답 DTO
     */
    public PlaceDetailResponseDto findOne(Long placeId, String usersId) {
        validateAuthenticatedUser(usersId);

        Place place = placeRepository.findByPlaceIdAndIsDeletedFalse(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        List<String> tags = findPlaceTags(placeId);
        boolean saved = savedPlaceRepository.existsByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(usersId, placeId);
        List<PlaceReviewPreviewResponseDto> reviewPreviews = findReviewPreviews(placeId);

        return PlaceDetailResponseDto.from(place, tags, saved, reviewPreviews);
    }

    /**
     * 메인 페이지 추천 장소 목록에 사용할 대표 태그를 장소별로 묶어 반환합니다.
     *
     * @param places 추천 장소 엔티티 목록
     * @return 장소 PK를 키로 가지는 태그 목록 맵
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
     * 인증 사용자 아이디가 비어 있지 않고 실제 사용자 테이블에 존재하는지 확인합니다.
     *
     * @param usersId 인증 사용자 아이디
     */
    private void validateAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
     * 저장 취소 요청에 필요한 장소 PK와 사용자 아이디가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 저장 취소 요청 DTO
     */
    private void validateDeleteSavedPlaceRequest(DeleteSavedPlaceRequestDto requestDto) {
        if (requestDto.getPlaceId() == null || requestDto.getPlaceId() < 1 || requestDto.getUsersId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 저장한 장소 목록 조회 요청에 필요한 사용자 아이디와 필터 정보가 모두 존재하는지 검증합니다.
     *
     * @param requestDto 저장한 장소 목록 조회 요청 DTO
     */
    private void validateSavedPlaceListRequest(SavedPlaceListRequestDto requestDto) {
        if (requestDto.getUsersId() == null || requestDto.getUsersId().isBlank() || requestDto.getFilterType() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 저장한 장소 카테고리 목록 조회 요청 파라미터를 검증합니다.
     *
     * @param requestDto 저장한 장소 카테고리 목록 조회 요청 DTO
     */
    private void validateSavedPlaceCategoryListRequest(SavedPlaceCategoryListRequestDto requestDto) {
        if (requestDto.getUsersId() == null || requestDto.getUsersId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 공유 요청에 필요한 장소 PK와 사용자 아이디가 모두 유효한지 검증합니다.
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
        return myPageRepository.findByUsersIdAndIsDeletedFalse(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 저장한 장소 목록 필터 유형에 맞춰 저장한 장소 엔티티 목록을 조회합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param filterType 저장한 장소 목록 필터 유형
     * @return 필터 조건이 반영된 저장한 장소 엔티티 목록
     */
    private List<SavedPlace> findSavedPlacesByFilter(String usersId, SavedPlaceFilterType filterType) {
        if (filterType == SavedPlaceFilterType.ALL) {
            return savedPlaceRepository.findAllByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseOrderByCreatedAtDesc(usersId);
        }

        return savedPlaceRepository
                .findAllByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseAndPlace_PlaceTypeOrderByCreatedAtDesc(
                        usersId,
                        filterType.getPlaceType()
                );
    }

    /**
     * 카테고리 필터 유형별 저장 개수를 조회합니다.
     * `ALL`은 전체 개수를 그대로 사용하고, 나머지는 placeType 조건 count 쿼리를 수행합니다.
     *
     * @param usersId 현재 로그인 사용자 아이디
     * @param filterType 저장한 장소 필터 유형
     * @param totalSavedPlaceCount 전체 저장 개수
     * @return 카테고리 필터에 해당하는 저장 개수
     */
    private long countSavedPlacesByFilterType(
            String usersId,
            SavedPlaceFilterType filterType,
            long totalSavedPlaceCount
    ) {
        if (filterType == SavedPlaceFilterType.ALL) {
            return totalSavedPlaceCount;
        }

        return savedPlaceRepository.countByUser_UsersIdAndIsDeletedFalseAndPlace_IsDeletedFalseAndPlace_PlaceType(
                usersId,
                filterType.getPlaceType()
        );
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
     * 장소명을 이용해 공유 시트 제목을 생성합니다.
     *
     * @param place 공유 대상 장소 엔티티
     * @return 공유 제목
     */
    /**
     * 현재 사용자가 해당 장소를 실제로 저장해 둔 이력이 있는지 조회합니다.
     *
     * @param usersId 현재 로그인한 사용자 아이디
     * @param placeId 저장 이력을 찾을 장소 PK
     * @return 저장한 장소 엔티티
     */
    private SavedPlace findSavedPlace(String usersId, Long placeId) {
        return savedPlaceRepository.findByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(usersId, placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVED_PLACE_NOT_FOUND));
    }

    private String createShareTitle(Place place) {
        return SHARE_TITLE_TEMPLATE.formatted(place.getName());
    }

    /**
     * 국가/도시 정보와 장소명을 조합해 공유 시트 설명을 생성합니다.
     *
     * @param place 공유 대상 장소 엔티티
     * @return 공유 설명
     */
    private String createShareDescription(Place place) {
        String location = hasText(place.getCityName())
                ? place.getCountryName() + " " + place.getCityName()
                : place.getCountryName();

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

    /**
     * 여행지 상세 페이지에 노출할 장소 태그를 최대 3개까지 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @return 중복을 제거한 장소 태그 목록
     */
    private List<String> findPlaceTags(Long placeId) {
        return placeTagMapRepository.findAllByPlace_PlaceIdAndIsDeletedFalse(placeId).stream()
                .map(placeTagMap -> placeTagMap.getPlaceTag().getTagName())
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();
    }

    /**
     * 주변 추천 후보 장소를 같은 도시 기준으로 먼저 조회하고, 없으면 같은 국가,
     * 마지막으로는 현재 장소를 제외한 전체 장소에서 조회합니다.
     *
     * @param targetPlace 현재 상세 페이지 기준 장소
     * @return 주변 추천 후보 장소 목록
     */
    private List<Place> findNearbyCandidates(Place targetPlace) {
        if (hasText(targetPlace.getCityName())) {
            List<Place> cityPlaces = placeRepository.findAllByCityNameAndPlaceIdNotAndIsDeletedFalse(
                    targetPlace.getCityName(),
                    targetPlace.getPlaceId()
            );
            if (!cityPlaces.isEmpty()) {
                return cityPlaces;
            }
        }

        if (hasText(targetPlace.getCountryName())) {
            List<Place> countryPlaces = placeRepository.findAllByCountryNameAndPlaceIdNotAndIsDeletedFalse(
                    targetPlace.getCountryName(),
                    targetPlace.getPlaceId()
            );
            if (!countryPlaces.isEmpty()) {
                return countryPlaces;
            }
        }

        return placeRepository.findAllByPlaceIdNotAndIsDeletedFalse(targetPlace.getPlaceId());
    }

    /**
     * 주변 추천 장소 정렬 기준을 생성합니다.
     * 거리 우선 정렬 후 평점, 리뷰 수, 장소 PK 순으로 정렬합니다.
     *
     * @param targetPlace 현재 상세 페이지 기준 장소
     * @return 주변 추천 후보 정렬 Comparator
     */
    private Comparator<Place> buildNearbyPlaceComparator(Place targetPlace) {
        return Comparator
                .comparing(
                        (Place place) -> calculateDistanceMeters(targetPlace, place),
                        Comparator.nullsLast(Long::compareTo)
                )
                .thenComparing(Place::getRatingAvg, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Place::getReviewCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Place::getPlaceId);
    }

    /**
     * 후보 장소가 주변 추천 섹션의 거리 기준 이내인지 확인합니다.
     *
     * @param targetPlace 현재 상세 페이지 기준 장소
     * @param candidate 주변 추천 후보 장소
     * @return 1km 이내이면 true
     */
    private boolean isWithinNearbyDistance(Place targetPlace, Place candidate) {
        Long distanceMeters = calculateDistanceMeters(targetPlace, candidate);
        return distanceMeters != null && distanceMeters <= MAX_NEARBY_DISTANCE_METERS;
    }

    /**
     *
     * @param source 기준 장소
     * @param target 비교 대상 장소
     * @return 거리(미터), 좌표가 없으면 null
     */
    private Long calculateDistanceMeters(Place source, Place target) {
        if (source.getLatitude() == null || source.getLongitude() == null
                || target.getLatitude() == null || target.getLongitude() == null) {
            return null;
        }

        double sourceLatitude = source.getLatitude().doubleValue();
        double sourceLongitude = source.getLongitude().doubleValue();
        double targetLatitude = target.getLatitude().doubleValue();
        double targetLongitude = target.getLongitude().doubleValue();

        double latitudeDelta = Math.toRadians(targetLatitude - sourceLatitude);
        double longitudeDelta = Math.toRadians(targetLongitude - sourceLongitude);
        double latitude1 = Math.toRadians(sourceLatitude);
        double latitude2 = Math.toRadians(targetLatitude);

        double haversine = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double distance = 6_371_000d * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        return Math.round(distance);
    }

    /**
     * 전달된 문자열이 실제로 사용할 수 있는 값인지 확인합니다.
     *
     * @param value 확인할 문자열 값
     * @return null이 아니고 공백이 아니면 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 여행지 상세 페이지에 노출할 최신 리뷰 미리보기 3개를 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @return 리뷰 미리보기 응답 DTO 목록
     */
    private List<PlaceReviewPreviewResponseDto> findReviewPreviews(Long placeId) {
        List<Review> reviews = reviewRepository.findTop3ByPlace_PlaceIdAndIsDeletedFalseOrderByCreatedAtDesc(placeId);
        Map<Long, List<String>> reviewImages = findReviewImages(reviews);

        return reviews.stream()
                .map(review -> PlaceReviewPreviewResponseDto.from(
                        review,
                        reviewImages.getOrDefault(review.getReviewId(), List.of())
                ))
                .toList();
    }

    /**
     * 리뷰 목록에 포함된 이미지 URL을 리뷰 PK 기준으로 묶어 반환합니다.
     *
     * @param reviews 미리보기용으로 조회한 리뷰 엔티티 목록
     * @return 리뷰 PK를 키로 가지는 이미지 URL 목록 맵
     */
    private Map<Long, List<String>> findReviewImages(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> reviewIds = reviews.stream()
                .map(Review::getReviewId)
                .toList();

        List<ReviewImage> reviewImages = reviewImageRepository
                .findAllByReview_ReviewIdInAndIsDeletedFalseOrderBySortOrderAsc(reviewIds);

        return reviewImages.stream()
                .collect(Collectors.groupingBy(
                        reviewImage -> reviewImage.getReview().getReviewId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));
    }
}
