package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.dto.request.UpdateProfileRequestDto;
import mars.tripplanappbackend.mypage.dto.resopnse.MyProfileResponseDto;
import mars.tripplanappbackend.mypage.dto.resopnse.UpdateProfileResponseDto;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

    /**
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 프로필 정보 수정 request
     * @return 변경된 값이 담긴 response
     *
     *
     */
    @Transactional
    public UpdateProfileResponseDto updateProfile(String usersId, UpdateProfileRequestDto requestDto) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String password = requestDto.getPassword();
        String confirm = requestDto.getPasswordConfirm();

        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getBirth(),
                requestDto.getGender(),
                requestDto.getCountryCode()
        );

        // 비밀번호가 입력되면 변경
        if (password != null || confirm != null) {

            if (password == null || confirm == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            if (password.isBlank() || confirm.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            if (!password.equals(confirm)) {
                throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
            }

            user.updatePassword(passwordEncoder.encode(password));
        }

        // 비밀번호 null 처리
        return new UpdateProfileResponseDto(user);
    }

}
