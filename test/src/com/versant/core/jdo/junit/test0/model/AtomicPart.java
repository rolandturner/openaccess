
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
package com.versant.core.jdo.junit.test0.model;

import java.util.HashSet;

/**
 * @keep-all
 */
public class AtomicPart extends DesignObj {

    private int x; // X-coordinate location of part
    private int y; // Y-coordinate location of part
    private HashSet to;    // <-> Connection.from
    private HashSet from;  // <-> Connection.to
    private CompositePart partOf;  // <-> CompositePart.parts
    private int docId;  // Id of CompositePart Document

    public AtomicPart() {
        from = new HashSet();
        to = new HashSet();
    }

    public AtomicPart(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public HashSet getTo() {
        return to;
    }

    public void setTo(HashSet to) {
        this.to = to;
    }

    public HashSet getFrom() {
        return from;
    }

    public void setFrom(HashSet from) {
        this.from = from;
    }

    public CompositePart getPartOf() {
        return partOf;
    }

    public void setPartOf(CompositePart partOf) {
        this.partOf = partOf;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

}

 
