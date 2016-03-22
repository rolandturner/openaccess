<%@ page import="petstore.www.CatalogAction,
                 java.util.Collection,
                 petstore.model.Page,
                 java.util.Iterator"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>


<layout:panel align="left" width="100%" styleClass="sidebar" key="sidebar.title" >

<%
    Page p = (Page) request.getAttribute(CatalogAction.CATEGORY_PAGE_KEY);
    Iterator oids = p.getOids().iterator();

%>
    <logic:iterate id="category" name="<%=CatalogAction.CATEGORY_PAGE_KEY%>" property="objects">
    <%
        String oid = (String) oids.next();
        String link = "/catalog.do?reqCode=listItems&amp;count=2&amp;parent=" + oid.trim();
    %>
    <tr align="left"><td>
    <html:link page="<%=link%>">
        <bean:write name="category" property="name"/>
    </html:link>
    </td></tr>
    </logic:iterate>

</layout:panel>
