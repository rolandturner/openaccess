package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
public @interface GeneratedIdTable {
    String name() default "";
    Table table() default @Table(specified=false);
    String pkColumnName() default "";
    String valueColumnName() default "";
}