<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>

    <tiles:useAttribute name="title" classname="String" />
    <tiles:useAttribute name="prefix" classname="String" />
    <tr><td colspan="10">
    <h3 class="catalog"><bean:message key="<%=title%>"/></h3>
    </td></tr>
    <tr><td>
    <%
        String firstNameProp = prefix + "firstName";
        String lastNameProp = prefix + "lastName";
        String streetName1Prop = prefix + "streetName1";
        String streetName2Prop = prefix + "streetName2";
        String cityProp = prefix + "city";
        String stateProp = prefix + "state";
        String zipCodeProp = prefix + "zipCode";
        String phoneProp = prefix + "phone";
        String emailProp = prefix + "email";
    %>
    <layout:text key="customer.firstName" property="<%=firstNameProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.lastName" property="<%=lastNameProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.street1" property="<%=streetName1Prop%>" styleClass="LABEL"/>
    <tr><td>
    <layout:text key="customer.street2" property="<%=streetName2Prop%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.city" property="<%=cityProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.state" property="<%=stateProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.zipCode" property="<%=zipCodeProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <%
        //Workaround for layout taglib select tag bug
        if (prefix.trim().equalsIgnoreCase("contact.")) {
    %>
        <layout:select key="customer.country" property="contact.country" styleClass="LABEL">
            <layout:options collection="countries" property="value" labelProperty="label" />
        </layout:select>
    <%
        } else {
    %>
        <layout:select key="customer.country" property="billInfo.country" styleClass="LABEL">
            <layout:options collection="countries" property="value" labelProperty="label" />
        </layout:select>
    <%
        }
    %>
    </td></tr>
    <tr><td>
    <layout:text key="customer.telephone" property="<%=phoneProp%>" styleClass="LABEL"/>
    </td></tr>
    <tr><td>
    <layout:text key="customer.email" property="<%=emailProp%>" styleClass="LABEL"/>
    </td></tr>


