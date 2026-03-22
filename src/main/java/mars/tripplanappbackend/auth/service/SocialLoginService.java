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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String naverClientSecret;

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
        SocialUserInfoResponseDto userInfo = getUserInfo(loginType, accessToken);

        // 이미 로그인했던 사람
        return myPageRepository
                .findByLoginTypeAndSocialProviderId(loginType, userInfo.getSocialProviderId())
                .map(user -> SocialLoginResponseDto.builder()
                        .registered(true)
                        .nextAction("login")
                        .login(issueLoginToken(user))
                        .signupResponse(null)
                        .build())

                // 신규 가입자
                .orElseGet(() -> SocialLoginResponseDto.builder()
                        .registered(false)
                        .nextAction("signup")
                        .login(null)
                        .signupResponse(SocialSignupResponseDto.builder()
                                .loginType(loginType)
                                .socialProviderId(userInfo.getSocialProviderId())
                                .nickname(userInfo.getNickname())
                                .email(userInfo.getEmail())
                                .name(userInfo.getName())
                                .gender(toGender(loginType, userInfo.getGender()))
                                .birth(toBirthDate(userInfo.getBirthYear(), userInfo.getBirthday()))
                                .build())
                        .build());
    }

    /**
     * loginType에 따라 각 소셜 API를 호출하고 공통 DTO로 변환해 반환한다.
     * 각 소셜마다 오는 response 값의 형태가 달라서 이렇게 처리함.
     *
     * @param loginType   소셜 제공자 타입
     * @param accessToken 소셜 액세스 토큰
     * @return 공통 사용자 정보 DTO
     */
    private SocialUserInfoResponseDto getUserInfo(LoginType loginType, String accessToken) {
        return switch (loginType) {
            case KAKAO -> getKakaoUserInfo(accessToken);
            case NAVER -> getNaverUserInfo(accessToken);
            default    -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }

    /**
     * 카카오 API 서버에서 사용자 프로필 정보를 조회하고 공통 DTO로 변환한다.
     *
     * @param accessToken 프론트엔드로부터 전달받은 카카오 액세스 토큰
     * @return 공통 사용자 정보 DTO
     * @throws BusinessException INVALID_TOKEN - 토큰이 유효하지 않거나 API 호출 실패 시
     */
    private SocialUserInfoResponseDto getKakaoUserInfo(String accessToken) {
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

            KakaoUserInfoResponseDto.KakaoAccount account = body.getKakaoAccount();

            return SocialUserInfoResponseDto.builder()
                    .socialProviderId(String.valueOf(body.getId()))
                    .nickname(account.getProfile().getNickname())
                    .email(account.getEmail())
                    .name(account.getName())
                    .gender(account.getGender())
                    .birthYear(account.getBirthYear())
                    .birthday(account.getBirthday())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 네이버 API 서버에서 사용자 프로필 정보를 조회하고 공통 DTO로 변환한다.
     *
     * @param accessToken 프론트엔드로부터 전달받은 네이버 액세스 토큰
     * @return 공통 사용자 정보 DTO
     * @throws BusinessException INVALID_TOKEN - 토큰이 유효하지 않거나 API 호출 실패 시
     */
    private SocialUserInfoResponseDto getNaverUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);

        try {
            ResponseEntity<NaverUserInfoResponseDto> response = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    NaverUserInfoResponseDto.class
            );

            NaverUserInfoResponseDto body = response.getBody();
            if (body == null) throw new BusinessException(ErrorCode.INVALID_TOKEN);

            NaverUserInfoResponseDto.NaverAccount account = body.getResponse();

            return SocialUserInfoResponseDto.builder()
                    .socialProviderId(account.getId())
                    .nickname(account.getNickname())
                    .email(account.getEmail())
                    .name(account.getName())
                    .gender(account.getGender())
                    .birthYear(account.getBirthYear())
                    .birthday(account.getBirthday())
                    .build();

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
     * 카카오: "female"/"male", 네이버: "F"/"M" 형식으로 제공되며,
     * 매칭되지 않는 값은 ETC로 처리한다.
     *
     * @param loginType 소셜 제공자 타입
     * @param gender    소셜 제공자가 반환한 성별 문자열
     * @return 변환된 Gender Enum, 값이 없으면 null
     */
    private Gender toGender(LoginType loginType, String gender) {
        if (gender == null) return null;
        return switch (loginType) {
            case NAVER -> switch (gender.toUpperCase()) {
                case "F" -> Gender.FEMALE;
                case "M" -> Gender.MALE;
                default  -> Gender.ETC;
            };
            default -> switch (gender.toLowerCase()) {
                case "female" -> Gender.FEMALE;
                case "male"   -> Gender.MALE;
                default       -> Gender.ETC;
            };
        };
    }

    /**
     * 소셜 제공자에서 받은 출생 연도와 생일을 조합해 LocalDate로 변환한다.
     *
     * 카카오는 "0711", 네이버는 "07-11" 형식으로 제공하므로 하이픈을 제거 후 파싱한다.
     *
     * @param birthYear 출생 연도 (YYYY)
     * @param birthday  생일 (MMDD 또는 MM-DD)
     * @return 변환된 LocalDate, 값이 없거나 파싱 실패 시 null
     */
    private LocalDate toBirthDate(String birthYear, String birthday) {
        if (birthYear == null || birthday == null) return null;
        try {
            String cleanBirthday = birthday.replace("-", "");
            int month = Integer.parseInt(cleanBirthday.substring(0, 2));
            int day   = Integer.parseInt(cleanBirthday.substring(2, 4));
            return LocalDate.of(Integer.parseInt(birthYear), month, day);
        } catch (Exception e) {
            return null;
        }
    }
}