
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
 * DirtyLifecycleListener.java
 *
 */

package javax.jdo.listener;

/**
 * This interface is implemented by listeners to be notified of
 * dirty events.
 * @version 2.0
 * @since 2.0
 */
public interface DirtyLifecycleListener
    extends InstanceLifecycleListener {

    /**
     *	Invoked whenever a persistent instance is first made dirty,
     *	during an operation that modifies the value of a persistent or
     *	transactional field. Called before the value is changed.
     * @since 2.0
     */
    void preDirty (InstanceLifecycleEvent event);

    /**
     *	Invoked whenever a persistent instance is first made dirty,
     *	during an operation that modifies the value of a persistent or
     *	transactional field. Called after the value is changed.
     * @since 2.0
     */
    void postDirty (InstanceLifecycleEvent event);
}
