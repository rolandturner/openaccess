<%@ page import="com.versant.testcenter.web.WebConstants,
                 com.versant.testcenter.service.TestCenterService,
                 java.text.DateFormat,
                 org.apache.struts.util.RequestUtils,
                 java.util.Locale,
                 java.text.SimpleDateFormat"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"  %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/ts-html.tld" prefix="tshtml"%>

<jsp:include page="../header.jsp">
	<jsp:param name="title" value="Maintain exam details"/>
</jsp:include>

<%
    String examOID = request.getParameter(WebConstants.EXAM_PARAM);
    if (examOID != null) {pageContext.setAttribute("examOID", examOID);}
%>
<html:form action="/admin/ExamSubmit.do" focus="name" onsubmit="return validateExamForm(this);" >

<% if (examOID != null) { %>
<input name="<%=WebConstants.EXAM_PARAM%>" type="hidden" value="<%=examOID%>">
<%}%>

<h1>
<% if (examOID != null) { %>
    Maintain exam details
<%} else {%>
    Add Exam
<%}%>
</h1>

<p>* are required fields</p>
<p><html:errors /></p>

<h2>Exam Details</h2>

<table border="0">
<tr>
    <td align="right" class="adminFieldName"><bean:message key="exam.name"/>&nbsp;<b class="red">*</b></td>
    <td>
        <html:text property="name" maxlength="50" size="50"/>
    </td>
</tr>

<tr>
    <td align="right" class="adminFieldName"><bean:message key="exam.description"/>&nbsp;<b class="red">*</b></td>
    <td>
        <html:textarea property="description" />
    </td>
</tr>


<tr>
    <td align="right" class="adminFieldName"><bean:message key="exam.examCategory"/>&nbsp;<b class="red">*</b></td>
    <td>
        <tshtml:select property="examCategory" >
            <html:option value=""  >--NONE--</html:option>
            <% pageContext.setAttribute("examCategories",TestCenterService.findAllExamCategories());%>
            <html:options collection="examCategories" property="OID" labelProperty="name" />
        </tshtml:select>
    </td>
</tr>

<tr>
    <td colspan = "2" align="left">
        <input class="inputAddBut" type="submit" name="action" value="Submit" >
    </td>
</tr>
</table>

</html:form>

<html:javascript formName="examForm"  />
<jsp:include page="../footer.jsp" />
