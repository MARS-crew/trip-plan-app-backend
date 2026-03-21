package mars.tripplanappbackend.place.service;

import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.place.domain.Place;
import mars.tripplanappbackend.place.domain.PlaceTagMap;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceListResponseDto;
import mars.tripplanappbackend.place.dto.response.RecommendedPlaceResponseDto;
import mars.tripplanappbackend.place.repository.PlaceRepository;
import mars.tripplanappbackend.place.repository.PlaceTagMapRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int DEFAULT_RECOMMEND_LIMIT = 5;
    private static final int MAX_RECOMMEND_LIMIT = 10;
    private static final int MAX_TAG_COUNT = 3;

    private final PlaceRepository placeRepository;
    private final PlaceTagMapRepository placeTagMapRepository;

    public RecommendedPlaceListResponseDto getRecommendedPlaces(Integer limit) {
        int recommendLimit = normalizeLimit(limit);

        List<Place> places = placeRepository.findByIsDeletedFalseOrderByRatingAvgDescReviewCountDesc(
                PageRequest.of(0, recommendLimit)
        );

        Map<Long, List<String>> tagsByPlaceId = getTagsByPlaceId(places);

        List<RecommendedPlaceResponseDto> recommendedPlaces = places.stream()
                .map(place -> toRecommendedPlaceResponse(place, tagsByPlaceId.getOrDefault(place.getPlaceId(), List.of())))
                .toList();

        return RecommendedPlaceListResponseDto.builder()
                .placeCount(recommendedPlaces.size())
                .recommendedPlaces(recommendedPlaces)
                .build();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_RECOMMEND_LIMIT;
        }
        return Math.min(limit, MAX_RECOMMEND_LIMIT);
    }

    private Map<Long, List<String>> getTagsByPlaceId(List<Place> places) {
        if (places.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> placeIds = places.stream()
                .map(Place::getPlaceId)
                .toList();

        List<PlaceTagMap> placeTagMaps = placeTagMapRepository.findAllByPlace_PlaceIdInAndIsDeletedFalse(placeIds);

        return placeTagMaps.stream()
                .collect(Collectors.groupingBy(
                        placeTagMap -> placeTagMap.getPlace().getPlaceId(),
                        Collectors.mapping(
                                placeTagMap -> placeTagMap.getPlaceTag().getTagName(),
                                Collectors.collectingAndThen(Collectors.toList(), tags -> tags.stream()
                                        .distinct()
                                        .limit(MAX_TAG_COUNT)
                                        .toList())
                        )
                ));
    }

    private RecommendedPlaceResponseDto toRecommendedPlaceResponse(Place place, List<String> tags) {
        return RecommendedPlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .name(place.getName())
                .countryName(place.getCountryName())
                .cityName(place.getCityName())
                .imageUrl(place.getImageUrl())
                .placeType(place.getPlaceType())
                .ratingAvg(place.getRatingAvg())
                .reviewCount(place.getReviewCount())
                .tags(tags)
                .build();
    }
}
