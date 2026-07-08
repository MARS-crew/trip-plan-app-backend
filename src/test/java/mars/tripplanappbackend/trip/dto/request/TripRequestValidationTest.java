package mars.tripplanappbackend.trip.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TripRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("여행 추가 요청은 10자를 초과하거나 이모지가 5개를 초과한 여행 제목도 허용한다")
    void createTripRequestAllowsLongTitleAndMoreThanFiveEmojis() throws Exception {
        CreateTripRequestDto requestDto = newInstance(CreateTripRequestDto.class);
        setField(requestDto, "title", "오사카먹방여행계획😀😀😀😀😀😀");
        setField(requestDto, "startDate", LocalDate.of(2026, 7, 20));
        setField(requestDto, "endDate", LocalDate.of(2026, 7, 25));

        Set<ConstraintViolation<CreateTripRequestDto>> violations = validator.validate(requestDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("여행 수정 요청은 10자를 초과하거나 이모지가 5개를 초과한 여행 제목도 허용한다")
    void updateTripRequestAllowsLongTitleAndMoreThanFiveEmojis() throws Exception {
        UpdateTripRequestDto requestDto = newInstance(UpdateTripRequestDto.class);
        setField(requestDto, "title", "도쿄디즈니가족여행😀😀😀😀😀😀");
        setField(requestDto, "startDate", LocalDate.of(2026, 8, 1));
        setField(requestDto, "endDate", LocalDate.of(2026, 8, 4));

        Set<ConstraintViolation<UpdateTripRequestDto>> violations = validator.validate(requestDto);

        assertThat(violations).isEmpty();
    }

    private <T> T newInstance(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
