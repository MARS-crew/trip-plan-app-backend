package mars.tripplanappbackend.review.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.review.domain.Review;
import mars.tripplanappbackend.review.dto.request.ReviewCreateRequestDto;
import mars.tripplanappbackend.review.dto.response.ReviewCreateResponseDto;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MyPageRepository myPageRepository;
    private final PlaceRepository placeRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;

    /**
     * 리뷰 생성
     *
     * @param usersId    JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 ID
     */
    @Transactional
    public ReviewCreateResponseDto createReview(String usersId, ReviewCreateRequestDto requestDto) {

        // 유저 조회
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 장소 조회
        Place place = placeRepository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        // 방문 기록 조회
        VisitedPlace visitedPlace = visitedPlaceRepository.findById(requestDto.getVisitedPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VISITED_PLACE_NOT_FOUND));

        // 검증
        if (!visitedPlace.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (!visitedPlace.getPlace().getPlaceId().equals(place.getPlaceId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Review review = requestDto.toEntity(user, place, visitedPlace);

        reviewRepository.save(review);

        return new ReviewCreateResponseDto(review.getReviewId());
    }
}