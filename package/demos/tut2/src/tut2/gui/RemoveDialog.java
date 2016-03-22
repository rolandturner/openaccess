
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
package tut2.gui;

import tut2.model.Person;

import javax.jdo.PersistenceManager;
import java.awt.*;

/**
 * This Dialog confirms the removal of a person.
 * <p>
 */
public class RemoveDialog extends AddDialog {

    public RemoveDialog(Frame owner, Person person, PersistenceManager pm) throws Exception {
        super(owner, person, pm);
        setTitle("Are you sure you want to delete this user?");
        personPanel.setEnabled(false);
    }

    /**
     * Do we have all the information we need?
     * @return false if we need more info; true if everyting is ok
     */
    protected boolean doCheck() {
        return true;
    }
}

