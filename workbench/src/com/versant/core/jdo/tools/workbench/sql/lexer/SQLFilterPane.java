
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
package com.versant.core.jdo.tools.workbench.sql.lexer;

import com.versant.core.jdo.tools.workbench.editor.JEditTextArea;
import com.versant.core.jdo.tools.workbench.editor.PLSQLTokenMarker;
import com.versant.core.jdo.tools.workbench.editor.CustomSQLTokenMarker;
import com.versant.core.jdo.tools.workbench.editor.misc.SqlInputHandler;
import com.versant.core.jdo.tools.workbench.model.MdProject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @keep-all
 */
public class SQLFilterPane extends JEditTextArea {

    private SqlInputHandler inputHandler;

    public SQLFilterPane(MdProject project) throws Exception {
        super();
        if (project != null &&
                project.getDataStore() != null &&
                project.getDataStore().getDb() != null) {
            setTokenMarker(
                    new CustomSQLTokenMarker(
                            project.getDataStore().getDb()));
        } else {
            setTokenMarker(new PLSQLTokenMarker());
        }
        setIndentOpenBrackets("(");
        setIndentCloseBrackets(")");
        setComment("--");
        inputHandler = new SqlInputHandler(this);
        inputHandler.setProject(project);
        setInputHandler(inputHandler);
    }

    public void setProject(MdProject project) {
        inputHandler.setProject(project);
    }

    public void setRightClickPopup(JPopupMenu textPopup) {
        if (isEditable()) {
            if (textPopup != null) {
                textPopup.addSeparator();
            } else {
                textPopup = new JPopupMenu();
            }

            ArrayList list = inputHandler.getOptions();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                JMenuItem item = (JMenuItem)iter.next();
                if (item != null) {
                    textPopup.add(item);
                } else {
                    textPopup.addSeparator();
                }
            }
        }
        super.setRightClickPopup(textPopup);
    }

}
