<%@ page import="petstore.www.CatalogAction,
                 java.util.Map,
                 java.util.HashMap,
                 petstore.model.Page,
                 java.util.Iterator,
                 petstore.www.PageForm,
                 petstore.www.Constants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:message key="itemPage.title"/></h3>

<layout:panel align="left" width="100%" styleClass="catalog" >

<%
    PageForm form = (PageForm) request.getAttribute("pageForm");
    Iterator oids = form.getPage().getOids().iterator();

%>
    <logic:iterate id="item" name="pageForm" property="page.objects">
    <%
        String oid = (String) oids.next();
        String showLink = "/catalog.do?reqCode=showItem&amp;" + Constants.ITEM_PRM + "=" + oid.trim();
        String addLink = "/cart.do?reqCode=addItem&amp;" + Constants.ITEM_PRM + "=" + oid.trim();
    %>
    <tr align="left"><td width="100%">
    <html:link page="<%=showLink%>">
        <bean:write name="item" property="name"/>&#32;-&#32;
        <bean:write name="item" property="code"/>
    </html:link><br>
        <bean:write name="item" property="description"/>
    </td>
    <td align="right" nowrap>
        <bean:write name="item" property="listPriceString"/><br>
        <html:link page="<%=addLink%>">
        <bean:message key="cart.add" />
        </html:link>
    </td>

    </tr>
    </logic:iterate>


</layout:panel>
<div align="right">
<logic:equal value="true" name="pageForm" property="page.hasPreviousPage" >
<html:link page="/catalog.do?reqCode=listItems&count=2"
                  name="pageForm" property="prevParameters">
        <bean:message key="catalog.prev"/>
</html:link>
</logic:equal>
<logic:equal value="true" name="pageForm" property="page.hasNextPage" >
<html:link page="/catalog.do?reqCode=listItems&count=2"
                  name="pageForm" property="nextParameters">
        <bean:message key="catalog.next"/>
</html:link>
</logic:equal>
</div>
