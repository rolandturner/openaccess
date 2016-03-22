
<%@ taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%>

<jsp:include page="../header.jsp">
	<jsp:param name="title" value="Login"/>
</jsp:include>

<p><i>Welcome to the JDO Genie Testcenter demo. This is a simple application
demonstrating how to use JDO with Struts in a web application. In addition
there is also a Swing GUI client that uses JDO Genie's support for remote
PersistenceManager's to connect to the JDO Genie server running in the
web tier.</i></p>

<p><i>This application manages exam sittings. You can log on as the
administrator (username admin, password admin) and create exams. Or you
can register as a student and manage your exam registrations.</i></p>

<table border="0" cellpadding="3" cellspacing="5" width="480">

    <tr><td height="10"></td></tr>

    <tr>
        <td><b>Login</b></td>
    </tr>

    <tr>
        <td class="tblHome2" width="300" valign="top">
            Enter your User ID into the <b>Username</b> field.
            Then enter your password into the <b>Password</b> field.<br>
            <br>
            Click on the <b>Login</b> button to start your session.<br>
            If you have not yet got and account please <html:link forward="register" >register</html:link>
        </td>
    </tr>

    <tr>
        <td valign="top" >
        <html:form action="/public/LoginSubmit.do" focus="login">
            <table cellpadding="3" cellspacing="0" border="0">
            <tr>
                <td class="genError" colspan="2"><html:errors /></td>
            </tr>
            <tr>
                <td class="gen"><b><bean:message key="login.login" /></b></td>
                <td><html:text property="login" /></td>
            </tr>
            <tr>
                <td class="gen"><b><bean:message key="login.password" /></b></td>
                <td><html:password property="password"/></td>
            </tr>
            <tr>
                <td colspan="2" align="right"><input class="inputLoginBut" type="submit" value="Login" /></td>
            </tr>
            </table>
        </html:form>
        </td>
    </tr>
</table>

<jsp:include page="../footer.jsp" />


