package mars.tripplanappbackend.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import mars.tripplanappbackend.trip.domain.Trip;
import mars.tripplanappbackend.trip.domain.TripSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 내 여행 상세 화면 진입 시 사용하는 일차별 일정 리스트 응답 DTO입니다.
 * 여행 기본 정보와 함께 여행 기간 전체 날짜를 일차별 섹션으로 내려주고,
 * 각 일정의 진행 상태/방문 상태를 함께 반환해 프론트엔드가 추가 계산 없이 즉시 렌더링할 수 있도록 합니다.
 */
@Getter
@Builder
@Schema(description = "내 여행 상세 일정 리스트 조회 응답 DTO")
public class MyTripScheduleListResponseDto {

    @Schema(description = "여행 PK", example = "7")
    private Long tripId;

    @Schema(description = "여행 제목", example = "오사카 여행")
    private String tripTitle;

    @Schema(description = "여행 시작일", example = "2026-04-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-04-14")
    private LocalDate endDate;

    @Schema(description = "여행 총 일수", example = "5")
    private long tripDayCount;

    @Schema(description = "현재 시각 기준 진행 중인 일정 존재 여부", example = "true")
    private boolean hasOngoingSchedule;

    @Schema(description = "여행 기간 전체를 기준으로 구성된 일차별 일정 섹션")
    private List<DayScheduleResponseDto> dailySchedules;

    /**
     * 여행 엔티티와 계산된 화면 상태를 결합해 상세 일정 리스트 응답 DTO를 생성합니다.
     *
     * @param trip 조회 대상 여행 엔티티
     * @param tripDayCount 여행 총 일수
     * @param hasOngoingSchedule 현재 진행 중 일정 존재 여부
     * @param dailySchedules 여행 기간 전체 기준의 일차별 일정 섹션 목록
     * @return 내 여행 상세 일정 리스트 조회 응답 DTO
     */
    public static MyTripScheduleListResponseDto of(
            Trip trip,
            long tripDayCount,
            boolean hasOngoingSchedule,
            List<DayScheduleResponseDto> dailySchedules
    ) {
        return MyTripScheduleListResponseDto.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripDayCount(tripDayCount)
                .hasOngoingSchedule(hasOngoingSchedule)
                .dailySchedules(dailySchedules)
                .build();
    }

    /**
     * 여행 상세 화면의 "일차(1일차/2일차/...)" 단위를 표현하는 DTO입니다.
     * 일정이 없는 날짜도 빈 schedules 배열로 내려주어 화면 구조를 고정할 수 있게 합니다.
     */
    @Getter
    @Builder
    @Schema(description = "내 여행 상세 화면의 일차별 일정 섹션")
    public static class DayScheduleResponseDto {

        @Schema(description = "여행 기준 일차 번호", example = "1")
        private int dayNo;

        @Schema(description = "해당 일차 일정 날짜", example = "2026-04-10")
        private LocalDate scheduleDate;

        @Schema(description = "일차 라벨", example = "1일차")
        private String dayLabel;

        @Schema(description = "해당 날짜 일정 개수", example = "2")
        private int scheduleCount;

        @Schema(description = "해당 날짜 일정 목록 (없으면 빈 배열)")
        private List<ScheduleItemResponseDto> schedules;

        /**
         * 일차별 일정 섹션 응답 DTO를 생성합니다.
         *
         * @param dayNo 여행 기준 일차 번호
         * @param scheduleDate 섹션 기준 날짜
         * @param schedules 해당 날짜 일정 목록
         * @return 일차별 일정 섹션 응답 DTO
         */
        public static DayScheduleResponseDto of(
                int dayNo,
                LocalDate scheduleDate,
                List<ScheduleItemResponseDto> schedules
        ) {
            return DayScheduleResponseDto.builder()
                    .dayNo(dayNo)
                    .scheduleDate(scheduleDate)
                    .dayLabel(dayNo + "일차")
                    .scheduleCount(schedules.size())
                    .schedules(schedules)
                    .build();
        }
    }

    /**
     * 여행 상세 화면 일정 카드 1건을 표현하는 DTO입니다.
     * 화면에서 바로 사용할 수 있도록 일정 순번, 시간 정보, 진행 상태, 방문지 저장 가능 여부를 함께 포함합니다.
     */
    @Getter
    @Builder
    @Schema(description = "내 여행 상세 화면 일정 카드 응답 DTO")
    public static class ScheduleItemResponseDto {

        @Schema(description = "해당 일차 내 일정 순번", example = "1")
        private int scheduleOrder;

        @Schema(description = "여행 일정 PK", example = "21")
        private Long tripScheduleId;

        @Schema(description = "연결된 장소 PK", example = "7", nullable = true)
        private Long placeId;

        @Schema(description = "일정 제목", example = "공항 이동")
        private String title;

        @Schema(description = "일정 장소명", example = "간사이 국제공항", nullable = true)
        private String placeName;

        @Schema(description = "일정 주소", example = "오사카 공항", nullable = true)
        private String address;

        @Schema(description = "일정 메모", example = "체크인 2시간 전 도착", nullable = true)
        private String memo;

        @Schema(description = "일정 시작 시간", example = "09:00:00")
        private LocalTime startTime;

        @Schema(description = "일정 종료 시간", example = "10:00:00")
        private LocalTime endTime;

        @Schema(description = "현재 시각 기준 진행 중 일정 여부", example = "false")
        private boolean isOngoing;

        @Schema(description = "진행 중 일정 안내 문구 (진행 중일 때만 노출)", example = "현재 진행되는 일정입니다.", nullable = true)
        private String ongoingMessage;

        @Schema(description = "이미 방문 기록으로 저장된 장소 여부", example = "false")
        private boolean visited;

        @Schema(description = "방문지 저장 버튼 노출 가능 여부", example = "true")
        private boolean canAddVisitedPlace;

        /**
         * 여행 일정 엔티티와 화면 상태 계산값을 바탕으로 일정 카드 응답 DTO를 생성합니다.
         *
         * @param tripSchedule 여행 일정 엔티티
         * @param scheduleOrder 해당 일차 내 일정 순번
         * @param address 화면에서 사용할 기준 주소(직접 입력 주소 우선)
         * @param isOngoing 현재 진행 중 여부
         * @param visited 방문 기록 여부
         * @param canAddVisitedPlace 방문지 저장 버튼 노출 가능 여부
         * @return 일정 카드 응답 DTO
         */
        public static ScheduleItemResponseDto from(
                TripSchedule tripSchedule,
                int scheduleOrder,
                String address,
                boolean isOngoing,
                boolean visited,
                boolean canAddVisitedPlace
        ) {
            return ScheduleItemResponseDto.builder()
                    .scheduleOrder(scheduleOrder)
                    .tripScheduleId(tripSchedule.getTripScheduleId())
                    .placeId(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getPlaceId() : null)
                    .title(tripSchedule.getTitle())
                    .placeName(tripSchedule.getPlace() != null ? tripSchedule.getPlace().getName() : null)
                    .address(address)
                    .memo(tripSchedule.getMemo())
                    .startTime(tripSchedule.getStartTime())
                    .endTime(tripSchedule.getEndTime())
                    .isOngoing(isOngoing)
                    .ongoingMessage(isOngoing ? "현재 진행되는 일정입니다." : null)
                    .visited(visited)
                    .canAddVisitedPlace(canAddVisitedPlace)
                    .build();
        }
    }
}
