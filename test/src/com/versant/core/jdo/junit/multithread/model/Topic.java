
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
package com.versant.core.jdo.junit.multithread.model;

import java.util.List;
import java.util.ArrayList;

/**
 * For testing multithreaded access.
 */
public class Topic {

    private String name;
    private List messageList = new ArrayList(); // of Message
    private int messageCount;

    public Topic(String name) {
        this.name = name;
    }

    public Topic() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public synchronized List getMessageList() {
        return new ArrayList(messageList);
    }

    public synchronized void addMessage(Message msg) {
        messageList.add(msg);
        ++messageCount;
    }

    public synchronized void removeMessage(Message msg) {
        if (messageList.remove(msg)) {
            --messageCount;
        }
    }

}

