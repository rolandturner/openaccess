
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
package com.versant.core.jdo.sco;

import com.versant.core.common.VersantFieldMetaData;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.*;

/**
 * Creates a Date SCO from a java.util.Date
 */
public class DateSCOFactory implements VersantSCOFactory,
        Serializable {

    /**
     * Create a Date instance that implements the JDOGenieSCO interface and fill
     * it with the data in o.
     */
    public VersantSimpleSCO createSCO(PersistenceCapable owner,
                                       VersantPMInternal pm, VersantStateManager stateManager,
                                       VersantFieldMetaData fmd, Object o) {
        if (o instanceof java.util.Date) {
            return new com.versant.core.jdo.sco.Date(owner, stateManager,
                    fmd, ((java.util.Date) o).getTime());
        } else if (o instanceof Long) {
            return new com.versant.core.jdo.sco.Date(owner, stateManager,
                    fmd, ((Long) o).longValue());
        } else {
            throw BindingSupportImpl.getInstance().illegalArgument(o.getClass() + " can not be converted into a Date SCO");
        }
    }
}
