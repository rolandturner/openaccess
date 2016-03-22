package javax.persistence;

import java.lang.annotation.*;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

@Target({TYPE, METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface AttributeOverride {
    String name();
    Column column();
}
