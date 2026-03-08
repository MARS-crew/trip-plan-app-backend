package mars.tripplanappbackend.global.config.swagger;

import mars.tripplanappbackend.global.enums.ErrorCode;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorExceptions {
    ErrorCode[] value();
}