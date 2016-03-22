package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target(TYPE) 
@Retention(RUNTIME)
public @interface SecondaryTable {
    String name();
    String catalog() default "";
    String schema() default "";
    JoinColumn[] join() default {};
    PrimaryKeyJoinColumn[] pkJoin() default {};
    UniqueConstraint[] uniqueConstraints() default {};
}