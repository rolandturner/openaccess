package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.persistence.InheritanceType.*;
import static javax.persistence.DiscriminatorType.*;

@Target({TYPE})
@Retention(RUNTIME)
public @interface Inheritance {
    InheritanceType strategy() default SINGLE_TABLE;
    DiscriminatorType discriminatorType() default STRING;
    String discriminatorValue() default "";
}