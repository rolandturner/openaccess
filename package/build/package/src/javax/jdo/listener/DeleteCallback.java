
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
 * DeleteCallback.java
 *
 */
 
package javax.jdo.listener;


/** 
 * This interface is used to notify instances of delete events.
 * @version 2.0
 * @since 2.0
 */
public interface DeleteCallback
{
    /**
     * Called before the instance is deleted.
     * This method is called before the state transition to persistent-deleted 
     * or persistent-new-deleted. Access to field values within this call 
     * are valid. Access to field values after this call are disallowed. 
     * <P>This method is modified by the enhancer so that fields referenced 
     * can be used in the business logic of the method.
     */
    void jdoPreDelete();
}
