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

    public PlaceDetailResponseDto findOne(Long placeId, String usersId) {
        if (usersId == null || usersId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Place place = placeRepository.findByPlaceIdAndIsDeletedFalse(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        List<String> tags = placeTagMapRepository.findAllByPlace_PlaceIdAndIsDeletedFalse(placeId).stream()
                .map(placeTagMap -> placeTagMap.getPlaceTag().getTagName())
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();

        boolean saved = savedPlaceRepository.existsByUser_UsersIdAndPlace_PlaceIdAndIsDeletedFalse(usersId, placeId);

        List<Review> reviewPreviews = reviewRepository.findTop3ByPlace_PlaceIdAndIsDeletedFalseOrderByCreatedAtDesc(placeId);
        Map<Long, List<String>> reviewImages = getReviewImages(reviewPreviews);

        return PlaceDetailResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .address(place.getAddress())
                .description(place.getDescription())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .ratingAvg(place.getRatingAvg())
                .reviewCount(place.getReviewCount())
                .openingHours(place.getOpeningHours())
                .imageUrl(place.getImageUrl())
                .placeType(place.getPlaceType())
                .saved(saved)
                .tags(tags)
                .reviewPreviews(reviewPreviews.stream()
                        .map(review -> toReviewPreview(review, reviewImages.getOrDefault(review.getReviewId(), List.of())))
                        .toList())
                .build();
    }

    private Map<Long, List<String>> getReviewImages(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> reviewIds = reviews.stream()
                .map(Review::getReviewId)
                .toList();

        List<ReviewImage> reviewImages = reviewImageRepository.findAllByReview_ReviewIdInAndIsDeletedFalseOrderBySortOrderAsc(reviewIds);

        return reviewImages.stream()
                .collect(Collectors.groupingBy(
                        reviewImage -> reviewImage.getReview().getReviewId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));
    }

    private PlaceReviewPreviewResponseDto toReviewPreview(Review review, List<String> imageUrls) {
        return PlaceReviewPreviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getUser().getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .visitedDate(review.getVisitedDate())
                .imageUrls(imageUrls)
                .build();
    }
}
