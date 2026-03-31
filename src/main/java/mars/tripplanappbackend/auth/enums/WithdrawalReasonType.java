package mars.tripplanappbackend.auth.enums;

public enum WithdrawalReasonType {
    NOT_ENOUGH_ACCESS,    // 앱에 잘 접속하지 않아요
    LOW_REVIEW_TRUST,     // 리뷰의 신뢰성이 떨어져요
    INAPPROPRIATE_TRIP,   // 여행지 추천이 적당하지 않아요
    OTHER                 // 기타
}