
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
package com.versant.core.jdo.tools.workbench.jdoql.pane;

import java.io.Reader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

/**
 * @keep-all
 *
 * A reader interface for an abstract document.  Since
 * the syntax highlighting packages only accept Stings and
 * Readers, this must be used.
 * Since the close() method does nothing and a seek() method
 * has been added, this allows us to get some performance
 * improvements through reuse.  It can be used even after the
 * lexer explicitly closes it by seeking to the place that
 * we want to read next, and reseting the lexer.
 *
 *
 */
public class DocumentReader extends Reader {
    /**
     * Current position in the document. Incremented
     * whenever a character is read.
     */
    private long position = 0;

    /**
     * Saved position used in the mark and reset methods.
     */
    private long mark = -1;

    /**
     * The document that we are working with.
     */
    private AbstractDocument document;
    /**
     * Modifying the document while the reader is working is like
     * pulling the rug out from under the reader.  Alerting the
     * reader with this method (in a nice thread safe way, this
     * should not be called at the same time as a read) allows
     * the reader to compensate.
     */
    public void update(int position, int adjustment) {
        if (position < this.position) {
            if (this.position < position - adjustment) {
                this.position = position;
            } else {
                this.position += adjustment;
            }
        }
    }



    /**
     * Construct a reader on the given document.
     *
     * @param document the document to be read.
     */
    public DocumentReader(AbstractDocument document) {
        this.document = document;
    }

    /**
     * Has no effect.  This reader can be used even after
     * it has been closed.
     */
    public void close() {
    }

    /**
     * Save a position for reset.
     *
     * @param readAheadLimit ignored.
     */
    public void mark(int readAheadLimit) {
        mark = position;
    }

    /**
     * This reader support mark and reset.
     *
     * @return true
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Read a single character.
     *
     * @return the character or -1 if the end of the document has been reached.
     */
    public int read() {
        if (position < document.getLength()) {
            try {
                char c = document.getText((int)position, 1).charAt(0);
                position++;
                return c;
            } catch (BadLocationException x) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Read and fill the buffer.
     * This method will always fill the buffer unless the end of the document is reached.
     *
     * @param cbuf the buffer to fill.
     * @return the number of characters read or -1 if no more characters are available in the document.
     */
    public int read(char[] cbuf) {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * Read and fill the buffer.
     * This method will always fill the buffer unless the end of the document is reached.
     *
     * @param cbuf the buffer to fill.
     * @param off offset into the buffer to begin the fill.
     * @param len maximum number of characters to put in the buffer.
     * @return the number of characters read or -1 if no more characters are available in the document.
     */
    public int read(char[] cbuf, int off, int len) {
        if (position < document.getLength()) {
            int length = len;
            if (position + length >= document.getLength()) {
                length = document.getLength() - (int)position;
            }
            if (off + length >= cbuf.length) {
                length = cbuf.length - off;
            }
            try {
                String s = document.getText((int)position, length);
                position += length;
                for (int i = 0; i < length; i++) {
                    cbuf[off + i] = s.charAt(i);
                }
                return length;
            } catch (BadLocationException x) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * @return true
     */
    public boolean ready() {
        return true;
    }

    /**
     * Reset this reader to the last mark, or the beginning of the document if a mark has not been set.
     */
    public void reset() {
        if (mark == -1) {
            position = 0;
        } else {
            position = mark;
        }
        mark = -1;
    }

    /**
     * Skip characters of input.
     * This method will always skip the maximum number of characters unless
     * the end of the file is reached.
     *
     * @param n number of characters to skip.
     * @return the actual number of characters skipped.
     */
    public long skip(long n) {
        if (position + n <= document.getLength()) {
            position += n;
            return n;
        } else {
            long oldPos = position;
            position = document.getLength();
            return (document.getLength() - oldPos);
        }
    }

    /**
     * Seek to the given position in the document.
     *
     * @param n the offset to which to seek.
     */
    public void seek(long n) {
        if (n <= document.getLength()) {
            position = n;
        } else {
            position = document.getLength();
        }
    }
}

