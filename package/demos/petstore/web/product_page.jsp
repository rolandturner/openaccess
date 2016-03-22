<%@ page import="petstore.www.CatalogAction,
                 java.util.Map,
                 java.util.HashMap"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:message key="productPage.title"/></h3>

<layout:panel align="left" width="100%" styleClass="catalog" >

    <logic:iterate id="product" name="pageForm" property="page.objects">

    <tr align="left"><td>
    <html:link page="/catalog.do?reqCode=listItems&amp;count=2"
               paramId="parent" paramName="product" paramProperty="code" >
        <bean:write name="product" property="name"/>
    </html:link><br>
        <bean:write name="product" property="description"/>
    </td></tr>
    </logic:iterate>


</layout:panel>

<div align="right">
<logic:equal value="true" name="pageForm" property="page.hasPreviousPage" >
<html:link page="/catalog.do?reqCode=listProducts&amp;count=2"
                  name="pageForm" property="prevParameters">
        <bean:message key="catalog.prev"/>
</html:link>
</logic:equal>
<logic:equal value="true" name="pageForm" property="page.hasNextPage" >
<html:link page="/catalog.do?reqCode=listProducts&amp;count=2"
                  name="pageForm" property="nextParameters">
        <bean:message key="catalog.next"/>
</html:link>
</logic:equal>
</div>
