package mars.tripplanappbackend.search.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.search.event.SearchImageEnrichmentRequestedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 검색 결과 저장 커밋 이후 이미지 보강을 별도 스레드에서 시작합니다.
 */
@Component
@RequiredArgsConstructor
public class SearchImageEnrichmentEventListener {

    private final SearchImageEnrichmentService searchImageEnrichmentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SearchImageEnrichmentRequestedEvent event) {
        searchImageEnrichmentService.enrichMissingImages(event.tasks());
    }
}
