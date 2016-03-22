<%@ page import="petstore.www.Constants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:message key="order.submitted.title"/></h3>
<layout:panel align="left" width="100%" styleClass="catalog" >
 <p><bean:message key="order.success" arg0="<%=request.getAttribute(Constants.PURCHASE_ID).toString()%>"/>
 </p>
</layout:panel>