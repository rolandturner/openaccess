Versant Open Access JDO @JDO.VERSION@ (@JDO.VERSION.DATE@)

VOA source code can be obtained from http://www.versant.com/opensource
under the Eclipse Public License.

This file describes what is currently happening with the VOA source code.
We will update it from time to time. Please see the top level build.xml
in the source distribution for instructions on how to build Versant Open
Access.

Submitting changes
------------------
If you make changes to VOA source please do so against a copy checked out
from CVS. Create patch files and send them to us so we can apply and
review the changes.

19 April 2005
-------------
We are busy refactoring many parts of Open Access especially the JDBC
support. So if you are looking at the code, expect it to change frequently
as we complete sections of work.

The static FetchGroup stuff in the meta data is being replaced with a
completely dynamic mechanism that will make it easy for us to support JDO 2
FetchPlan's and the prefetching built into EJBQL queries. The current SQL
query processing code produces very good SQL for JDOQL queries but has
been hacked too many times.

The Eclipse Tools plugin currently uses a mix of SWT and Swing code shared
with the standalong Swing Workbench. We are busy converting all of this code
to SWT. The new standalong Workbench will be Eclipse based.

