package mars.tripplanappbackend.global.enums;

import lombok.Getter;

@Getter
public enum File {
    TRIP("trips"),
    REVIEW("reviews");

    private final String directory;

    File(String directory) {
        this.directory = directory;
    }
}
