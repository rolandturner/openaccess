
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
package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import com.versant.core.common.Debug;

import java.rmi.dgc.VMID;
import java.util.Date;

/**
 * @author so,lth
 *         <p/>
 *         diese Klasse steht genau hier (service package) um aus dem Service eine Zugriffsm?glichkeit auf private Attribute des Materials
 *         zu bekommen. Werkzeuge sollen diese Attribute (FlagOriginal) nicht manipulieren, sondern nur abfragen k?nnen Es handelt sich um
 *         ein "Thing", das ?ber den LockManager verwaltet werden kann.
 *         <p/>
 *         Diese Klasse implementiert nun DuplicatableThing direkt, um Problemen mit JDO aus dem Weg zu gehen. Dazu wurden Methoden aus
 *         AbstactDuplicatableThing und AbstractThing kopiert. Dadurch wurde man die ?berschattung der JWAM-Klassen los.
 */
public class PersistentThingImpl implements PersistentThing {

    // Verwaltung aus Abstract Thing
    private String _id = null;
    private String _originalId = "";

    /**
     * Das Material wird in den Zustand 'Neu erzeugt' versetzt.
     */
    public PersistentThingImpl() {
        setID(IdentificatorDV.identificatorDV(new VMID().toString()));
    }

    /**
     * @see Thing#getDisplayName()
     */
    public String getDisplayName() {
        return "PersistentThing";
    }

    /**
     * @return
     */
    public Date getLetztesSpeicherDatum() {
        return null;
    }

    /**
     * @param speicherDatum
     */
    protected void setLetztesSpeicherDatum(Date speicherDatum) {
    }

    final void getHandle() {

    }

    public boolean isEditable() {
        return false;
    }

    /**
     * @param isEditable The isEditable to set.
     */
    final void setEditable(boolean isEditable) {
    }

    /**
     * @return Returns the isPersistent.
     */
    public boolean isPersistent() {
        return false;
    }

    /**
     * @param isPersistent The isPersistent to set.
     */
    public final void setPersistent(boolean isPersistent) {
    }

    // folgende Methoden sind wg. Verwaltung von Abstract Thing ...

    final private void setID(IdentificatorDV id) {
//        assert id != null : "Precondition violated: id";
        if (Debug.DEBUG){
            Debug.assertInternal(id != null , "Precondition violated: id");      
        }
        _id = id.toString();
//        assert getID() != null : "Postondition violated: getID()";
        if (Debug.DEBUG) {
            Debug.assertInternal(getID() != null, "Postondition violated: getID()");
        }
    }

    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Thing) {
            result = getID().equals(((Thing)obj).getID());
        }
        return result;
    }

    public int hashCode() {
        return Thing.class.hashCode() + getID().hashCode();
    }

    public String toString() {
        return getDisplayName();
    }

    public IdentificatorDV getID() {
//        assert _id != null : "Postcondition violated: _id";
        if (Debug.DEBUG) {
            Debug.assertInternal(_id != null , "Postcondition violated: _id");
        }
        return IdentificatorDV.identificatorDV(_id);
    }

    public Object adaptTo(Class aspect) {
//        assert aspect != null : "Precondition violated: aspect not null";
//
//        Object result = null;
//
//        if (aspect.isInstance(this))
//        {
//            result = this;
//        }
//        else
//        {
//            result = AdapterRegistry.getInstance().adapt(this, aspect);
//        }
//
//        assert result == null || aspect.isInstance(result) : "Postcondition violated: result == null || aspect.isInstance(result)";
//
//        return result;
        throw new UnsupportedOperationException(
                "Wir brauchen keine Adapter!!! tp & rb 05.08.2004");
    }

    // folgende Methoden sind wg. Verwaltung aus Duplicatable Thing ...

    public void setOriginalID(IdentificatorDV id) {
    }

    public IdentificatorDV originalID() {
        return null;
    }

    public boolean isCopy() {
        return false;
    }

    /**
     * Methode, die die Konsistenz eines Materials pr?ft Standardimplementierung return false Materialien die bei bearbeitbaren
     * TAbellen genutzt werden m?ssen diese Methode ?berschreiben!
     *
     * @return
     */
    public boolean checkMaterial() {
        return false;
    }

    final void setHandle() {
    }

    public Class getMaterialInterface() {
        return null;
    }
}
