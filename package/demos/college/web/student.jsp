<%@page import="util.*,model.*,java.util.*,javax.jdo.*,java.io.*,
                java.text.SimpleDateFormat" %>

<%
    long start = System.currentTimeMillis();
    PersistenceManager pm = Sys.pm();
    String oid = request.getParameter("oid");
    Student student = (Student)pm.getObjectById(
            pm.newObjectIdInstance(Student.class, oid), true);
    String title = "Study plan for " + student.getName();
%>

<html>
    <head><title><%=title%></title></head>
<body>
<h1><%=title%></h1>

<table border="1" cellspacing="1" cellpadding="1">
<tr>
<td><b>Subject</b></td>
<td><b>Code</b></td>
<td><b>Modules selected</b></td>
</tr>

<%
    for (Iterator i = student.getRegistrations().iterator(); i.hasNext(); ) {
        Registration r = (Registration)i.next();
%>
<tr>
<td><%=r.getSubject().getName()%></td>
<td><%=r.getSubject().getCode()%></td>
<td><%=r.getModuleSummary()%>&nbsp;</td>
</tr>
<%
    }
%>
</table>

<p>Page displayed in <%=System.currentTimeMillis() - start%> ms</p>

<%=Sys.getNewSQL()%>

</body>
</html>
