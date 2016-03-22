
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.versant.core.jdo.tools.workbench.model.MdProject;
import com.versant.core.jdo.tools.workbench.model.MdClass;

/**
 * @keep-all
 */
public class ParamHandler {
    private HashMap avalableMap = new HashMap();
    private HashMap varMap = new HashMap();
    private HashMap paramMap = new HashMap();

    public ParamHandler() {
    }

    public void clear(){
        varMap.clear();
        paramMap.clear();
    }

    public void setVariables(String vars){
        parseVariables(vars);
    }

    private void parseVariables(String params) {
        try {
            if (params == null || params.equals("")) return;
            StringTokenizer st = new StringTokenizer(params, ";", false);
            int i = 0;
            while (st.hasMoreTokens()) {
                String t = st.nextToken().trim();
                i = t.indexOf(" ");

                String type = getShortName(t.substring(0, i).trim());
                String var = t.substring(i, t.length()).trim();
                if (avalableMap.containsKey(type)) {
                    varMap.put(var, avalableMap.get(type));
                } else {
                    varMap.put(var, type);
                }
            }
        } catch (Exception e) {
        }//do nothing
    }

    public void setParams(String params){
        parseParams(params);
    }

    private void parseParams(String params){
        try {
            if (params == null || params.equals(""))return;
            StringTokenizer st = new StringTokenizer(params,",",false);
            int i = 0;
            while (st.hasMoreTokens()){
                String t = st.nextToken().trim();
                i = t.indexOf(" ");

                String type = getShortName(t.substring(0,i).trim());
                String var = t.substring(i,t.length()).trim();
                if (avalableMap.containsKey(type)){
                    paramMap.put(var,avalableMap.get(type));
                } else {
                    paramMap.put(var,type);
                }
            }
        } catch (Exception e) {}//do nothing
    }

    public void setProject(MdProject project){
        List list = project.getAllClasses();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            MdClass mdClass = (MdClass) iterator.next();
            avalableMap.put(mdClass.getName(),mdClass);
        }

    }

    private String getShortName(String longName){
        if (longName.lastIndexOf('.') > 0){
            return longName.substring(longName.lastIndexOf('.')+1,longName.length());
        } else {
            return longName;
        }
    }

    public HashMap getVarMap(){
        return varMap;
    }

    public HashMap getParamMap() {
        return paramMap;
    }


}

