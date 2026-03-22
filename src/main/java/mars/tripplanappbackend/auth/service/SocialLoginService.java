package mars.tripplanappbackend.auth.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.auth.dto.response.*;
import mars.tripplanappbackend.global.config.auth.JwtProvider;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.domain.User;
import mars.tripplanappbackend.mypage.enums.Gender;
import mars.tripplanappbackend.mypage.enums.LoginType;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLoginService {
    private final MyPageRepository myPageRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * 소셜 로그인 공통 진입점.
     *
     * 소셜 액세스 토큰으로 사용자 정보를 조회한 뒤, DB에 등록된 회원인지 확인한다.
     * 기존 회원이면 JWT를 발급해 로그인 처리하고,
     * 신규 회원이면 소셜에서 받은 프리셋 데이터를 반환해 회원가입 페이지로 유도한다.
     *
     * @param loginType   소셜 제공자 타입 (KAKAO, NAVER, GOOGLE)
     * @param accessToken 프론트엔드로부터 전달받은 소셜 액세스 토큰
     * @return registered true → 로그인 토큰 정보, false → 회원가입용 프리셋 데이터
     */
    @Transactional
    public SocialLoginResponseDto socialLogin(LoginType loginType, String accessToken) {
        KakaoUserInfoResponseDto userInfo = getKakaoUserInfo(accessToken);
        String socialProviderId = String.valueOf(userInfo.getId());

        // 기존에 로그인했떤 사람의 경우
        return myPageRepository
                .findByLoginTypeAndSocialProviderId(loginType, socialProviderId)
                .map(user -> SocialLoginResponseDto.builder()
                        .registered(true)
                        .nextAction("login")
                        .login(issueLoginToken(user))
                        .signupResponse(null)
                        .build())

                // 신규 회원일 경우
                .orElseGet(() -> {
                    KakaoUserInfoResponseDto.KakaoAccount account = userInfo.getKakaoAccount();
                    return SocialLoginResponseDto.builder()
                            .registered(false)
                            .nextAction("signup")
                            .login(null)
                            .signupResponse(SocialSignupResponseDto.builder()
                                    .loginType(loginType)
                                    .socialProviderId(socialProviderId)
                                    .nickname(account.getProfile().getNickname())
                                    .email(account.getEmail())
                                    .name(account.getName())
                                    .gender(toGender(account.getGender()))
                                    .birth(toBirthDate(account.getBirthYear(), account.getBirthday()))
                                    .build())
                            .build();
                });
    }

    /**
     * 카카오 API 서버에서 사용자 프로필 정보를 조회한다.
     *
     * @param accessToken 프론트엔드로부터 전달받은 카카오 액세스 토큰
     * @return 카카오 사용자 정보 응답 DTO
     * @throws BusinessException INVALID_TOKEN - 토큰이 유효하지 않거나 API 호출 실패 시
     */
    private KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoUserInfoResponseDto.class
            );

            KakaoUserInfoResponseDto body = response.getBody();
            if (body == null) throw new BusinessException(ErrorCode.INVALID_TOKEN);
            return body;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 기존 회원에게 JWT를 발급하고 Refresh 토큰을 갱신한다.
     *
     * Refresh 토큰은 재사용을 방지하기 위해 로그인 시마다 새 값으로 교체하며,
     * 만료 기간은 발급 시점으로부터 14일로 설정한다.
     *
     * @param user 조회된 사용자 엔티티
     * @return 새로 발급된 AccessToken, RefreshToken 및 사용자 기본 정보
     */
    private LoginResponseDto issueLoginToken(User user) {
        String accessToken = jwtProvider.createAccessToken(
                user.getUsersId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken();

        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(14));

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userDetails(SignupResponseDto.from(user))
                .build();
    }

    /**
     * 소셜 제공자에서 받은 성별 문자열을 서비스 내부 Gender Enum으로 변환한다.
     *
     * 카카오는 "female"/"male" 형식으로 제공하며,
     * 매칭되지 않는 값은 ETC로 처리한다.
     *
     * @param gender 소셜 제공자가 반환한 성별 문자열
     * @return 변환된 Gender Enum, 값이 없으면 null
     */
    private Gender toGender(String gender) {
        if (gender == null) return null;
        return switch (gender.toLowerCase()) {
            case "female" -> Gender.FEMALE;
            case "male" -> Gender.MALE;
            default -> Gender.ETC;
        };
    }


    /**
     * 소셜 제공자에서 받은 출생 연도와 생일을 조합해 LocalDate로 변환한다.
     *
     * 카카오는 출생 연도(YYYY)와 생일(MMDD)을 별도 필드로 제공하므로 직접 조합이 필요하다.
     *
     * @param birthYear 출생 연도 (YYYY)
     * @param birthday  생일 (MMDD)
     * @return 변환된 LocalDate, 값이 없거나 파싱 실패 시 null
     */
    private LocalDate toBirthDate(String birthYear, String birthday) {
        if (birthYear == null || birthday == null) return null;
        try {
            int month = Integer.parseInt(birthday.substring(0, 2));
            int day = Integer.parseInt(birthday.substring(2, 4));
            return LocalDate.of(Integer.parseInt(birthYear), month, day);
        } catch (Exception e) {
            return null;
        }
    }
}