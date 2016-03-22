
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
package com.versant.core.jdbc.sql.conv;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for converting Strings to and from byte and character
 * streams.
 * @keep-all
 */
public class StreamUtils {

    private static final int BUF_LEN = 4096;

    /**
     * Buffer to hold a chunk of character data read from a stream.
     */
    public static class CharBuf {
        public char[] cbuf = new char[BUF_LEN];
        public CharBuf next;
    }

    /**
     * Buffer to hold a chunk of byte data read from the stream.
     */
    public static class ByteBuf {
        public byte[] bbuf = new byte[BUF_LEN];
        public ByteBuf next;
    }

    /**
     * Read all of the characters from in into a String and close in.
     */
    public static String readAll(Reader in) throws IOException {
        CharBuf root = new CharBuf();
        int sz = in.read(root.cbuf);
        if (sz < 0) {
            in.close();
            return "";
        }
        if (sz < BUF_LEN) {
            in.close();
            return new String(root.cbuf, 0, sz);
        }
        int bufCount = 0;
        for (CharBuf buf = root;;) {
            buf = buf.next = new CharBuf();
            ++bufCount;
            sz = in.read(buf.cbuf);
            if (sz < 0) {
                sz = 0;
                break;
            }
            if (sz < BUF_LEN) break;
        }
        in.close();
        int tot = bufCount * BUF_LEN + sz;
        char[] a = new char[tot];
        int pos = 0;
        for (CharBuf buf = root; ; buf = buf.next) {
            if (buf.next == null) {
                if (sz > 0) System.arraycopy(buf.cbuf, 0, a, pos, sz);
                break;
            } else {
                System.arraycopy(buf.cbuf, 0, a, pos, BUF_LEN);
                pos += BUF_LEN;
            }
        }
        return new String(a);
    }

    /**
     * Read all of the bytes from in into a String and close in.
     */
    public static String readAll(InputStream in, String encoding) throws IOException {
        ByteBuf root = new ByteBuf();
        int sz = in.read(root.bbuf);
        if (sz < 0) {
            in.close();
            return "";
        }
        if (sz < BUF_LEN) {
            in.close();
            return new String(root.bbuf, 0, sz, encoding);
        }
        int bufCount = 0;
        for (ByteBuf buf = root;;) {
            buf = buf.next = new ByteBuf();
            ++bufCount;
            sz = in.read(buf.bbuf);
            if (sz < 0) {
                sz = 0;
                break;
            }
            if (sz < BUF_LEN) break;
        }
        in.close();
        int tot = bufCount * BUF_LEN + sz;
        byte[] a = new byte[tot];
        int pos = 0;
        for (ByteBuf buf = root; ; buf = buf.next) {
            if (buf.next == null) {
                if (sz > 0) System.arraycopy(buf.bbuf, 0, a, pos, sz);
                break;
            } else {
                System.arraycopy(buf.bbuf, 0, a, pos, BUF_LEN);
                pos += BUF_LEN;
            }
        }
        return new String(a, encoding);
    }

    /**
     * Read all of the bytes from in into a byte[] and close in.
     */
    public static byte[] readAll(InputStream in) throws IOException {
        ByteBuf root = new ByteBuf();
        int sz = in.read(root.bbuf);
        if (sz < 0) {
            in.close();
            return new byte[0];
        }
        if (sz < BUF_LEN) {
            in.close();
            byte[] a = new byte[sz];
            System.arraycopy(root.bbuf, 0, a, 0, sz);
            return a;
        }
        int bufCount = 0;
        for (ByteBuf buf = root;;) {
            buf = buf.next = new ByteBuf();
            ++bufCount;
            sz = in.read(buf.bbuf);
            if (sz < 0) {
                sz = 0;
                break;
            }
            if (sz < BUF_LEN) break;
        }
        in.close();
        int tot = bufCount * BUF_LEN + sz;
        byte[] a = new byte[tot];
        int pos = 0;
        for (ByteBuf buf = root; ; buf = buf.next) {
            if (buf.next == null) {
                if (sz > 0) System.arraycopy(buf.bbuf, 0, a, pos, sz);
                break;
            } else {
                System.arraycopy(buf.bbuf, 0, a, pos, BUF_LEN);
                pos += BUF_LEN;
            }
        }
        return a;
    }

}

 
