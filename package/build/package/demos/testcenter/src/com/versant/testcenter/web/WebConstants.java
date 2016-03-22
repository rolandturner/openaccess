
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
package com.versant.testcenter.web;

public interface WebConstants {

    /**
     * OID of the currently logged in user is stored in
     * {@link javax.servlet.http.HttpSession} under this key
     */
    public static final String USER_OID_KEY = WebConstants.class.getName() + "USER_OID_KEY";

    /**
     * Key for request attribute used to pass list
     * of {@link com.versant.testcenter.model.Exam} from action to jsp
     */
    public static final String EXAM_SEARCH_RESULT = "examSearchResult";

    /**
     * Http parameter name specifying exam OID
     */
    public static final String EXAM_PARAM = "exam";

}
