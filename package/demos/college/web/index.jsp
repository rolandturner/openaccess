<%@page import="util.*,model.*,java.util.*,javax.jdo.*,java.io.*" %>

<%
    String name = request.getParameter("name");
    name = name == null ? "" : name;
    String s = request.getParameter("count");
    int count = s == null ? 10 : Integer.parseInt(s);
%>

<html>
    <head><title>Students</title></head>
<body>
<h1>Students</h1>

<table border="1" cellspacing="1" cellpadding="1">
<tr><td><b>Student</b> <font size="-1"><i>Click to view study spec</i></font></td></tr>

<%
    long start = System.currentTimeMillis();
    Query q = Sys.pm().newQuery(Student.class, "name >= p");
    q.declareParameters("String p, String jdoGenieOptions");
    q.setOrdering("name ascending");
    Collection col = (Collection)q.execute(name,
            "fetchGroup=name_only;maxRows=" + count);
    int c = 0;
    Student student = null;
    for (Iterator i = col.iterator(); c < count && i.hasNext(); c++) {
        student = (Student)i.next();
        String oid = Sys.pm().getObjectId(student).toString();
%>
<tr>
<td><a href="student.jsp?oid=<%=oid%>"><%=student.getName()%></a></td>
</tr>

<%
    }
    q.closeAll();
%>
</table>

<p>
<% if (student != null) { %>
    <a href="index.jsp?name=<%=student.getName()%>&count=<%=count%>">Next</a>
<% } %>
</p>

<p>
<form action="index.jsp" method="GET">
Starting name <input type="text" name="name" value="<%=name%>" size="20"><br>
Students per page <input type="text" name="count" value="<%=count%>" size="4">
<input type="submit" value="GO">
</form>
</p>

<p>Page displayed in <%=System.currentTimeMillis() - start%> ms</p>

<%=Sys.getNewSQL()%>

</body>
</html>
