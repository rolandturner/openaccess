
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
package petstore.www;

import petstore.model.Page;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 */
public class PageForm extends ActionForm {

    private String parent;
    private String start;
    private String count;

    private Page page;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }


    public Map getNextParameters() {
        Map map = new HashMap();
        map.put("parent", parent);
        map.put("start", String.valueOf(page.getStartOfNextPage()));
        return map;
    }

    public Map getPrevParameters() {
        Map map = new HashMap();
        map.put("parent", parent);
        map.put("start", String.valueOf(page.getStartOfPreviousPage()));
        return map;
    }

    public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {
        parent = null;
        start = "0";
        count = "0";
    }
}
