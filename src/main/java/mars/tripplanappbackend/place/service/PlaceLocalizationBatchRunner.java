package mars.tripplanappbackend.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceLocalizationBatchRunner implements ApplicationRunner {

    @Value("${place.localization.run-on-startup:false}")
    private boolean runOnStartup;

    @Value("${place.localization.batch-size:200}")
    private int batchSize;

    private final PlaceLocalizationBatchService placeLocalizationBatchService;

    @Override
    public void run(ApplicationArguments args) {
        if (!runOnStartup) {
            return;
        }

        log.info("Starting place localization batch. batchSize={}", batchSize);
        PlaceLocalizationBatchService.BatchResult result =
                placeLocalizationBatchService.localizeAllPlacesToKorean(batchSize);
        log.info(
                "Completed place localization batch. scannedCount={}, updatedCount={}",
                result.scannedCount(),
                result.updatedCount()
        );
    }
}
