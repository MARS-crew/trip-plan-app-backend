package mars.tripplanappbackend.review.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.review.repository.ReviewImageRepository;
import mars.tripplanappbackend.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
}
