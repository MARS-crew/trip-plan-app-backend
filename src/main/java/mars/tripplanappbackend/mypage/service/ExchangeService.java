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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public ExchangeResponseDto getExchangeRate(String usersId, ExchangeRequestDto requestDto) {
        // 사용자 검증
        myPageRepository.findByUsersId(usersId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 애플리케이션 기동은 유지하되, 환율 API 키가 비어있으면 기능 호출 시 명확히 실패시킨다.
        if (authKey == null || authKey.isBlank()) {
            log.error("Exchange API key is missing. Please set EXCHANGE_API_KEY.");
            throw new BusinessException(ErrorCode.EXCHANGE_RATE_FAILED);
        }

        String today = getRecentBusinessDay();

        try {
            // 2. 환율 API 호출 (공휴일 대비 최대 5일 재시도)
            List<Map> response = null;
            String searchDate = today;

            for (int i = 0; i < 5; i++) {
                final String dateToSearch = searchDate;

                response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/site/program/financial/exchangeJSON")
                                .queryParam("authkey", authKey)
                                .queryParam("searchdate", dateToSearch)
                                .queryParam("data", "AP01")
                                .build())
                        .retrieve()
                        .bodyToFlux(Map.class)
                        .collectList()
                        .block();

                if (response != null && !response.isEmpty()) break;

                log.warn("환율 데이터 없음 [{}], 이전 영업일로 재시도", searchDate);
                searchDate = getPreviousBusinessDay(searchDate);
            }

            if (response == null || response.isEmpty()) {
                throw new BusinessException(ErrorCode.EXCHANGE_RATE_FAILED);
            }

            // 통화 찾기
            Map targetCurrency = response.stream()
                    .filter(r -> {
                        String unit = (String) r.get("cur_unit");
                        return unit != null &&
                                (unit.equals(requestDto.getCurUnit())
                                        || unit.startsWith(requestDto.getCurUnit() + "("));
                    })
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

            String curUnit = (String) targetCurrency.get("cur_unit");
            String curNm = (String) targetCurrency.get("cur_nm");

            // 환율 파싱 (원 기준 환율)
            String dealBasRStr = ((String) targetCurrency.get("deal_bas_r")).replace(",", "");
            BigDecimal rate = new BigDecimal(dealBasRStr);

            BigDecimal amount = BigDecimal.valueOf(requestDto.getAmount());

            // 100단위 통화 처리 (JPY 등)
            boolean isHundredUnit = curUnit.endsWith("(100)");

            BigDecimal convertedAmount;

            if (requestDto.isFromKrw()) {
                // KRW → 외화
                convertedAmount = amount.divide(rate, 6, RoundingMode.HALF_UP);
                if (isHundredUnit) {
                    convertedAmount = convertedAmount.multiply(BigDecimal.valueOf(100));
                }
            } else {
                // 외화 → KRW
                convertedAmount = amount.multiply(rate);
                if (isHundredUnit) {
                    convertedAmount = convertedAmount.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                }
            }

            // 통화별 소수점 처리
            if (curUnit.startsWith("JPY")) {
                convertedAmount = convertedAmount.setScale(0, RoundingMode.HALF_UP); // 엔화 → 정수
            } else if (curUnit.startsWith("USD") || curUnit.startsWith("EUR")) {
                convertedAmount = convertedAmount.setScale(2, RoundingMode.HALF_UP); // 달러/유로 → 소수점 2자리
            }

            // 프론트 표시용 환율 (1 KRW 기준)
            BigDecimal displayRate = isHundredUnit
                    ? BigDecimal.valueOf(100).divide(rate, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP);

            return new ExchangeResponseDto(
                    curUnit.replace("(100)", ""),
                    curNm,
                    displayRate.doubleValue(),
                    convertedAmount.doubleValue(),
                    searchDate
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("환율 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXCHANGE_RATE_FAILED);
        }
    }

    /**
     * 오늘 기준 가장 최근 영업일 반환 (토/일 제외)
     */
    private String getRecentBusinessDay() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 주어진 날짜 기준 이전 영업일 반환 (토/일 제외)
     */
    private String getPreviousBusinessDay(String currentDate) {
        LocalDate date = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
                .minusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
