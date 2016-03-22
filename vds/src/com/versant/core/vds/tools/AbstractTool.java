
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
/*
 * Created on Mar 23, 2004
 *
 * Copyright Versant Corporation, 2004
 */
package com.versant.core.vds.tools;

/**
 * AbstractTool designates what a command-line oriented tool must provide
 * in terms of
 * <LI>specifying command-line options
 * <LI>parsing command-line options in a reusable way
 * <p/>
 * Mar 23, 2004
 *
 * @author ppoddar
 */
public abstract class AbstractTool {

    protected String _usageCommand;
    protected String _bannerInfo;

    /**
     * @return String that is used as a banner for this receiver.
     */
    public final String getBannerInfo() {
        return _bannerInfo;
    }

    /**
     * @return String that is used as an usage command for this receiver.
     */
    public final String getUsageCommand() {
        return _usageCommand;
    }

    /**
     * @param string used as a banner for this receiver.
     */
    public final void setBannerInfo(String string) {
        _bannerInfo = string;
    }

    /**
     * @param string used as an usage command for this receiver.
     */
    public final void setUsageCommand(String string) {
        _usageCommand = string;
    }

}
