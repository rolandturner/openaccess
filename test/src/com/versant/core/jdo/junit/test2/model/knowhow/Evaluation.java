
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
package com.versant.core.jdo.junit.test2.model.knowhow;

/** 
 * @keep-all
 */
public class Evaluation extends ResourceData {

    private int questionCount;

    public Evaluation(String description, int questionCount) {
        super(description);
        this.questionCount = questionCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

}
