<%@ page import="com.versant.testcenter.model.Student,
                 com.versant.testcenter.model.Exam,
                 com.versant.testcenter.service.TestCenterService,
                 com.versant.testcenter.web.WebConstants"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"  %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<jsp:include page="../header.jsp">
	<jsp:param name="title" value="Welcome to Test Center"/>
</jsp:include>

<%
    Student student = (Student) TestCenterService.getCurrentUser();
    pageContext.setAttribute("student", student);
%>

<h1>Student Exam Management</h1>

<p>Welcome to Test Center <b><bean:write name="student" property="firstName" /></b>!</p>

<p><html:link page="/public/LogoutSubmit.do">Logout</html:link></p>

<p>
<html:form action="/student/ExamDeleteSubmit.do" >
<table border="1">
<tr>
    <th>Registered Exam</th>
    <th>Category</th>
    <th>Deregister</th>
</tr>
<logic:iterate id="exam" name="student" property="exams" type="Exam">
<tr>
    <td><bean:write name="exam" property="name"/></td>
    <td><bean:write name="exam" property="examCategory"/></td>
    <td><html:multibox property="exams" value="<%=exam.getOID()%>"/></td>
</tr>
</logic:iterate>
<tr >
    <td colspan="3">
        <input class="inputAddBut" type="submit" name="action" value="Submit" >
    </td>
</tr>
</table>
</html:form>
</p>

<p>
<html:form action="/student/ExamSearchSubmit.do" method="get" >
Search for exams by name:<br>
<html:text property="name" maxlength="50" size="20"/>
<input class="inputAddBut" type="submit" name="action" value="Submit" >
</html:form>
</p>

<logic:present name="<%=WebConstants.EXAM_SEARCH_RESULT%>" >
<p>
<html:form action="/student/ExamAddSubmit.do" >
<table border="1">
<tr>
    <th>Exam Name</th>
    <th>Category</th>
    <th>Register</th>
</tr>
<logic:iterate id="exam" name="<%=WebConstants.EXAM_SEARCH_RESULT%>" type="Exam">
<tr>
    <td><bean:write name="exam" property="name"/></td>
    <td><bean:write name="exam" property="examCategory"/></td>
    <td><html:multibox property="exams" value="<%=exam.getOID()%>" /></td>
</tr>
</logic:iterate>
<tr >
<td colspan="3">
    <input class="inputAddBut" type="submit" name="action" value="Submit" >
</td>
</tr>
</table>
</html:form>
</p>
</logic:present>

<jsp:include page="../footer.jsp" />