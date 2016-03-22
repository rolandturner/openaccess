
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
package com.versant.core.vds.metadata;

import com.versant.odbms.model.*;
import com.versant.core.common.Debug;

/**
 *
 */
public class VdsSchemaClassBinder implements UserSchemaClassListener {
    
    public void actionPerformed(final UserSchemaClassEvent event) {
        if (Debug.DEBUG) {
            Debug.assertIndexOutOfBounds(event != null,
                    "UserSchemaClassEvent is null");
        }
        switch (event.getEventType()) {
        	case UserSchemaClassEvent.SYNCHRONIZED_EVENT :
                event.getDatastoreSchemaClass().setUserObject(event.getUserSchemaClass());
        		break;
           case UserSchemaClassEvent.UNSYNCHRONIZED_EVENT :
               event.getDatastoreSchemaClass().setUserObject(null);
           	   break;
           default :
               break;
        }
    }
}
