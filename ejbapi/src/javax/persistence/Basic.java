package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.persistence.FetchType.*;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Basic {
    FetchType fetch() default EAGER;
    TemporalType temporalType() default TemporalType.NONE;
    boolean optional() default true;
}