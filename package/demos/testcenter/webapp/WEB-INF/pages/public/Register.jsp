<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"  %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<jsp:include page="../header.jsp">
	<jsp:param name="title" value="Registration"/>
</jsp:include>

<html:form action="/public/RegisterSubmit.do" focus="firstName" onsubmit="return validateStudentForm(this);" >

<h1>Student Registration</h1>

<p>* are required fields</p>
<p><html:errors /></p>

<table border="0">
    <tr>
        <td align="right" class="adminFieldName"><bean:message key="student.firstName"/>&nbsp;<b class="red">*</b></td>
        <td>
            <html:text property="firstName" maxlength="50" size="50"/>
        </td>
    </tr>

    <tr>
        <td align="right" class="adminFieldName"><bean:message key="student.surname"/>&nbsp;<b class="red">*</b></td>
        <td>
            <html:text property="surname" maxlength="50" size="50"/>
        </td>
    </tr>

    <tr>
        <td align="right" class="adminFieldName"><bean:message key="student.login"/>&nbsp;<b class="red">*</b></td>
        <td>
            <html:text property="login" maxlength="20" size="50"/>
        </td>
    </tr>

    <tr>
        <td align="right" class="adminFieldName"><bean:message key="student.password"/>&nbsp;<b class="red">*</b></td>
        <td>
            <html:password property="password"  maxlength="20" size="50"/>
        </td>
    </tr>

    <tr>
        <td colspan = "2" align="left">
            <input class="inputAddBut" type="submit" name="action" value="Submit" >
        </td>
    </tr>
</table>
</html:form>

<html:javascript formName="studentForm"  />
<jsp:include page="../footer.jsp" />