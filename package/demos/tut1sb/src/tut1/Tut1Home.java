
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
package tut1;

import java.rmi.*;
import javax.ejb.*;

/**
 * Home interface for the Tut1 session bean.
 */
public interface Tut1Home extends EJBHome {

    public Tut1 create() throws RemoteException, CreateException;

}

