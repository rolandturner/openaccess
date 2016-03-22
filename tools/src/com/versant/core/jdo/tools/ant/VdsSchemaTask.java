
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
 * Created on Aug 23, 2004
 */

package com.versant.core.jdo.tools.ant;

import java.util.*;
import com.versant.core.vds.tools.jdo.SchemaTool;


/**
 * @author hzhao
 */

/**
 * This ant task will create/evolve the VDS schema for a set of .jdo files and
 * classes. The classes do not have to be enhanced. The schema will be created
 * in VDS database specified in the project file. This class is a wrapper class
 * of {@link com.versant.core.vds.tools.jdo.SchemaTool} which should be used
 * outside of Ant by creating an instance and setting properties or by using
 * the mainmethod and command line args. {@see com.versant.jdo.tools.SchemaTool}
 * for options and arguments.
 */
public class VdsSchemaTask extends JdoTaskBase {

    private ArrayList cmdArgs = new ArrayList();
  

    public void execute() {
	    cmdArgs.add("-cp");
      cmdArgs.add(classpath.toString());
      cmdArgs.add("-p");
      cmdArgs.add(configFilename);

      String[] args = new String[cmdArgs.size()];
      cmdArgs.toArray(args);

      try {
        SchemaTool.main(args);
      } catch (Exception exp) {
        System.err.println (exp);
        exp.printStackTrace();
      }
    }


    public void setOutputdir(String out) {
      cmdArgs.add("-out");
      cmdArgs.add(out);
    }

    public void setDefine(String s) {
      if (isTrue(s)) { 
        cmdArgs.add("-action");
        cmdArgs.add("define");
      }
    }

    public void setCompare(String s) {
      if (isTrue(s)) {
        cmdArgs.add("-action");
        cmdArgs.add("compare");
      }
    }

    public void setEvolve(String s) {
      if (isTrue(s)) {
        cmdArgs.add("-action");
        cmdArgs.add("evolve");
      }
    }

    private static boolean isTrue(String s) {
        return "*".equals(s) || "true".equals(s);
    }
}
