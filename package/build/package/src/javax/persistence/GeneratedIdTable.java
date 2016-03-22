
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package javax.persistence;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
public @interface GeneratedIdTable {
    String name() default "";
    Table table() default @Table(specified=false);
    String pkColumnName() default "";
    String valueColumnName() default "";
}
