package mars.tripplanappbackend.place.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.dto.response.PlaceDetailResponseDto;
import mars.tripplanappbackend.place.dto.response.PlaceReviewPreviewResponseDto;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.review.domain.ReviewImage;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int MAX_TAG_COUNT = 3;

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    /**
     * 장소 상세 페이지에 필요한 조회 결과를 응답 DTO 형태로 반환합니다.
     *
     * @param placeId 조회할 장소 PK
     * @param usersId JWT 인증 정보에서 추출한 사용자 아이디
     * @return 장소 기본 정보, 태그, 저장 여부, 리뷰 미리보기를 담은 응답 DTO
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
     * 인증 사용자 아이디가 비어 있지 않고 실제 회원으로 존재하는지 검증
     *
     * @param usersId JWT 인증 정보에서 추출한 사용자 아이디
     */
    private void validateAuthenticatedUser(String usersId) {
        if (usersId == null || usersId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 장소 상세 상단에 노출할 태그를 최대 3개까지 조회합니다.
     *
     * @param placeId 조회할 장소 PK
     * @return 중복이 제거된 태그명 목록
     */
    private List<String> findPlaceTags(Long placeId) {
        return placeTagMapRepository.findAllByPlace_PlaceIdAndIsDeletedFalse(placeId).stream()
                .map(placeTagMap -> placeTagMap.getPlaceTag().getTagName())
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();
    }

    /**
     * 장소 상세 하단에 노출할 최신 리뷰 미리보기 3건을 조회
     *
     * @param placeId 조회할 장소 PK
     * @return 리뷰 응답 DTO 목록
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
     * 리뷰 목록에 포함된 리뷰 이미지 URL을 리뷰 PK 기준으로 그룹화시킵니다.
     *
     * @param reviews 리뷰 미리보기 대상 목록
     * @return reviewId 기준 이미지 URL 목록 맵
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
