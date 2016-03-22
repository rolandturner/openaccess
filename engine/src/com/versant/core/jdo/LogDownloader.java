
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
package com.versant.core.jdo;

import com.versant.core.util.BeanUtils;
import com.versant.core.logging.LogEvent;
import com.versant.core.metric.MetricSnapshotPacket;

import javax.jdo.JDOHelper;
import java.util.Properties;
import java.util.Date;
import java.io.*;
import java.text.SimpleDateFormat;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.config.ConfigParser;

/**
 * Utility bean to download event logs and performance metric snapshots and
 * store then in binary and/or text files. The binary files can be opened
 * in the Workbench for later analysis. This can be run from the command
 * line or embedded into an application. The JDO Genie server uses an
 * instance of this class for its built in logging features. To use it in
 * an application create an instance, set properties and invoke the run()
 * method at regular intervals (e.g. create a Thread to do this).
 */
public class LogDownloader implements VersantBackgroundTask {

    private String project = "versant.properties";
    private String host;
    private int port;
    private String server;
    private String username;
    private String password;
    private VersantPersistenceManagerFactory pmf;
    private int eventPollSecs = 1;
    private int metricPollSecs = 60;
    private boolean append;
    private int maxFileSizeK = 1000;
    private int backups = 3;
    private String filename;
    private boolean eventBinary = true;
    private boolean eventText;
    private String dateFormat = "HH:mm:ss.SSS";
    private boolean metricBinary = true;
    private boolean quiet;
    private boolean single;

    private boolean shutdown;

    private int lastEventId;
    private RollingFile eventBinaryFile;
    private ObjectOutputStream eventOutput;
    private RollingFile eventTextFile;
    private OutputStreamWriter eventWriter;
    private SimpleDateFormat eventDateFormat;

    private int lastMetricId;
    private RollingFile metricBinaryFile;
    private ObjectOutputStream metricOutput;
    private boolean metricsDone;

    private boolean downloadedEvents;
    private boolean downloadedMetrics;

    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator");

    public static void main(String[] args) {
        try {
            LogDownloader l = new LogDownloader();
            BeanUtils.setCommandLineArgs(l, args, new String[]{
                "host", "port", "server", "username", "password", "eventPollSecs",
                "metricPollSecs", "append", "maxFileSizeK", "backups", "filename",
                "eventBinary", "eventText", "metricBinary", "quiet", "project"
            });
            run(l);
            System.exit(0);
        } catch (Exception x) {
            x.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public static void run(LogDownloader l) throws Exception {
        if (l.getProject() != null) {
            Properties p = loadProperties(l.getProject());
            if (l.getHost() == null) {
                l.setHost(p.getProperty("versant.host"));
            }
            if (l.getServer() == null) {
                l.setServer(p.getProperty(ConfigParser.SERVER));
            }
            if (l.getPort() == 0) {
                String s = p.getProperty(ConfigParser.SERVER_PORT);
                if (s != null) {
                    try {
                        l.setPort(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        throw BindingSupportImpl.getInstance().illegalArgument(
                                "Invalid port: " + e);
                    }
                }
            }
        }
        if (l.getHost() == null) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "-host is required");
        }
        if (l.getServer() == null) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "-server is required");
        }
        Properties p = new Properties();
        String host = l.getHost();
        int port = l.getPort();
        String server = l.getServer();
        p.setProperty(ConfigParser.PMF_CLASS,
                "com.versant.core.jdo.BootstrapPMF");
        p.setProperty("versant.host", host);
        p.setProperty(ConfigParser.SERVER, server);
        if (port != 0) p.setProperty(ConfigParser.SERVER_PORT, Integer.toString(port));
        if (!l.isQuiet()) {
            System.out.println("Connecting to " + host +
                    (port == 0 ? "" : ":" + port) + "/" + server);
        }
        if (l.getUsername() != null) {
            p.setProperty("versant.remoteUsername", l.getUsername());
        }
        if (l.getPassword() != null) {
            p.setProperty("versant.remotePassword", l.getPassword());
        }
        VersantPersistenceManagerFactory pmf =
                (VersantPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(
                        p);
        l.setPmf(pmf);
        l.run();
    }

    private static Properties loadProperties(String filename)
            throws IOException {
        ClassLoader cl = LogDownloader.class.getClassLoader();
        InputStream in = null;
        try {
            if (filename.startsWith("/")) filename = filename.substring(1);
            in = cl.getResourceAsStream(filename);
            if (in == null) {
                throw BindingSupportImpl.getInstance().runtime(
                        "Resource not found: " + filename);
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            if (in != null) in.close();
        }
    }

    public void setPmf(VersantPersistenceManagerFactory pmf) throws Exception {
        this.pmf = pmf;

        if (!eventBinary && !eventText && !metricBinary) {
            return;
        }

        if (filename == null) {
            filename = generateFilename();
        }

        int maxSize = maxFileSizeK * 1024;

        if (eventBinary) {
            eventBinaryFile = new RollingFile(filename + ".jdolog");
            eventBinaryFile.setMaxBackupIndex(backups);
            eventBinaryFile.setMaxSize(maxSize);
            openEventBinaryFile();
        }

        if (eventText) {
            eventDateFormat = new SimpleDateFormat(dateFormat);
            eventTextFile = new RollingFile(filename + ".txt");
            eventTextFile.setMaxBackupIndex(backups);
            eventTextFile.setMaxSize(maxSize);
            eventTextFile.setAppend(append);
            openEventTextFile();
        }

        if (metricBinary) {
            metricBinaryFile = new RollingFile(filename + ".jdoperf");
            metricBinaryFile.setMaxBackupIndex(backups);
            metricBinaryFile.setMaxSize(maxSize);
            openMetricBinaryFile();
        }
    }

    /**
     * Generate a filename. This is invoked if no filename has been set when
     * the pmf is set.
     */
    protected String generateFilename() {
        if (server != null) {
            return "openaccess_" + (host == null ? "" : host + "_") + server;
        } else {
            return "openaccess";
        }
    }

    public void run() {
        try {
            runImp();
        } catch (Exception e) {
            if (!shutdown) {
                e.printStackTrace(System.out);
            }
        }
    }

    protected void log(String msg) {
        if (quiet) return;
        System.out.println(new Date() + " " + msg);
    }

    protected void runImp() throws Exception {
        boolean eventsOn = eventBinary || eventText;
        if (!eventsOn && !metricBinary) return;

        try {
            log("Log downloader running");

            long lastEventTime = 0;
            long lastMetricTime = 0;
            int eventPoll = eventPollSecs * 1000;
            if (eventPoll < 1000) eventPoll = 1000;
            int metricPoll = metricPollSecs * 1000;
            if (metricPoll < 1000) metricPoll = 1000;
            long sleep = 0;
            for (; !shutdown;) {
                try {
                    if (sleep > 0) Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    // ignore
                }

                long now = System.currentTimeMillis();

                if (eventsOn) {
                    if (shutdown || now - lastEventTime >= eventPoll) {
                        lastEventTime = now;
                        downloadEvents();
                        sleep = eventPoll;
                    } else {
                        sleep = now - lastEventTime;
                    }
                } else {
                    sleep = Long.MAX_VALUE;
                }

                if (metricBinary) {
                    long nsleep;
                    if (shutdown || now - lastMetricTime >= metricPoll) {
                        lastMetricTime = now;
                        downloadMetrics();
                        nsleep = metricPoll;
                    } else {
                        nsleep = now - lastMetricTime;
                    }
                    if (nsleep < sleep) sleep = nsleep;
                }

                if (single && (downloadedEvents || downloadedMetrics)) {
                    shutdown();
                }
            }

            if (eventOutput != null) eventOutput.close();
            if (eventWriter != null) eventWriter.close();
            if (metricOutput != null) metricOutput.close();
        } finally {
            log("Log downloader stopped");
        }
    }

    private void downloadEvents() throws IOException {
        LogEvent[] a = pmf.getNewPerfEvents(lastEventId);
        if (a == null) return;
        downloadedEvents = true;
        int n = a.length;
        log("Received " + n + " event(s)");
        lastEventId = a[n - 1].getId();
        if (eventText) {
            StringBuffer s = new StringBuffer();
            Date d = new Date();
            for (int i = 0; i < n; i++) {
                LogEvent ev = a[i];
                d.setTime(ev.getStart());
                s.append(eventDateFormat.format(d));
                s.append(' ');
                s.append(ev);
                s.append(LINE_SEPARATOR);
            }
            eventWriter.write(s.toString());
            if (eventTextFile.isRolloverRequired()) {
                eventWriter.close();
                eventTextFile.rollover();
                openEventTextFile();
            } else {
                eventWriter.flush();
            }
        }
        if (eventBinary) {
            eventOutput.writeObject(a);
            if (eventBinaryFile.isRolloverRequired()) {
                eventOutput.close();
                eventBinaryFile.rollover();
                openEventBinaryFile();
            } else {
                eventOutput.flush();
            }
        }
    }

    private void downloadMetrics() throws IOException {
        // make sure each file contains the Metric's themselves
        if (!metricsDone) {
            metricOutput.writeObject(pmf.getMetrics());
            metricOutput.flush();
            metricsDone = true;
        }
        MetricSnapshotPacket a = pmf.getNewMetricSnapshots(lastMetricId);
        if (a == null) return;
        downloadedMetrics = true;
        log("Received " + a.getSize() + " snapshot(s)");
        lastMetricId = a.getMostRecentID();
        metricOutput.writeObject(a);
        if (metricBinaryFile.isRolloverRequired()) {
            metricOutput.close();
            metricBinaryFile.rollover();
            openMetricBinaryFile();
            metricsDone = false;
        } else {
            metricOutput.flush();
        }
    }

    private void openEventBinaryFile() throws IOException {
        eventOutput = new ObjectOutputStream(eventBinaryFile.getOut());
    }

    private void openEventTextFile() throws IOException {
        eventWriter = new OutputStreamWriter(eventTextFile.getOut());
    }

    private void openMetricBinaryFile() throws IOException {
        metricOutput = new ObjectOutputStream(metricBinaryFile.getOut());
    }

    /**
     * Stop downloading events. This will cause one more call to the server
     * to get any remaining data and then the run method will return. You
     * should also interrupt the Thread that called run to speed exit of
     * the run method.
     */
    public void shutdown() {
        shutdown = true;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VersantPersistenceManagerFactory getPmf() {
        return pmf;
    }

    public int getEventPollSecs() {
        return eventPollSecs;
    }

    public void setEventPollSecs(int eventPollSecs) {
        this.eventPollSecs = eventPollSecs;
    }

    public int getMetricPollSecs() {
        return metricPollSecs;
    }

    public void setMetricPollSecs(int metricPollSecs) {
        this.metricPollSecs = metricPollSecs;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public int getMaxFileSizeK() {
        return maxFileSizeK;
    }

    public void setMaxFileSizeK(int maxFileSizeK) {
        this.maxFileSizeK = maxFileSizeK;
    }

    public int getBackups() {
        return backups;
    }

    public void setBackups(int backups) {
        this.backups = backups;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isEventBinary() {
        return eventBinary;
    }

    public void setEventBinary(boolean eventBinary) {
        this.eventBinary = eventBinary;
    }

    public boolean isEventText() {
        return eventText;
    }

    public void setEventText(boolean eventText) {
        this.eventText = eventText;
    }

    public boolean isMetricBinary() {
        return metricBinary;
    }

    public void setMetricBinary(boolean metricBinary) {
        this.metricBinary = metricBinary;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

}
