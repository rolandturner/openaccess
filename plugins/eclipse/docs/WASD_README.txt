Versant OpenAccess WASD (WebSphere Application Studio Developer) plugin
This plugin has been tested on WASD 5.1.2

Installation instructions
-------------------------

<WASD_HOME> is the directory where WebSphere Application Studio Developer is installed.
<OPENACCESS_HOME> is the directory where OpenAccess is installed.

1 Copy the com.versant.voa_@ECLIPSE.VERSION@ dir to <WASD_HOME>/eclipse/plugings directory.

2 Copy openaccess.jar, jta.jar and activation.jar from <OPENACCESS_HOME>/lib to
  <WASD_HOME>/eclipse/plugings/com.versant.voa_@ECLIPSE.VERSION@/lib dir.

3 Remove any old versions of the OpenAccess plugin
  (e.g. com.versant.voa_* or openaccess-* or jdogenie-*).

4 If WASD was running then you must restart it.


Using OpenAccess Plugin
----------------------
Please refer to about.html in the openaccess-1.0.1 directory. This file is
also displayed by the (More Info) button on the plugins listing within
WASD (Help | About WebSphere Studio Application Developer | Plug-in Details).