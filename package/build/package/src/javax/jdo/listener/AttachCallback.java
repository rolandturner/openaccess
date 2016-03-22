
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
/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * AttachCallback.java
 *
 */
 
package javax.jdo.listener;

import javax.jdo.PersistenceManager;

/**
 * This interface is used to notify instances of attach events.
 * @version 2.0
 * @since 2.0
 */
public interface AttachCallback {
    
    /**
     * This method is called during the execution of
     * {@link PersistenceManager#attachCopy} before the copy is made.
     * @since 2.0
     */
    public void jdoPreAttach();

    /**
     * This method is called during the execution of
     * {@link PersistenceManager#attachCopy} on the persistent
     * instance after the copy is made.
     * @param attached	The corresponding (non-attached) instance that was
     * attached in the call to {@link PersistenceManager#attachCopy}.
     * @since 2.0
     */
    public void jdoPostAttach(Object attached);
}
