package javax.persistence; 

import java.lang.annotation.*; 
import static java.lang.annotation.ElementType.*; 
import static java.lang.annotation.RetentionPolicy.*; 

@Target({METHOD, TYPE, PACKAGE})  
@Retention(RUNTIME) 
public @interface NamedQueries { 
     NamedQuery [] value (); 
}