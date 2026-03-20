package mars.tripplanappbackend.user.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.user.repository.SavedPlaceRepository;
import mars.tripplanappbackend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SavedPlaceRepository savedPlaceRepository;
}
