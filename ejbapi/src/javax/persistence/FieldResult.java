package javax.persistence;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

@Target({TYPE})
@Retention(RUNTIME)
public @interface FieldResult {
    String name();
    String column();
}
