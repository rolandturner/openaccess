
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
package com.versant.core.jdo.tools.workbench.forms;

import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.model.CursorModel;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;
import com.versant.core.jdo.tools.workbench.XmlPreviewPanel;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.metadata.MDStatics;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.event.InputEvent;
import java.awt.*;

/**
 * View/edit properties for a field. This swaps forms in and out of a tabbed
 * pane depending on the category of the selected field.
 */
public class JDBCFieldPropertyForm extends WorkbenchPanel {

    private ExpTableModel modelField;
    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
    private JLabel errorLabel = new JLabel();
    private FieldPropertyForm.SubForm activeForm;

    private SpecialFieldPropertyForm formSpecial = new SpecialFieldPropertyForm();
    private SimpleFieldPropertyForm formSimple = new SimpleFieldPropertyForm();
    private EmbeddedFieldPropertyForm formEmbedded = new EmbeddedFieldPropertyForm();
    private ExternalizedFieldPropertyForm formExternalized = new ExternalizedFieldPropertyForm();
    private RefFieldPropertyForm formRef = new RefFieldPropertyForm();
    private CollectionFieldPropertyForm formCollection = new CollectionFieldPropertyForm();
    private ObjectFieldPropertyForm formObject = new ObjectFieldPropertyForm();
    private MapFieldPropertyForm formMap = new MapFieldPropertyForm();

    private XmlPreviewPanel xmlPreview = new XmlPreviewPanel("xmlPreview",
            true);

    public JDBCFieldPropertyForm() throws Exception {
        modelField = new ExpTableModel("modelField");
        modelField.setConfig(getConfig());
        setModel(modelField);

        formSpecial.setBusinessLogic(this);
        formSimple.setBusinessLogic(this);
        formEmbedded.setBusinessLogic(this);
        formExternalized.setBusinessLogic(this);
        formRef.setBusinessLogic(this);
        formCollection.setBusinessLogic(this);
        formObject.setBusinessLogic(this);
        formMap.setBusinessLogic(this);

        formSpecial.setModel(modelField);
        formSimple.setModel(modelField);
        formEmbedded.setModel(modelField);
        formExternalized.setModel(modelField);
        formRef.setModel(modelField);
        formCollection.setModel(modelField);
        formObject.setModel(modelField);
        formMap.setModel(modelField);

        //xmlPreview.setModel(modelField);
        //xmlPreview.setColumnName("element");
        setTitle("Field Properties");

        errorLabel.setName("errorLabel");
        tabs.setName("tabs");
        add(tabs);
        add(errorLabel);
        updateSetup();
    }

    public void setMdField(MdField mdField) {
        if (mdField != null) {
            modelField.getList().getList().clear();
            modelField.add(mdField);
        }
    }

    public void tableChanged(TableModelEvent e) {
        updateSetup();
    }

    public void cursorMoved(CursorModel source, int oldRow, int oldCol,
            InputEvent inputEvent) {
        updateSetup();
    }

    private MdField getMdField() {
        return (MdField)modelField.getCursorObject();
    }

    void updateSetup() {
        try {
            Window w = SwingUtilities.windowForComponent(this);
            if (w != null && !w.isVisible()) return;
            MdField f = getMdField();
            FieldPropertyForm.SubForm subForm = getSubFormForField(f);
            if (subForm != null) {
                subForm.updateSetup();
                MdField top = subForm.getTopXmlField();
                MdField bottom = subForm.getBottomXmlField();
                xmlPreview.setElements(top == null ? null : top.getElement(),
                        top == null ? null : top.getMiniClassDef(),
                        bottom == null ? null : bottom.getElement(),
                        bottom == null ? null : bottom.getMiniClassDef());
            } else {
                xmlPreview.setElements(null, null, null, null);
            }
            if (activeForm != subForm) {
                tabs.removeAll();
                if (subForm != null) tabs.add(subForm.getTabTitle(), subForm);
                tabs.add("JDO XML", xmlPreview);
                activeForm = subForm;
            }
            if (f != null && f.hasErrors()) {
                errorLabel.setText(f.getErrorText());
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        } catch (Exception e) {
            za.co.hemtech.gui.util.GuiUtils.dispatchException(this, e);
        }
        tabs.doLayout();
    }

    private FieldPropertyForm.SubForm getSubFormForField(MdField f) {
        if (f == null) return null;
        int category = f.getCategory();
        // If the field is not persistent then use the category it would have
        // when persistent to choose a dialog.
        if (category == MDStatics.CATEGORY_TRANSACTIONAL
                || category == MDStatics.CATEGORY_NONE) {
            int c = f.getCategoryWhenPersistent();
            switch (c) {
                case MDStatics.CATEGORY_POLYREF:
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_COLLECTION:
                case MDStatics.CATEGORY_MAP:
                case MDStatics.CATEGORY_SIMPLE:
                case MDStatics.CATEGORY_EXTERNALIZED:
                case MDStatics.CATEGORY_ARRAY:
                    category = c;
            }
        }
        switch (category) {
            case MDStatics.CATEGORY_DATASTORE_PK:
            case MDStatics.CATEGORY_OPT_LOCKING:
            case MDStatics.CATEGORY_CLASS_ID:
                return formSpecial;
            case MDStatics.CATEGORY_EXTERNALIZED:
                return formExternalized;
            case MDStatics.CATEGORY_SIMPLE:
                return formSimple;
            case MDStatics.CATEGORY_REF:
                if(f.isEmbeddedRef()){
                    return formEmbedded;
                }
                return formRef;
            case MDStatics.CATEGORY_POLYREF:
                return formObject;
            case MDStatics.CATEGORY_ARRAY:
                if (f.isEmbedded()) return formSimple;
            case MDStatics.CATEGORY_COLLECTION:
                return formCollection;
            case MDStatics.CATEGORY_MAP:
                return formMap;
        }
        return null;
    }

    public boolean editCompositePkRefEnabled() {
        MdField f = getMdField();
        return f != null && f.isCompositePkRef();
    }

    public void editCompositePkRefCols() throws Exception {
        return;
    }

    public boolean editLinkTableEnabled() {
        MdField f = getMdField();
        return f != null
                && !f.isForeignKeyCollectionOrArray()
                && !f.isEmbedded();
    }

    public void editLinkTable() throws Exception {
        return;
    }

    public void updateUI() {
        super.updateUI();
        if (formSpecial != null) {
            formSpecial.updateUI();
            formSimple.updateUI();
            formEmbedded.updateUI();
            formExternalized.updateUI();
            formRef.updateUI();
            formCollection.updateUI();
            formObject.updateUI();
            formCollection.updateUI();
            formMap.updateUI();
            xmlPreview.updateUI();
        }
    }

    /**
     * The project has changed.
     */
    public void projectChanged(MdProjectEvent ev) {
        switch (ev.getFlags()) {
            case MdProjectEvent.ID_ENGINE_STARTED:
            case MdProjectEvent.ID_ENGINE_STOPPED:
            case MdProjectEvent.ID_PARSED_DATABASE:
            case MdProjectEvent.ID_DIRTY_FLAG:
            case MdProjectEvent.ID_CLASSES_REMOVED:
//            case MdProjectEvent.ID_PARSED_META_DATA:
            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                return;
        }
        updateSetup();
    }

}

