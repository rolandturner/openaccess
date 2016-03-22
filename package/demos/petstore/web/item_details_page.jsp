<%@ page import="petstore.www.CatalogAction,
                 java.util.Map,
                 java.util.HashMap,
                 petstore.www.Constants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:write name="<%=CatalogAction.ITEM_KEY%>" property="category.name"/></h3>
<layout:panel align="left" width="100%" styleClass="catalog" >
    <tr align="left"><td>
    <bean:define id="imageLocation" name="<%=CatalogAction.ITEM_KEY%>" property="imageLocation"/>
    <html:img page="<%=String.valueOf(imageLocation)%>"/>
    </td>
    <td align="left">
        <bean:write name="<%=CatalogAction.ITEM_KEY%>" property="name"/>&#32;-&#32;
        <bean:write name="<%=CatalogAction.ITEM_KEY%>" property="code"/><br>
        <bean:write name="<%=CatalogAction.ITEM_KEY%>" property="description"/><br><br>
        <bean:message key="catalog.listPrice"/>:
        <bean:write name="<%=CatalogAction.ITEM_KEY%>" property="listPriceString"/><br><br>
        <%
            String oid = (String) request.getAttribute(CatalogAction.ITEM_OID_KEY);
            String link = "/cart.do?reqCode=addItem&amp;" + Constants.ITEM_PRM + "=" + oid.trim();
        %>
        <html:link page="<%=link%>">
        <bean:message key="cart.add" />
        </html:link>
    </td>
    </tr>
</layout:panel>
