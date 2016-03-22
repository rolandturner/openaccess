Versant Open Access Eclipse Tools plugin

This plugin requires:
    Eclipse 3.0.1 or higher on Windows
    Eclipse 3.1 or higher on Linux (must run with JDK1.5 or higher)

This plugin has been tested on:
    Eclipse 3.0.1 Windows JDK 1.4.2
    Eclipse 3.1.0M4 Windows JDK 1.5
    Eclipse 3.1.0RC1 Linux JDK 1.5

It is known to not work on 3.1.0RC2 and RC3. We are looking at this problem
but may wait for a final 3.1.0 release before fixing it.

Installation instructions
-------------------------

<ECLIPSE_HOME> is the directory where eclipse is installed.
<OPENACCESS_HOME> is the directory where OpenAccess is installed.

1 Copy the com.versant.voa_@ECLIPSE.TOOLS.VERSION@ dir to <ECLIPSE_HOME>/plugings directory.

2 Copy all the jar files from <OPENACCESS_HOME>/lib to
  <ECLIPSE_HOME>/plugings/com.versant.voa_@ECLIPSE.TOOLS.VERSION@/lib dir.
  
3 Copy the openaccess-tools.jar file from <OPENACCESS_HOME>/tools to
  <ECLIPSE_HOME>/plugings/com.versant.voa_@ECLIPSE.TOOLS.VERSION@/lib dir.

4 Remove any old versions of the OpenAccess plugin
  (e.g. com.versant.voa_* or openaccess-* or jdogenie-*).

5 If eclipse was running then you must restart it.

Using Versant OpenAccess Eclipse Tools plugin
---------------------------------------------
Please refer to about.html in the com.versant.voa_@ECLIPSE.TOOLS.VERSION@ directory. This file is
also displayed by the More Info button on the plugins listing within
Eclipse (Help | About Eclipse Platform | Plug-in Details).
