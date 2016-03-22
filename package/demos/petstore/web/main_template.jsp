<%@ page import="petstore.www.RequestUtils"%>
 <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>
<%@ page contentType="text/html" language="java" %>
<%! Object title=""; %>



<tiles:useAttribute name="title" ignore="true" />
<% if (title==null) title="";%>
<html>
<head>
    <html:base />
    <title><bean:message key="main.title" arg0="<%= String.valueOf(title)%>" /></title>

    <layout:skin />
    <!--script language="Javascript" src="/petstore/config/javascript.js"/-->

</head>

<body>

    <table border="0" cellpadding="0" cellspacing="0" valign="top">

    <tr>
        <td colspan="3">
        <tiles:insert attribute="heading" /><br/>
        <hr noshade="noshade" size="1" width="100%"/>
        </td>
    </tr>
    <tr><td height="8" valign="top" align="right">&nbsp;</td></tr>

    <tr>

        <td valign="top" width="180">
            <tiles:insert attribute="sidebar" />
        </td>
        <td width="10" valign="top">&nbsp;</td>

        <td height="400" width="506" valign="top">
            <tiles:insert attribute="content" />
        </td>


    </tr>


    </table>



</body>
</html>

<% RequestUtils.closePersistenceManager(request); %>
