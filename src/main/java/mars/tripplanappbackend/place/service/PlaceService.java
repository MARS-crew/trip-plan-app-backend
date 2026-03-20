package mars.tripplanappbackend.place.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import mars.tripplanappbackend.place.repository.PlaceTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceTagRepository placeTagRepository;
    private final PlaceTagMapRepository placeTagMapRepository;
}
