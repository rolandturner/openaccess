package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static javax.persistence.AccessType.*;

@Target({TYPE}) 
@Retention(RUNTIME)

public @interface Embeddable {
    AccessType access() default PROPERTY;
}