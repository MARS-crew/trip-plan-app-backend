package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.dto.resopnse.MyProfileResponseDto;
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

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회
     *
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 사용자 프로필 정보가 담긴 response
     *
     * 사용자를 찾을 수 없는 경우 USER_NOT_FOUND
     */
    @Transactional(readOnly = true)
    public MyProfileResponseDto getMyProfile(String usersId) {

        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new MyProfileResponseDto(user);
    }
}
