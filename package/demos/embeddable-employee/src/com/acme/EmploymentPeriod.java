
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
package com.acme;
import javax.persistence.*;
@Embeddable
public class EmploymentPeriod {
	java.util.Date startDate;
	java.util.Date endDate;
	public java.util.Date getStartDate() {return startDate;}
	public void setStartDate(java.util.Date date) {
		this.startDate = date;
	}
	public java.util.Date getEndDate() {return endDate;}
	public void setEndDate(java.util.Date date) {
		this.endDate = date;
	}
}
