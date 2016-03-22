Versant OpenAccess Eclipse plugin
This plugin has been tested on Eclipse 2.1.3 and Eclipse 3

Installation instructions
-------------------------

<ECLIPSE_HOME> is the directory where eclipse is installed.
<OPENACCESS_HOME> is the directory where OpenAccess is installed.

1 Copy the com.versant.voa_@ECLIPSE.VERSION@ dir to <ECLIPSE_HOME>/plugings directory.

2 Copy openaccess.jar, jta.jar and activation.jar from <OPENACCESS_HOME>/lib to
  <ECLIPSE_HOME>/plugings/com.versant.voa_@ECLIPSE.VERSION@/lib dir.

3 Remove any old versions of the OpenAccess plugin
  (e.g. com.versant.voa_* or openaccess-* or jdogenie-*).

4 If eclipse was running then you must restart it.

Using OpenAccess Plugin
----------------------
Please refer to about.html in the com.versant.voa_@ECLIPSE.VERSION@ directory. This file is
also displayed by the More Info button on the plugins listing within
Eclipse (Help | About Eclipse Platform | Plug-in Details).
