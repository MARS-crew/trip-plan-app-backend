package mars.tripplanappbackend.auth.service;

import mars.tripplanappbackend.auth.dto.request.*;
import mars.tripplanappbackend.auth.dto.response.*;
import mars.tripplanappbackend.auth.dto.response.CheckIdResponseDto;
import mars.tripplanappbackend.auth.dto.response.LoginResponseDto;
import mars.tripplanappbackend.auth.dto.response.SignupResponseDto;
import mars.tripplanappbackend.auth.dto.response.TokenReissueResponseDto;
import mars.tripplanappbackend.auth.enums.WithdrawalReasonType;
import mars.tripplanappbackend.global.config.auth.JwtProvider;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.enums.UseYnEnum;
import mars.tripplanappbackend.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.LoginType;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MyPageRepository myPageRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 회원가입 처리
     *
     * 소셜 가입은 비밀번호 없이 진행되므로 loginType이 LOCAL인 경우에만 비밀번호를 검증한다.
     * 비밀번호는 평문 저장을 방지하기 위해 BCrypt로 암호화 후 저장한다.
     *
     * @param requestDto 회원가입 요청 정보
     * @return 저장된 사용자 정보를 기반으로 생성된 응답 DTO
     */
    @Transactional
    public SignupResponseDto signUp(SignupRequestDto requestDto) {

        String encodedPassword = null;

        if (requestDto.getLoginType() == null || requestDto.getLoginType() == LoginType.LOCAL) {
            // 일반 가입은 비밀번호 필수
            if (requestDto.getPassword() == null || requestDto.getPasswordConfirm() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            // 클라이언트 입력 실수로 서로 다른 비밀번호가 전달되는 경우를 방지
            if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
                throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
            }
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        }

        User user = requestDto.toEntity(encodedPassword);
        User savedUser = myPageRepository.save(user);
        return SignupResponseDto.from(savedUser);
    }

    /**
     * 로그인 처리
     *
     * 사용자의 ID로 계정을 조회하고, 저장된 해시 비밀번호와 입력 비밀번호를 비교한다.
     * 소셜 가입 회원이 일반 로그인 API를 호출하는 경우 예외를 던진다.
     * 로그인 성공 시 AccessToken + RefreshToken을 발급하며,
     * RefreshToken은 재발급을 위해 DB에 저장한다.
     *
     * @param requestDto 로그인 요청 정보
     * @return 로그인 성공 시 토큰 및 사용자 정보
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {

        User user = myPageRepository.findByUsersId(requestDto.getUsersId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 소셜 가입 회원이 일반 로그인 시도 방지
        if (user.getLoginType() != LoginType.LOCAL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // BCrypt 해시 비교를 통해 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getUsersId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.createRefreshToken();
        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(14));

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userDetails(SignupResponseDto.from(user))
                .build();
    }

    /**
     * AccessToken 재발급
     *
     * 클라이언트가 RefreshToken을 전달하면
     * 해당 토큰이 DB에 저장된 값과 일치하는지 확인하고,
     * 만료 여부와 토큰 유효성을 검증한 뒤 새로운 토큰을 발급한다.
     * RefreshToken은 보안을 위해 재발급 시 새로운 값으로 갱신한다.
     *
     * @param request RefreshToken 재발급 요청
     * @return 새 AccessToken 및 RefreshToken
     */
    @Transactional
    public TokenReissueResponseDto reissue(TokenReissueRequestDto request) {

        String refreshToken = request.getRefreshToken();

        User user = myPageRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        // 서버에 저장된 만료시간 기준으로 RefreshToken 만료 여부 확인
        if (user.isRefreshTokenExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // JWT 자체 서명 및 구조 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(
                user.getUsersId(),
                user.getEmail(),
                user.getRole().name()
        );

        // RefreshToken 재사용을 방지하기 위해 새 토큰으로 교체
        String newRefreshToken = jwtProvider.createRefreshToken();
        user.updateRefreshToken(newRefreshToken, LocalDateTime.now().plusDays(14));

        return new TokenReissueResponseDto(newAccessToken, newRefreshToken);
    }

    /**
     * 아이디 중복 확인
     *
     * 회원가입 전 사용자가 입력한 아이디의 중복 여부를 확인한다.
     * 이미 존재하는 아이디인 경우 예외를 발생시켜
     * 클라이언트에 즉시 알린다.
     *
     * @param usersId 중복 확인할 아이디
     * @return 사용 가능 여부 및 아이디 정보를 담은 응답 DTO
     */
    public CheckIdResponseDto checkUsersIdDuplicate(String usersId) {
        if (myPageRepository.existsByUsersId(usersId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USER);
        }
        return new CheckIdResponseDto(usersId, "사용 가능한 아이디입니다.");
    }

    /**
     * 아이디 찾기
     *
     * 사용자가 입력한 닉네임과 이메일로 계정을 조회한다.
     * 일치하는 계정이 없을 경우 예외를 발생시킨다.
     *
     * @param requestDto 닉네임 및 이메일 요청 정보
     * @return 조회된 사용자 아이디를 담은 응답 DTO
     */
    public FindIdResponseDto findUsersId(FindIdRequestDto requestDto) {
        User user = myPageRepository.findByNicknameAndEmail(requestDto.getNickname(), requestDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new FindIdResponseDto(user.getUsersId());
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

    /**
     * 이메일 인증
     *
     * 전송된 이메일과 인증코드를 입력받고
     * Redis에 저장된 인증 코드와 비교하여 일치하는지 확인한다.
     * 일치하지 않으면 INVALID_EMAIL_CODE 에러 처리
     *
     * @param requestDto 전송 보낸 이메일, 전송된 인증 코드
     * @return 인증된 이메일, 인증 여부
     */
    @Transactional
    public EmailVerifyResponseDto verifyEmailCode(EmailVerifyRequestDto requestDto) {

        String email = requestDto.getEmail();
        String EMAIL_VERIFY_KEY = "email:verify:";
        String EMAIL_REQUEST_KEY = "email:requested:";

        Boolean isRequested = redisTemplate.hasKey(EMAIL_REQUEST_KEY + email);
        if (!Boolean.TRUE.equals(isRequested)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_REQUEST);
        }

        String savedCode = redisTemplate.opsForValue().get(EMAIL_VERIFY_KEY + email);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        if (!savedCode.equals(requestDto.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_CODE);
        }

        redisTemplate.delete(EMAIL_VERIFY_KEY + email);
        redisTemplate.delete(EMAIL_REQUEST_KEY + email);

        User user = myPageRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setEmailVerified(UseYnEnum.Y);

        return new EmailVerifyResponseDto(email, UseYnEnum.Y);
    }

    /**
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * refreshToken을 null로 업데이트하여 로그아웃 처리
     */
    @Transactional
    public void logout(String usersId) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateRefreshToken(null, null);
    }

    /**
     *
     * @param usersId JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 탈퇴 유형, 기타 탈퇴 사유
     */
    @Transactional
    public void withdraw(String usersId, WithdrawRequestDto requestDto) {
        User user = myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기타 선택 시 텍스트 필수
        if (requestDto.getReasonType() == WithdrawalReasonType.OTHER
                && (requestDto.getReasonText() == null || requestDto.getReasonText().isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 기타가 아닌 경우 reasonText는 null 처리
        String reasonText = requestDto.getReasonType() == WithdrawalReasonType.OTHER
                ? requestDto.getReasonText()
                : null;

        user.withdraw(requestDto.getReasonType().name(), reasonText);
    }

    /**
     * 임시 비밀번호 발급을 위한 이메일 인증 코드 전송
     *
     * @param requestDto 유저 로그인 아이디, 이메일
     * @return 작성한 유저 로그인 아이디, 이메일
     */
    @Transactional
    public PasswordEmailResponseDto sendPasswordResetEmail(PasswordEmailRequestDto requestDto) {

        String EMAIL_VERIFY_KEY = "password:verify:";
        String EMAIL_REQUEST_KEY = "password:requested:";

        // 유저 검증 (아이디 + 이메일)
        User user = myPageRepository.findByUsersIdAndEmail(
                requestDto.getUsersId(),
                requestDto.getEmail()
        ).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String email = user.getEmail();
        String usersId = user.getUsersId();
        String code = generateVerificationCode();

        // Redis 저장
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

        // 3. 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("비밀번호 재설정 인증 코드");
        message.setText("인증 코드: " + code);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAIL);
        }

        return new PasswordEmailResponseDto(usersId, email);
    }


}