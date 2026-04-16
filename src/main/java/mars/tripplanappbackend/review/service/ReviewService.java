package mars.tripplanappbackend.review.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.review.dto.response.ReviewPreviewResponseDto;
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
    private final ReviewImageRepository reviewImageRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;

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
}
