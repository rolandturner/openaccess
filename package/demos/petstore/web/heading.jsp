<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>
<table width="100%">
<tr><td width="50%">
<html:img page="/images/banner_logo.gif" altKey="heading.tooltip" border="0"/>
<td>
<td width="50%" align="right">
    <html:link page="/customer.do?reqCode=showCustomer">
        <bean:message key="account.show"/>
    </html:link>&nbsp;|

    <html:link page="/cart.do?reqCode=showCart">
        <bean:message key="cart.show"/>
    </html:link>&nbsp;|
    <html:link page="/user.do?reqCode=logout">
        <bean:message key="login.signout"/>
    </html:link>
    <br>
</td>
</tr>
</table>
