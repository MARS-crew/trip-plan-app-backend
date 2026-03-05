package mars.tripplanappbackend.global.pagination;

import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
public class PageRequestDto {

    private int page = 0;
    private int size = 10;
    private String sort = "id";
    private String direction = "DESC";

    public Pageable toPageable() {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        return PageRequest.of(page, size, Sort.by(dir, sort));
    }
}