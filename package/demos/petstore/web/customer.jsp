<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

<h3 class="catalog"><bean:message key="customer.title"/></h3>

<layout:form action="/customer.do" styleClass="FORM" width="100%" reqCode="editCustomer" >

    <tiles:insert page="contact.jsp" >
        <tiles:put name="title" value="customer.contact.title" />
        <tiles:put name="prefix" value="contact." />
    </tiles:insert>


    <tr><td colspan="10">
    <h3 class="catalog"><bean:message key="customer.creditCard.title"/></h3>
    </td><tr>
    <layout:text key="customer.creditCard.number" property="cardNumber" styleClass="LABEL" size="30" maxlength="30"/>
    <layout:select key="customer.creditCard.type" property="cardType" styleClass="LABEL">
        <layout:options collection="creditCards" property="value" labelProperty="label" />
    </layout:select>

    <tr><th class="LABEL">
    <bean:message key="customer.creditCard.expiry"/>
    </td><td colspan="10"><table align="left">
    <layout:line>
    <layout:select key="" property="expiryMonth" styleClass="LABEL">
        <layout:option value="01"  />
    </layout:select>

    <layout:select key="customer.creditCard.expiry.year" property="expiryYear" styleClass="LABEL">
        <layout:option value="2004"  />
    </layout:select>
    </layout:line>
    </table>
    </td></tr>
    <tr><td align="center">
    <layout:submit reqCode="editCustomer" mode="N,N,D"><bean:message key="customer.edit"/></layout:submit>
    <layout:submit reqCode="updateCustomer" mode="N,D,N"/>
    </td></tr>


</layout:form>
