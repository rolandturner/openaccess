
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
package com.versant.core.ejb.junit.ejbtest1.model;

import javax.persistence.NamedQuery;
import javax.persistence.Inheritance;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "QUERY_BIG_TASK")
@Inheritance(discriminatorValue = "B")
@NamedQuery(
        name = "findWithBudgetLargerThan",
        queryString = "SELECT OBJECT(task) FROM BigTask task WHERE task.budget >= :amount"
)
public class BigTask extends Task {
    private double budget;

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }
}

