Versant Open Access JDO @JDO.VERSION@ (@JDO.VERSION.DATE@)

Fast Java Data Objects implementation for relational databases
(c)2005 Versant http://www.versant.com

Thank you for downloading Versant Open Access. Versant Open Access is now
affiliated with the Eclipse JSR220-ORM Project.

http://www.versant.com/opensource
http://www.eclipse.org/proposals/eclipse-jsr220-orm/index.html

This release includes alpha level support for the EJB 3 persistence
EntityManager and EJBQL. See README_EJB3.txt for more information.

Introduction
------------
Versant Open Access JDO is a Java Data Objects (JDO) implementation for
relational databases and Versant ODBMS focussing on performance, flexible
mapping, optimistic transactions and distributed PersistenceManagers. It
implements version 1.0.1 of the JDO specification found at
http://access1.sun.com/jdo/.

JDO provides for easy, transparent access to persistent data via a neutral
query syntax. Developers using JDO do not have to write any JDBC code or
SQL queries. Objects are persisted and queried through a PersistenceManager
implementation provided by the JDO vendor. Classes are mapped to the
physical store using JDO meta data. This mapping may be changed without
changing the application classes making it easy to support different
databases and schemas.

Installation
------------
Uncompress this archive if you have not already done so.

The binary distribution of Versant Open Access includes an evaluation license
in the license directory for the extra commercial components not included in
the source distribution (remote access, standalone Workbench).  You should add 
openaccess.license to openaccess.jar as follows:

jar uf lib/openaccess.jar -C license openaccess.license

The source distribution does not include any components that require a license
file.

Versant Open Access has been tested on JDK 1.3.1_02, 1.4.2_03 and JDK 1.5.0
on Gentoo Linux (2.6 kernel) and Sun JDK 1.4.2_03 on Windows 2000.
Any 1.4.x or 1.5.x JDK should work. JDK 1.3.1 is not supported when using
Versant ODBMS. JDK 1.2 is not supported.

Versant ODBMS Support
---------------------
Versant Open Access supports VDS release 6.0.5.2K and will not work
with other current versions of VDS. An Enterprise or Evaluation license
is required to use Versant Open Access with VDS.

Support
-------
Support for evaluations is provided on our discussion forums:
http://www.jdogenie.com/forums/index.jsp

Directory Structure
-------------------
bin/            - Scripts and batch files
docs/           - Documentation (including user manual).
demos/          - Simple demo applications.
demos/hsqldb    - Hypersonic databases for the demos.
lib/            - JAR files needed for the Open Access runtime and Ant tasks.
tools/          - Additional JAR files for the Open Access Workbench.
license/        - Put your openaccess.license file here.
src/            - Java source for converter, name generator and event classes.
templates/      - Templates for new project wizard.
plugins/        - Plugins for IDEs (e.g. Eclipse)

Quickstart
----------
1) Run bin/hypersonic.sh (or .bat) to start Hypersonic database server.
2) Open a new prompt and run bin/workbench.sh (or .bat) to start the Workbench.
3) Select Tut1 from the open project dialog.
4) Choose Build | Ant | run from the menu bar to run the demo.

Acknowledgements
----------------
Versant Open Access would not have been possible without the work of the JDO
expert group (JSR00012).

This product uses software developed by the Apache Software Foundation
(http://www.apache.org). See docs/LICENSE_APACHE.txt for details.

This product uses software developed by the JDOM Project
(http://www.jdom.org/). See docs/LICENSE_JDOM.txt for details.

This product uses the Alloy Look and Feel from Incors
(http://www.incors.com).

This product uses the BeanShell Java interpreter (http://www.beanshell.org).
This is distributed under the GNU Lesser Public License
(http://www.gnu.org/licenses/lgpl.html) and the Sun Public License
(http://www.sun.com/developers/spl.html).

This product uses the JGraph Java graph component (http://www.jgraph.org).
This is distributed under the GNU Lesser Public License
(http://www.gnu.org/licenses/lgpl.html).

This product uses the JFreeChart Java charting component
(http://www.jfree.org/jfreechart). This is distributed under the GNU
Lesser Public License (http://www.gnu.org/licenses/lgpl.html).

This product includes the Hypersonic SQL embedded Java database. Please
see docs/LICENSE_HYPERSONIC.txt for license details.

This product includes a modified version of the Pizza compiler. Please
see docs/LICENSE_pizza.txt for license details. The full source code for the
version of Pizza used in this product is available here:
http://downloads.hemtech.co.za/jdo/versant-pizza.zip


