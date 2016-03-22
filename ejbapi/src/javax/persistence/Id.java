package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.persistence.GeneratorType.*;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Id {
    GeneratorType generate() default NONE;
    String generator() default "";
}