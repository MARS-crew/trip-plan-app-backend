package mars.tripplanappbackend.mypage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.mypage.dto.request.ExchangeRequestDto;
import mars.tripplanappbackend.mypage.dto.resopnse.ExchangeResponseDto;
import mars.tripplanappbackend.mypage.repository.MyPageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExchangeService {
    @Value("${spring.social.exchange.auth-key}")
    private String authKey;

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://oapi.koreaexim.go.kr")
                    .build();

    private final MyPageRepository myPageRepository;

    /**
     * 통화 코드와 금액을 받아 환율 계산 결과 반환
     *
     * @param usersId    JWT 토큰에서 추출된 사용자 식별자
     * @param requestDto 통화 코드, 변환할 금액, 변환 방향
     * @return 환율 정보 및 변환된 금액
     * 사용자를 찾을 수 없는 경우 USER_NOT_FOUND
     * 환율 조회 실패 시 EXCHANGE_RATE_FAILED
     * 지원하지 않는 통화 코드인 경우 INVALID_INPUT
     */
    public ExchangeResponseDto getExchangeRate(String usersId, ExchangeRequestDto requestDto) {
        myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String today = getRecentBusinessDay();

        try {
            List<Map> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/site/program/financial/exchangeJSON")
                            .queryParam("authkey", authKey)
                            .queryParam("searchdate", today)
                            .queryParam("data", "AP01")
                            .build())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();

            Map targetCurrency = response.stream()
                    .filter(r -> {
                        String unit = (String) r.get("cur_unit");
                        return unit != null && unit.contains(requestDto.getCurUnit());
                    })
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

            String dealBasRStr = ((String) targetCurrency.get("deal_bas_r")).replace(",", "");
            double dealBasR = Double.parseDouble(dealBasRStr);

            double convertedAmount = requestDto.isFromKrw()
                    ? requestDto.getAmount() / dealBasR
                    : requestDto.getAmount() * dealBasR;

            return new ExchangeResponseDto(
                    (String) targetCurrency.get("cur_unit"),
                    (String) targetCurrency.get("cur_nm"),
                    dealBasR,
                    convertedAmount
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("환율 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXCHANGE_RATE_FAILED);
        }
    }

    /**
     * 영업일 기준으로 날짜 계산
     * @return 최근 영업일 날짜 문자열
     */
    private String getRecentBusinessDay() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
