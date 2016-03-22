<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:message key="order.address.title"/></h3>

<layout:form action="/order.do" styleClass="FORM" width="100%" reqCode="createOrder" >

    <tiles:insert page="contact.jsp" >
        <tiles:put name="title" value="order.billing.title" />
        <tiles:put name="prefix" value="billInfo." />
    </tiles:insert>

    <tiles:insert page="contact.jsp" >
        <tiles:put name="title" value="order.shipping.title" />
        <tiles:put name="prefix" value="shipInfo." />
    </tiles:insert>

    <layout:submit reqCode="createOrder"><bean:message key="order.create"/></layout:submit>

</layout:form>
