package javax.persistence;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
public @interface UniqueConstraint {
    String[] columnNames();
}
