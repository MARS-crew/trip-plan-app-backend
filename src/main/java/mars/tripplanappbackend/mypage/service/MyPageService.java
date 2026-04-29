package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.request.EmailRequestDto;
import mars.tripplanappbackend.auth.dto.response.EmailResponseDto;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.dto.request.UpdateAgreeRequestDto;
import mars.tripplanappbackend.mypage.dto.request.UpdateProfileRequestDto;
import mars.tripplanappbackend.mypage.dto.resopnse.*;
import mars.tripplanappbackend.mypage.repository.SavedPlaceRepository;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import mars.tripplanappbackend.trip.domain.VisitedPlace;
import mars.tripplanappbackend.trip.repository.TripRepository;
import mars.tripplanappbackend.trip.repository.VisitedPlaceRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageRepository myPageRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TripRepository tripRepository;
    private final VisitedPlaceRepository visitedPlaceRepository;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 사용자 프로필 정보가 담긴 response
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
     * @param usersId    JWT 토큰에서 추출된 사용자 식별자
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

    /**
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 사용자의 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public AgreeResponseDto getAgree(String usersId) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new AgreeResponseDto(user);
    }

    /**
     *
     * @param usersId    JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 변경할 알림 설정
     * @return 변경된 알림 설정이 담긴 response
     */
    @Transactional
    public AgreeResponseDto updateAgree(String usersId, UpdateAgreeRequestDto requestDto) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateAgree(
                requestDto.getMarketingAgreed(),
                requestDto.getNightMarketingAgreed()
        );

        return new AgreeResponseDto(user);
    }

    /**
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 사용자의 계정 설정 조회
     */
    @Transactional(readOnly = true)
    public SettingResponseDto getSetting(String usersId) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new SettingResponseDto(user);
    }

    /**
     * 
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 사용자의 마이페이지 정보 조회
     */
    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(String usersId) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new MyPageResponseDto(
                user.getNickname(),
                user.getEmail(),
                tripRepository.countByUser_UserIdAndIsDeletedFalse(user.getUserId()),
                savedPlaceRepository.countByUserAndIsDeletedFalse(user),
                visitedPlaceRepository.countByUser_UserIdAndIsDeletedFalse(user.getUserId())
        );
    }

    /**
     * 내 방문 장소 목록 조회
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @return 방문 장소 목록
     */
    public List<VisitedPlaceResponseDto> getMyVisitedPlaces(String usersId) {
        List<VisitedPlace> visitedPlaces =
                visitedPlaceRepository.findByUser_UsersIdAndIsDeletedFalseOrderByVisitedAtDesc(usersId);

        return visitedPlaces.stream()
                .map(VisitedPlaceResponseDto::new)
                .toList();
    }

    /**
     *
     * 이메일 전송
     *
     * 입력된 gmail로 인증 코드 6자리를 전송한다.
     * 전송에 실패할 경우 EMAIL_SEND_FAIL 에러를 발생시킨다.
     *
     * @param requestDto 요청 보낸 이메일 정보
     * @return 이메일 전송 결과를 담은 응답 DTO
     */

    @Transactional
    public EmailResponseDto sendEmail(EmailRequestDto requestDto) {

        String email = requestDto.getEmail();
        String code = generateVerificationCode();

        String EMAIL_VERIFY_KEY = "email:verify:";
        String EMAIL_REQUEST_KEY = "email:requested:";

        //redis에 저장되는 내용 ex) email:verify: email@gmail.com
        // 5분 뒤에 알아서 삭제됨
        redisTemplate.opsForValue().set(
                EMAIL_VERIFY_KEY + email,
                code,
                5,
                TimeUnit.MINUTES
        );

        redisTemplate.opsForValue().set(
                EMAIL_REQUEST_KEY + email,
                "true",
                10,
                TimeUnit.MINUTES
        );

        // 사용자에게 전달되는 이메일 내용
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PLI 이메일 인증");
        message.setText("PLI 이메일 인증 코드: " + code);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }

        return new EmailResponseDto(email);
    }

    // 인증 코드 계산 방법
    private String generateVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}
