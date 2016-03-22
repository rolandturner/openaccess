
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

import java.io.*;

/**
 * File that rolls over to backups at a configurable max size. Parts of this
 * file have been cut and pasted from org.apache.log4j.RollingFileAppender.
 * @keep-all
 */
public class RollingFile {

    private String filename;
    private long maxSize = 1024 * 1024;
    private int maxBackupIndex = 1;
    private boolean append;
    private CountingOutputStream out;
    private boolean firstOpen = true;

    public RollingFile(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    public void setMaxBackupIndex(int maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    /**
     * Open our file. This will close any existing file.
     */
    public void open() throws IOException {
        close();
        if (firstOpen) {
            firstOpen = false;
            if (!append) {  // nuke old backups
                for (int i = 1; i <= maxBackupIndex; i++) {
                    File file = new File(filename + "." + i);
                    if (file.exists()) file.delete();
                }
            }
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(filename,append);
            this.out = new CountingOutputStream(fos);
        }
        catch(FileNotFoundException e)
        {
            // Exception can happen when directory does not exist.


            throw e; // plain rethrowing what we caught

        }
        if (append)
            out.setCount(new File(filename).length());
    }

    /**
     * Close our file.
     */
    public void close() throws IOException {
        if (out != null) out.close();
    }

    /**
     * Get the stream that writes to our file.
     */
    public OutputStream getOut() throws IOException {
        if (out == null) open();
        return out;
    }

    /**
     * Is a rollover required? This should be called after writes to out. We
     * cannot rollover automatically as out will typically be wrapped by
     * other streams.
     */
    public boolean isRolloverRequired() {
        return out.getCount() > maxSize;
    }

    /**
     * Perform a rollover to a new file.
     */
    public void rollover() throws IOException {
        File target, file;

        // If maxBackups <= 0, then there is no file renaming to be done.
        if (maxBackupIndex > 0) {
            // Delete the oldest file, to keep Windows happy.
            file = new File(filename + '.' + maxBackupIndex);
            if (file.exists()) file.delete();

            // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
            for (int i = maxBackupIndex - 1; i >= 1; i--) {
                file = new File(filename + "." + i);
                if (file.exists()) {
                    target = new File(filename + '.' + (i + 1));
                    file.renameTo(target);
                }
            }

            // Rename filename to filename.1
            target = new File(filename + "." + 1);

            out.close();

            file = new File(filename);
            file.renameTo(target);
        }
        open();
    }

    /**
     * OutputStream that counts bytes written to another stream.
     */
    public static class CountingOutputStream extends OutputStream  {

        private OutputStream out;
        private long count;

        public CountingOutputStream(OutputStream out) {
            this.out = out;
        }

        /**
         * Get number of bytes written so far.
         */
        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public void write(int b) throws IOException {
            ++count;
            out.write(b);
        }

        public void write(byte b[]) throws IOException {
            count += b.length;
            out.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            count += len;
            out.write(b, off, len);
        }

        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }
    }

}
