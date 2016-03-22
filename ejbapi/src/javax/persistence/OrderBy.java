package javax.persistence;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface OrderBy {
    String value() default "";
}
