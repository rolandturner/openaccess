
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

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.LogDownloader;

/**
 * Property info for LogDownloader.
 * @keep-all
 */
public class LogDownloaderBeanInfo extends SimpleBeanInfo {

    public LogDownloaderBeanInfo() {
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            String[] s = new String[]{
                "eventPollSecs", "Event poll seconds",
                    "Time in seconds between polls of the event ring buffer",
                "metricPollSecs", "Metric poll seconds",
                    "Time in seconds between polls of the metric snapshot ring buffer",
                "append", "Append",
                    "Append to end of existing event text file (if any)",
                "maxFileSizeK", "Max file size in K",
                    "Maximum size for a file before rollover to backup",
                "backups", "Number of backups",
                    "Number of old log files to keep (filename.jdolog.1, filename.jdolog.2, ...)",
                "filename", "Filename",
                    "Data is written to files based on this name (default is jdogenie_<server>)",
                "eventBinary", "Write event binary file",
                    "Write events to a binary file (filename.jdolog, open in management console)",
                "eventText", "Write event text file",
                    "Write events to text file (filename.txt, for tail -f)",
                "metricBinary", "Write metric binary file",
                    "Write metric snapshots to binary file (filename.jdoperf, open in management console)",
                "dateFormat", "Date format",
                    "Date format for event text file (for java.text.SimpleDateFormat)",
            };
            int n = s.length / 3;
            PropertyDescriptor[] ans = new PropertyDescriptor[n];
            int q = 0;
            for (int i = 0; i < n; i++, q += 3) {
                PropertyDescriptor p = new PropertyDescriptor(
                    s[q], LogDownloader.class);
                p.setDisplayName(s[q + 1]);
                p.setShortDescription(s[q + 2]);
                ans[i] = p;
            }
            return ans;
        } catch (IntrospectionException e) {
            throw BindingSupportImpl.getInstance().internal(
                    e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

}

 
