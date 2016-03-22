package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface JoinColumns {
    JoinColumn[] value();
}