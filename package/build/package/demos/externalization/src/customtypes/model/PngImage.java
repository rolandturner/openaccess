
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
package customtypes.model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.awt.image.*;
import java.awt.*;
import java.net.URL;

/**
 * <p>This is a wrapper for an Image that stores the image in PNG format in
 * the database. It is similar to the Swing ImageIcon class.</p>
 */
public class PngImage implements Icon {

    private transient BufferedImage image;

    public PngImage(URL url) throws IOException {
        image = ImageIO.read(url);
    }

    /**
     * JDO Genie uses this constructor after the bytes for the image have
     * been read from the database.
     */
    public PngImage(byte[] a) {
        try {
            image = ImageIO.read(new ByteArrayInputStream(a));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * JDO Genie calls this method when it needs to store the image in the
     * database.
     */
    public byte[] toBytes() {
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
        try {
            ImageIO.write(image, "PNG", o);
        } catch (IOException e) {
            throw new IllegalStateException(e.toString());
        }
        return o.toByteArray();
    }

    public int getIconHeight() {
        return image.getHeight();
    }

    public int getIconWidth() {
        return image.getWidth();
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(image, x, y, c);
    }

    //= Serialize the image in compressed PNG format =====================

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        image = ImageIO.read(s);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        ImageIO.write(image, "PNG", s);
    }

}
