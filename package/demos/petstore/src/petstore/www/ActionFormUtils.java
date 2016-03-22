
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */

package petstore.www;

import petstore.db.ContactInfo;

import javax.jdo.PersistenceManager;
import java.lang.reflect.Method;

import org.apache.struts.action.DynaActionForm;


public class ActionFormUtils {

    public static  void populateContactForm(ContactForm cf, ContactInfo ci) {

        cf.setFirstName(ci.getFirstName());
        cf.setLastName(ci.getLastName());
        cf.setStreetName1(ci.getAddress().getStreetName1());
        cf.setStreetName2(ci.getAddress().getStreetName2());
        cf.setCity(ci.getAddress().getCity());
        cf.setState(ci.getAddress().getState());
        cf.setZipCode(ci.getAddress().getZipCode());
        cf.setCountry(ci.getAddress().getCountry());
        cf.setPhone(ci.getTelephone());
        cf.setEmail(ci.getEmail());

    }

    public static void populateContactInfo(ContactForm cf, ContactInfo ci) {

        ci.setLastName(cf.getLastName());
        ci.setFirstName(cf.getFirstName());
        ci.getAddress().setStreetName1(cf.getStreetName1());
        ci.getAddress().setStreetName2(cf.getStreetName2());
        ci.getAddress().setCity(cf.getCity());
        ci.getAddress().setState(cf.getState());
        ci.getAddress().setZipCode(cf.getZipCode());
        ci.getAddress().setCountry(cf.getCountry());
        ci.setTelephone(cf.getPhone());
        ci.setEmail(cf.getEmail());

    }

    public static Object getDynaForm(String idName, Object src, PersistenceManager pm) {
        DynaActionForm dynForm = new DynaActionForm();
        try {
            System.out.println("###############Here1");
            String oid = pm.getObjectId(src).toString();
            System.out.println("###############Here2 Name=" + idName + " OID=" + oid);
            //dynForm.set(idName, oid);
            dynForm.set(idName, idName, oid);
            System.out.println("###############Here3");
            Class cls = src.getClass();
            Method methods[] = cls.getDeclaredMethods();
            System.out.println("###############Here4");
            for (int i=0; i < methods.length; i++) {
                System.out.println("###############Here5");
                if (methods[i].getName().startsWith("get") && (methods[i].getParameterTypes().length == 0)) {
                    System.out.println("###############Here6");
                    Object args[] = {};
                    Object object = methods[i].invoke(src, args);
                    System.out.println("###############Here7");
                    String prop = methods[i].getName().trim().substring(3,3).toLowerCase()
                            + methods[i].getName().trim().substring(4);
                    System.out.println("###############Here8");
                    dynForm.set(prop, object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dynForm;
    }
}
