
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="login"><bean:message key="login.signin"/></h3>
<table width="100%" cellpadding="0" cellspacing="0" border="1">
<tr><td height="100%" class="catalog">

<layout:form action="/user.do?reqCode=login" styleClass="login">

    <html:hidden property="requestURL"/>
    <tr><td align="center" colspan="10"><bean:message key="login.answer1"/></td></tr>

    <layout:text key="login.userName" property="login" styleClass="LABEL"/>
    <layout:password key="login.password" property="password" styleClass="LABEL"/>
    <layout:line><td align="center" colspan="10">
    <html:submit value="Sign In"/>
    </td></layout:line>

</layout:form>

</td>
<td height="100%" class="catalog">

<layout:form action="/user.do?reqCode=createUserCustomer" styleClass="login">

    <tr><td align="center" colspan="10"><bean:message key="login.answer2"/></td></tr>

    <layout:text key="login.userName" property="login" styleClass="LABEL"/>
    <layout:password key="login.password" property="password" styleClass="LABEL"/>
    <layout:password key="login.password1" property="password1" styleClass="LABEL" />
    <layout:line><td align="center" colspan="10">
    <html:submit value="Create New Account"/>
    </td></layout:line>



</layout:form>



</td>
</tr>
</table>
