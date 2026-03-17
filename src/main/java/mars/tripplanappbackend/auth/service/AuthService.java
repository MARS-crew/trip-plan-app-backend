package mars.tripplanappbackend.auth.service;

import mars.tripplanappbackend.auth.dto.request.FindIdRequestDto;
import mars.tripplanappbackend.auth.dto.request.LoginRequestDto;
import mars.tripplanappbackend.auth.dto.request.SignupRequestDto;
import mars.tripplanappbackend.auth.dto.request.TokenReissueRequestDto;
import mars.tripplanappbackend.auth.dto.response.*;
import mars.tripplanappbackend.domain.User;
import mars.tripplanappbackend.auth.repository.UserRepository;
import mars.tripplanappbackend.global.config.auth.JwtProvider;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입 처리
     *
     * 비밀번호는 평문 저장을 방지하기 위해 BCrypt로 암호화 후 저장한다.
     * 또한 서비스 레이어에서 비밀번호 확인 값을 비교하여
     * 클라이언트의 잘못된 요청을 사전에 차단한다.
     *
     * @param requestDto 회원가입 요청 정보
     * @return 저장된 사용자 정보를 기반으로 생성된 응답 DTO
     */
    @Transactional
    public SignupResponseDto signUp(SignupRequestDto requestDto) {

        // 클라이언트 입력 실수로 서로 다른 비밀번호가 전달되는 경우를 방지
        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호는 보안을 위해 해싱 처리 후 저장
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // DTO → Entity 변환을 DTO 내부에서 처리하여 서비스 로직 단순화
        User user = requestDto.toEntity(encodedPassword);

        User savedUser = userRepository.save(user);

        return SignupResponseDto.from(savedUser);
    }

    /**
     * 로그인 처리
     *
     * 사용자의 ID로 계정을 조회하고,
     * 저장된 해시 비밀번호와 입력 비밀번호를 비교한다.
     *
     * 로그인 성공 시 AccessToken + RefreshToken을 발급하며,
     * RefreshToken은 재발급을 위해 DB에 저장한다.
     *
     * @param requestDto 로그인 요청 정보
     * @return 로그인 성공 시 토큰 및 사용자 정보
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {

        // 존재하지 않는 사용자 로그인 시도 방지
        User user = userRepository.findByUsersId(requestDto.getUsersId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // BCrypt 해시 비교를 통해 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // AccessToken은 사용자 인증용으로 사용
        String accessToken = jwtProvider.createAccessToken(
                user.getUsersId(),
                user.getEmail(),
                user.getRole().name()
        );

        // RefreshToken은 AccessToken 재발급을 위한 용도로 생성
        String refreshToken = jwtProvider.createRefreshToken();

        // RefreshToken 탈취 위험을 줄이기 위해 유효기간을 DB에 함께 저장
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
     *
     * RefreshToken은 보안을 위해 재발급 시 새로운 값으로 갱신한다.
     *
     * @param request RefreshToken 재발급 요청
     * @return 새 AccessToken 및 RefreshToken
     */
    @Transactional
    public TokenReissueResponseDto reissue(TokenReissueRequestDto request) {

        String refreshToken = request.getRefreshToken();

        // DB에 저장된 RefreshToken 기준으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        // 서버에 저장된 만료시간 기준으로 RefreshToken 만료 여부 확인
        if (user.isRefreshTokenExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // JWT 자체 서명 및 구조 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 새로운 AccessToken 발급
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
        if (userRepository.existsByUsersId(usersId)) {
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
        User user = userRepository.findByNicknameAndEmail(requestDto.getNickname(), requestDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new FindIdResponseDto(user.getUsersId());
    }
}