package javax.persistence;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
public @interface NamedNativeQuery {
    String name();
    String queryString();
    Class resultClass() default void.class;
    String resultSetMapping() default ""; // name of SQLResultSetMapping
}
