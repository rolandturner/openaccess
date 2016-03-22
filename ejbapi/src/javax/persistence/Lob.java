package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Lob {
    FetchType fetch() default FetchType.LAZY;
    LobType type() default LobType.BLOB;
    boolean optional() default true;
}
