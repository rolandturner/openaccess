
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

import com.versant.core.jdbc.logging.JdbcLogEvent;
import com.versant.core.logging.LogEvent;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This is a simple application that will load and analyze one or more .jdolog
 * event log files. This example looks for scarce resources (e.g. ResultSets)
 * that are not closed or released.
 *
 */
public class CheckJdoLog {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                    "Usage: java Main <filename1.jdolog> [.. <filenameN.jdolog>]");
            System.exit(1);
        }
        try {
            Resource[] ra = new Resource[]{
                new Resource("Connection", JdbcLogEvent.POOL_ALLOC,
                        new int[]{
                            JdbcLogEvent.POOL_RELEASE,
                            JdbcLogEvent.POOL_CON_TIMEOUT}),
                new Resource("PreparedStatement", JdbcLogEvent.PSPOOL_ALLOC,
                        JdbcLogEvent.PSPOOL_RELEASE),
                new Resource("ResultSet", JdbcLogEvent.STAT_EXEC_QUERY,
                        JdbcLogEvent.RS_CLOSE),
            };
            EventSource es = new EventSource(args);
            int c = 0;
            for (; ; c++) {
                LogEvent pe = es.next();
                if (pe == null) break;
                if (pe instanceof JdbcLogEvent) {
                    for (int i = ra.length - 1; i >= 0 && !ra[i].process(pe); i--);
                }
            }
            System.out.println("Checked " + c + " event(s)");
            for (int i = 0; i < ra.length; i++) {
                if (ra[i].missingEvents) {
                    System.out.println("Warning: The event log was not complete - " +
                            "analysis may be inaccurate");
                    break;
                }
            }

            int exitCode = 0;
            for (int i = 0; i < ra.length; i++) {
                if (!ra[i].report()) exitCode = 1;
            }
            System.exit(exitCode);
        } catch (Exception x) {
            x.printStackTrace(System.out);
            System.exit(1);
        }
    }

    /**
     * A scarce resources we need to track with its allocation and release
     * events.
     */
    private static class Resource {

        public String name;
        public int allocEvent;
        public int[] releaseEvents;
        public HashMap map = new HashMap();
        public boolean missingEvents;

        public Resource(String name, int allocEvent, int[] releaseEvents) {
            this.name = name;
            this.allocEvent = allocEvent;
            this.releaseEvents = releaseEvents;
        }

        public Resource(String name, int allocEvent, int releaseEvent) {
            this(name, allocEvent, new int[]{releaseEvent});
        }

        /**
         * Update for the event and return true if it was one of ours.
         */
        public boolean process(LogEvent ev) {
            int t = ev.getType();
            if (t == allocEvent) {
                map.put(new Integer(ev.getId()), ev);
                return true;
            }
            for (int i = releaseEvents.length - 1; i >= 0; i--) {
                if (releaseEvents[i] == t) {
                    Object key = new Integer(ev.getId());
                    if (map.remove(key) == null) missingEvents = true;
                    return true;
                }
            }
            return false;
        }

        /**
         * Report results to sysout. Return true if there were no leaked
         * resources.
         */
        public boolean report() {
            if (map.isEmpty()) {
                //System.out.println("All " + name + "'s returned");
                return true;
            } else {
                System.out.println(name + "'s not returned:");
                ArrayList a = new ArrayList(map.values());
                Collections.sort(a, new EventIDComparator());
                int n = a.size();
                for (int i = 0; i < n; i++) {
                    LogEvent e = (LogEvent)a.get(i);
                    System.out.println("  " + tos(e));
                }
                return false;
            }
        }
    }

    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("HH:mm:ss.SSS");

    private static String tos(LogEvent ev) {
        return FORMAT.format(new Date(ev.getStart())) + " " + ev;
    }

    /**
     * Orders events by increasing ID.
     */
    public static class EventIDComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            LogEvent a = (LogEvent)o1;
            LogEvent b = (LogEvent)o2;
            return a.getId() - b.getId();
        }

    }

    /**
     * This supplies PerfEvent's read from one or more files one at a time.
     */
    public static class EventSource {

        private String[] filenames;
        private int filenamePos;
        private ObjectInputStream in;
        private LogEvent[] events;
        private int eventPos;

        public EventSource(String[] filenames) {
            this.filenames = filenames;
        }

        /**
         * Return the next event or null if no more are available.
         */
        public LogEvent next() throws IOException, ClassNotFoundException {
            if (events == null || eventPos == events.length) {
                events = readNextEvents();
                if (events == null) return null;
                eventPos = 0;
                return next();
            }
            return events[eventPos++];
        }

        /**
         * Finish reading events.
         */
        public void close() throws IOException {
            if (in != null) {
                in.close();
                in = null;
            }
        }

        private LogEvent[] readNextEvents() throws IOException,
                ClassNotFoundException {
            if (in == null) {
                in = openNextFile();
                if (in == null) return null;
            }
            try {
                return (LogEvent[])in.readObject();
            } catch (EOFException e) {
                in.close();
                in = null;
                return readNextEvents();
            }
        }

        private ObjectInputStream openNextFile() throws IOException {
            if (filenamePos == filenames.length) return null;
            String n = filenames[filenamePos++];
            System.out.println("Opening '" + n + "'");
            FileInputStream fin = new FileInputStream(n);
            return new ObjectInputStream(fin);
        }

    }

}


