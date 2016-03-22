package javax.persistence;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
public @interface JoinTable {
    Table table() default @Table(specified=false);
    JoinColumn[] joinColumns() default {};
    JoinColumn[] inverseJoinColumns() default {};
}
