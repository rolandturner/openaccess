
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
package com.versant.core.jdo.tools.ant;

import com.versant.core.jdo.LogDownloader;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import com.versant.core.jdo.LogDownloader;

/**
 * Ant task wrapper for LogDownloader.
 */
public class LogDownloaderTask extends Task {

    private LogDownloader bean = new LogDownloader();

    public void execute() throws BuildException {
        try {
            LogDownloader.run(bean);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void setProject(String project) {
        bean.setProject(project);
    }

    public void setHost(String host) {
        bean.setHost(host);
    }

    public void setPort(int port) {
        bean.setPort(port);
    }

    public void setServer(String server) {
        bean.setServer(server);
    }

    public void setUsername(String username) {
        bean.setUsername(username);
    }

    public void setPassword(String password) {
        bean.setPassword(password);
    }

    public void setEventPollSecs(int eventPollSecs) {
        bean.setEventPollSecs(eventPollSecs);
    }

    public void setMetricPollSecs(int metricPollSecs) {
        bean.setMetricPollSecs(metricPollSecs);
    }

    public void setAppend(boolean append) {
        bean.setAppend(append);
    }

    public void setMaxFileSizeK(int maxFileSizeK) {
        bean.setMaxFileSizeK(maxFileSizeK);
    }

    public void setBackups(int backups) {
        bean.setBackups(backups);
    }

    public void setFilename(String filename) {
        bean.setFilename(filename);
    }

    public void setEventBinary(boolean eventBinary) {
        bean.setEventBinary(eventBinary);
    }

    public void setEventText(boolean eventText) {
        bean.setEventText(eventText);
    }

    public void setMetricBinary(boolean metricBinary) {
        bean.setMetricBinary(metricBinary);
    }

    public void setQuiet(boolean on) {
        bean.setQuiet(on);
    }

    public void setDateFormat(String dateFormat) {
        bean.setDateFormat(dateFormat);
    }

    public void setSingle(boolean on) {
        bean.setSingle(on);
    }

}
