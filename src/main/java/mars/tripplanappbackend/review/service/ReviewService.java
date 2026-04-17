package mars.tripplanappbackend.review.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.service.MyPageService;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.review.domain.ReviewImage;
import mars.tripplanappbackend.review.dto.request.ReviewCreateRequestDto;
import mars.tripplanappbackend.review.dto.response.ReviewCreateResponseDto;
import mars.tripplanappbackend.review.dto.response.ReviewPreviewResponseDto;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;
    private final MyPageRepository myPageRepository;
    private VisitedPlace visitedPlace;

    /**
     * 리뷰 작성 전 방문 장소 및 방문 날짜 사전 조회
     *
     * @param usersId        JWT 토큰에서 추출된 사용자 식별자
     * @param visitedPlaceId 방문 기록 ID
     * @return 방문 장소명, 이미지, 방문일 등 사전 정보
     * @throws BusinessException 방문 기록이 없는 경우 VISITED_PLACE_NOT_FOUND
     * @throws BusinessException 본인 소유가 아닌 경우 FORBIDDEN
     */
    public ReviewPreviewResponseDto getReviewPreview(String usersId, Long visitedPlaceId) {
        VisitedPlace visitedPlace = visitedPlaceRepository.findByVisitedPlaceIdAndIsDeletedFalse(visitedPlaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VISITED_PLACE_NOT_FOUND));

        if (!visitedPlace.getUser().getUsersId().equals(usersId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return new ReviewPreviewResponseDto(visitedPlace);
    }

    /**
     * 리뷰 생성
     *
     * @param usersId    JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 별점, 내용, 이미지 URL 목록
     * @return 생성된 리뷰 정보
     * @throws BusinessException 방문 기록이 없는 경우 VISITED_PLACE_NOT_FOUND
     * @throws BusinessException 본인 소유가 아닌 경우 FORBIDDEN
     * @throws BusinessException 이미 리뷰가 존재하는 경우 DUPLICATE_REVIEW
     */
    @Transactional
    public ReviewCreateResponseDto createReview(String usersId, ReviewCreateRequestDto requestDto) {
        // 유저 확인 및 방문지 확인
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        VisitedPlace visitedPlace = visitedPlaceRepository.findByVisitedPlaceIdAndIsDeletedFalse(requestDto.getVisitedPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VISITED_PLACE_NOT_FOUND));

        // 권한 확인, 아닐 시 forbidden
        if (!visitedPlace.getUser().getUsersId().equals(usersId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 요청한 이름과 db에 있는 이름 || 요청한 날짜와 db에 있는 날짜가 일치하지 않으면 에러
        Place place = visitedPlace.getPlace();

        if (!place.getName().equals(requestDto.getPlaceName())) {
            throw new BusinessException(ErrorCode.PLACE_DATE_NOT_FOUND);
        }

        if (!visitedPlace.getVisitedAt().toLocalDate().equals(requestDto.getVisitedAt())) {
            throw new BusinessException(ErrorCode.PLACE_DATE_NOT_FOUND);
        }

        // 중복 리뷰 체크
        if (reviewRepository.existsByVisitedPlace_VisitedPlaceIdAndIsDeletedFalse(requestDto.getVisitedPlaceId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        Review review = Review.builder()
                .user(user)
                .place(place)
                .visitedPlace(visitedPlace)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .visitedDate(requestDto.getVisitedAt())
                .build();

        reviewRepository.save(review);

        // 이미지 배열로 저장 (string으로 저장하기엔 문자가 너무 많음)
        List<ReviewImage> images = new ArrayList<>();
        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            List<String> urls = requestDto.getImageUrls();
            int limit = Math.min(urls.size(), 3);
            for (int i = 0; i < limit; i++) {
                images.add(ReviewImage.builder()
                        .review(review)
                        .imageUrl(urls.get(i))
                        .sortOrder(i + 1)
                        .build());
            }
            reviewImageRepository.saveAll(images);
        }

        // 장소 평점 업데이트
        // 리뷰 조회 시 평점과 리뷰 갯수 조회 가능하게 함
        place.updateRating(requestDto.getRating());

        return new ReviewCreateResponseDto(review, images);
    }
}
