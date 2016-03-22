package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE}) 
@Retention(RUNTIME)

public @interface DiscriminatorColumn {
    String name() default "";
    String columnDefinition() default "";
    int length() default 10;
}