package mars.tripplanappbackend.search.event;

import java.util.List;

/**
 * 검색 응답 트랜잭션이 끝난 뒤 장소 이미지를 비동기로 보강하기 위한 이벤트입니다.
 */
public record SearchImageEnrichmentRequestedEvent(List<Task> tasks) {

    public SearchImageEnrichmentRequestedEvent {
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }

    public record Task(Long placeId, String photoName) {
    }
}
