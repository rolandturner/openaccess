
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
package util;

import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.server.perf.PerfEvent;
import com.versant.core.jdbc.perf.JdbcPerfEvent;

/**
 * This class isolates JDO Genie specific stuff. If this app was not using
 * JDO Genie it could be replaced with a vendorx specific class or changed
 * so that these methods are NOPs. None of them are essential to the
 * functioning of the application.
 *
 */
public class Versant {

    /**
     * Get the ID of the most recent event in the event log.
     */
    public static int getMostRecentEventID() {
        VersantPersistenceManagerFactory pmf = (VersantPersistenceManagerFactory)Sys.pmf();
        PerfEvent[] a = pmf.getNewPerfEvents(0);
        return a == null ? 0 : a[a.length - 1].getId();
    }

    /**
     * Query the event log buffer for events since the event with eventId and
     * return all SQL formatted into HTML.
     */
    public static String getNewSQL(int eventId) {
        VersantPersistenceManagerFactory pmf = (VersantPersistenceManagerFactory)Sys.pmf();
        PerfEvent[] a = pmf.getNewPerfEvents(eventId);
        StringBuffer s = new StringBuffer();
        if (a != null) {
            boolean first = true;
            for (int i = 0; i < a.length; i++) {
                PerfEvent e = a[i];
                if (e instanceof JdbcPerfEvent) {
                    if (first) {
                        s.append(
                                "<table cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");
                        first = false;
                    }
                    s.append("<tr valign=\"top\"><td><code>");
                    s.append(e.getName());
                    s.append("</code></td><td><code>");
                    String des = e.getDescription();
                    s.append(des == null ? "&nbsp;" : des);
                    s.append("</code></td></tr>\n");
                }
            }
            if (!first) s.append("</table>");
        }
        if (s.length() == 0) return "<p><i>No SQL executed</i></p>";
        return s.toString();
    }

}

