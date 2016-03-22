Simple tutorial on using JDO Genie from a session bean with jca.

NB! This demo requires ant1.6 and above.
Edit the common.properties as needed.

The common.xml file defines all the available targets and these are implemented
in the application server specific xml file (eg. weblogic.xml).
If you want to run agains weblogic then execute as follows:
ant -f weblogic.xml NAME_OF_TARGET
This can also be achieved by 'copying' the application server specific build file
to 'build.xml'. Using 'soft links' would be a better choice instead of copying
the file if you OS supports it(eg. Linux).  

There is a walkthrough for this tutorial in Appendix C of the JDO Genie
manual. Please open docs/index.html in your browser to access the manual.

There are important extra steps required to run this tutorial on WebLogic
and WebSphere that are explained in the manual.
