package mars.tripplanappbackend.user.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
}
