
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
 * Copyright (c) 1999-2003 it Workplace Solutions and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/cpl.php
 *
 * Contributors:
 *     it Workplace Solutions - initial API and implementation
 */

package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import java.io.Serializable;

/**
 * <p>This interface defines a thing. Materials, tools, automatons and
 * containers are things.</p>
 * <p>Things have a unique constant id and a name. Materials, tools, automatons
 * and containers should implement this interface.</p>
 * <p><b>Note:</b> AbstractThing is a default implementation that is suitable as a superclass in most cases.
 * (This interface is primarily used to implement aspect classes for materials.)</p>
 *
 * @pattern Adapter (GoF) - a <code>Thing</code> has the operation <code>adaptTo()</code> that may
 * eventually provide an adapter for the thing
 */
public interface Thing extends Serializable {

    //
    // public interface
    //

    /**
     * Return the display name of this Thing.
     *
     * @return - The Displayname
     */
    public String getDisplayName();

    /**
     * Return the unique id of the thing
     *
     * @return The ID of the thing
     * @ensure result != null
     */
    public IdentificatorDV getID();

    /**
     * <p>Try to adapt this thing to another aspect type (aspect is a term of
     * the tool & materials approach).</p>
     * <p>If the thing can be adapted, an object of the requested type is returned.
     * If the thing cannot be adapted as wished, <code>null</code> is returned.</p>
     * <p><b>The implementation of this operation has to follow these rules in particular:</b>
     * <ul>
     * <li>If the requested type is available and is a sub type of <code>Thing</code>,
     * the result must have the same ID as this object. Furthermore, the
     * result's <code>adaptTo()</code> operation should behave exactly like
     * this one's.</li>
     * <li>For all types of which this object is an instance of, the <code>adaptTo</code>
     * operation should yield a non-null result</li>
     * </ul>
     *
     * @param aspect the aspect that this thing should adapt to
     * @return If the thing can be adapted, an object of the requested type is returned.
     *         Otherwise, <code>null</code> is returned.
     * @require aspect != null
     * @ensure result == null || aspect.isInstance(result)
     */
    public Object adaptTo(Class aspect);
}
