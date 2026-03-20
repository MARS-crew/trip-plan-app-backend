package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
}
