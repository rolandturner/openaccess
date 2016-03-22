<%@ page import="petstore.www.CatalogAction,
                 java.util.Map,
                 java.util.HashMap,
                 petstore.www.CartAction,
                 petstore.model.CartItem,
                 java.util.Iterator,
                 petstore.model.CartVO,
                 petstore.www.Constants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-layout.tld" prefix="layout" %>
<%! CartItem item; %>



<logic:notEqual value="0" name="<%=CartAction.CART_KEY%>" property="size">
<h3 class="cart"><bean:message key="cartPage.title"/></h3>
<layout:panel align="left" width="100%" styleClass="cart" >
<html:form action="/cart.do?reqCode=updateCart">

    <logic:iterate id="cartItem" name="cartForm" property="cartItem">

    <tr align="left"><td width="40%">
    <html:link page="/catalog.do?reqCode=showItem"
               paramId="item" paramName="cartItem" paramProperty="item.itemId" >
        <bean:write name="cartItem" property="item.name"/>
    </html:link>
    </td>

    <td align="center" width="20%" nowrap>
        <html:link page="/cart.do?reqCode=removeItem"
               paramId="item" paramName="cartItem" paramProperty="item.itemId" >
        <bean:message key="cart.remove" />
        </html:link>
    </td>

    <td align="right" width="20%">
        <html:text name="cartItem" property="quantity" indexed="true" size="3"/>
    </td>

    <td align="right" width="20%">
        @&nbsp;<bean:write name="cartItem" property="item.listPriceString"/><br>
    </td>

    </tr>
    </logic:iterate>
    <tr>
    <td>
        <html:submit value="Update Cart"></html:submit>
    </td><td></td>
    <td align="right"><bean:message key="cartPage.subtotal"/>:</td>
    <td align="right"><bean:write name="<%=CartAction.CART_KEY%>" property="totalCostString"/></td>
    </tr>
</html:form>
</layout:panel>
<p align="right">
<html:link page="/order.do?reqCode=confirmCheckout">
        <bean:message key="cart.checkout"/>
</html:link>
</p>
</logic:notEqual>
<logic:equal value="0" name="<%=CartAction.CART_KEY%>" property="size">
<h3 class="cart"><bean:message key="cartPage.emptyTitle"/></h3>
</logic:equal>
