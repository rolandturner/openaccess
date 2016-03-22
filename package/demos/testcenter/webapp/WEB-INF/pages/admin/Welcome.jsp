<%@ page import="com.versant.testcenter.service.TestCenterService,
                 com.versant.testcenter.model.Administrator,
                 com.versant.testcenter.model.Exam,
                 com.versant.testcenter.web.WebConstants,
                 java.util.Map,
                 java.util.HashMap"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"  %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<jsp:include page="../header.jsp">
	<jsp:param name="title" value="Test Center Administration"/>
</jsp:include>
<%
    Administrator admin = (Administrator) TestCenterService.getCurrentUser();
    pageContext.setAttribute("admin",admin);
%>

<h1>Test Center Administration</h1>

<p>
<html:link page="/public/LogoutSubmit.do" >Logout</html:link><br/>
<html:link page="/admin/Exam.do">Add exam</html:link>
</p>

<p>
<html:form action="/admin/ExamSearchSubmit.do" method="get" >
Search for exams by name:<br>
<html:text property="name" maxlength="50" size="20"/>
<input class="inputAddBut" type="submit" name="action" value="Submit" >
</html:form>
</p>

<logic:present name="<%=WebConstants.EXAM_SEARCH_RESULT%>" >
<p>
<table border="1">
<tr>
    <th>Exam Name</th>
    <th>Category</th>
</tr>
<logic:iterate id="exam" name="<%=WebConstants.EXAM_SEARCH_RESULT%>" type="Exam">
<tr>
    <%
        Map paramMap = new HashMap();
        paramMap.put(WebConstants.EXAM_PARAM, exam.getOID());
        pageContext.setAttribute("paramMap", paramMap);
    %>
    <td>
        <html:link page="/admin/Exam.do" name="paramMap" >
            <bean:write name="exam" property="name"/>
        </html:link>
    </td>
    <td>
        <bean:write name="exam" property="examCategory"/>
    </td>
</tr>
</logic:iterate>
</table>

</p>
</logic:present>

<jsp:include page="../footer.jsp" />