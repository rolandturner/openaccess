
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
package com.versant.core.jdo.tools.workbench.jdoql.insight;

import com.versant.core.jdo.tools.workbench.jdoql.pane.JdoqlFilterPane;
import com.versant.core.jdo.tools.workbench.jdoql.pane.HighLightPane;
import com.versant.core.jdo.tools.workbench.jdoql.lexer.JdoqlLexer;
import com.versant.core.jdo.tools.workbench.jdoql.ordering.OrderingLexer;

/**
 * @keep-all
 */
public class InsightOrderingDataHandler {
    public InsightOrderingDataHandler() {
    }

    public boolean fillBody(HighLightPane editPane, InsightBody body) {
        try {
            OrderingLexer.BodyDataWrapper wrapper = getClassData(editPane);
            if (wrapper != null) {
                if (wrapper.data == null) return false;
                body.setHeader(wrapper.className);
                body.setListData(wrapper.data);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public OrderingLexer.BodyDataWrapper getClassData(HighLightPane editor) throws Exception {
        editor.initLexer();
        OrderingLexer orderingLexer = (OrderingLexer) editor.getLexer();
        return orderingLexer.getBodyDataWrapper(editor.getCaretPosition());

    }
}
