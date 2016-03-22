package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({PACKAGE, TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface TableGenerator {
    String name();
    Table table() default @Table(specified=false);
    String pkColumnName() default "";
    String valueColumnName() default "";
    String pkColumnValue() default "";
    int initialValue() default 0;
    int allocationSize() default 50;
}