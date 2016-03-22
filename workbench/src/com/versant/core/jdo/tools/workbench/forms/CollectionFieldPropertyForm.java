
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

import za.co.hemtech.config.Config;
import za.co.hemtech.gui.FormPanel;
import za.co.hemtech.gui.form.FormLayout;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdo.tools.workbench.model.DotDotDotColumn;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.jdo.externalizer.SerializedExternalizer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * View/edit properties for a collection or array[] field.
 */
public class CollectionFieldPropertyForm extends FieldPropertyForm.SubForm {

    private FormPanel formMapping = new FormPanel("formMapping");
    private JRadioButton radLinkTable;
    private JRadioButton radOneToMany;
    private JRadioButton radManyToMany;
    private JRadioButton radExternalize;
    private JRadioButton radNotPersistent;
    private JRadioButton radTransactional;

    private static final int LINK_TABLE = 1;
    private static final int ONE_TO_MANY = 2;
    private static final int MANY_TO_MANY = 3;
    private static final int NOT_PERSISTENT = 4;
    private static final int TRANSACTIONAL = 5;

    private MdField field;
    private MdField fieldMM;    // non-inv side of many-to-many
    private MdField fieldMMinv; // inv side of many-to-many
    private int forceMapping;

    private FormPanel formElementType = new FormPanel("formElementType");
    private FormPanel formScoFactory = new FormPanel("formScoFactory");

    private FormPanel formLinkTable = new FormPanel("formLinkTable");
    private FieldERGraphForm formGraph = new FieldERGraphForm();
    private FieldFetchGroupForm formFetchGroup = new FieldFetchGroupForm();
    private FieldOrderingForm formFieldOrdering = new FieldOrderingForm();
    private JCheckBox chkDependent = new JCheckBox();

    private FormPanel formOneToMany = new FormPanel("formOneToMany");
    private JCheckBox chkDeleteOrphans = new JCheckBox();
    private JCheckBox chkIndexInverse = new JCheckBox();
    private JCheckBox chkManaged = new JCheckBox();

    private FormPanel formManyToMany = new FormPanel("formManyToMany");
    private JPanel formMMholder = new JPanel(new GridLayout(1, 2));

    private FormPanel formMM = new FormPanel("formMM");
    private JLabel labTitleMM = new JLabel();
    private FieldFetchGroupForm formFetchGroupMM = new FieldFetchGroupForm(
            true);
    private FieldOrderingForm formFieldOrderingMM = new FieldOrderingForm(true);
    private JCheckBox chkDependentMM = new JCheckBox();

    private FormPanel formMMinv = new FormPanel("formMMinv");
    private JLabel labTitleMMinv = new JLabel();
    private FieldFetchGroupForm formFetchGroupMMinv = new FieldFetchGroupForm(
            true);
    private FieldOrderingForm formFieldOrderingMMinv = new FieldOrderingForm(
            true);
    private JCheckBox chkDependentMMinv = new JCheckBox();

    // for link table and many-to-many mapping
    private MdLinkTable linkTable = new MdLinkTable();

    // for one-to-many mapping
    private MdClassTable ownerTable = new MdClassTable();
    private MdClassTable elementTable = new MdClassTable();
    private MdJdbcRef ref = new MdJdbcRef();

    public CollectionFieldPropertyForm() throws Exception {

        formMapping.setBusinessLogic(this);
        formLinkTable.setBusinessLogic(this);
        formOneToMany.setBusinessLogic(this);
        formElementType.setBusinessLogic(this);
        formScoFactory.setBusinessLogic(this);
        formManyToMany.setBusinessLogic(this);
        formMM.setBusinessLogic(this);
        formMMinv.setBusinessLogic(this);

        formMapping.setConfig(getConfig());
        formLinkTable.setConfig(getConfig());
        formOneToMany.setConfig(getConfig());
        formElementType.setConfig(getConfig());
        formScoFactory.setConfig(getConfig());
        formManyToMany.setConfig(getConfig());
        formMM.setConfig(getConfig());
        formMMinv.setConfig(getConfig());

        radLinkTable = createRad("radLinkTable", "Link table",
                "Store contents of collection in a link or join table");
        radOneToMany = createRad("radOneToMany", "One-to-Many",
                "Use an inverse foreign key in the element table to complete collection");
        radManyToMany = createRad("radManyToMany", "Many-to-Many",
                "Use a link or join table shared by two collections");
        radExternalize = createRad("radExternalize", "Externalized",
                "Convert to/from byte[] or other type for storage (e.g. using serialization)");
        radNotPersistent = createRad("radNotPersistent", "Not persistent",
                "Do not store or manage field");
        radTransactional = createRad("radTransactional", "Transactional",
                "Manage field accross transaction boundaries but do not store");
        ButtonGroup grpMap = new ButtonGroup();
        grpMap.add(radLinkTable);
        grpMap.add(radOneToMany);
        grpMap.add(radManyToMany);
        grpMap.add(radExternalize);
        grpMap.add(radNotPersistent);
        grpMap.add(radTransactional);
        formMapping.add(radLinkTable);
        formMapping.add(radOneToMany);
        formMapping.add(radManyToMany);
        formMapping.add(radExternalize);
        formMapping.add(radNotPersistent);
        formMapping.add(radTransactional);
        formMapping.setTitle("Mapping");

        chkDependent.setName("chkDependent");
        chkDependent.setText("Delete objects in collection/array when " +
                "owning object is deleted");
        chkDependent.addActionListener(this);

        chkDependentMM.setName("chkDependentMM");
        chkDependentMM.setText("Delete elements when owner is deleted");
        chkDependentMM.addActionListener(this);

        chkDependentMMinv.setName("chkDependentMMinv");
        chkDependentMMinv.setText("Delete elements when owner is deleted");
        chkDependentMMinv.addActionListener(this);

        chkDeleteOrphans.setName("chkDeleteOrphans");
        chkDeleteOrphans.setText("delete-orphans");
        chkDeleteOrphans.setToolTipText("Delete orphaned collection elements");
        chkDeleteOrphans.addActionListener(this);

        chkIndexInverse.setName("chkIndexInverse");
        chkIndexInverse.setText("Index inverse");
        chkIndexInverse.setToolTipText("Create an index on the inverse field");
        chkIndexInverse.addActionListener(this);

        chkManaged.setName("chkManaged");
        chkManaged.setText(
                "Fill the other side of the relationship automatically (managed)");
        chkManaged.setToolTipText("When one side of relationship is changed " +
                "automatically fill the other side");
        chkManaged.addActionListener(this);

        formGraph.getGraph().setAutoOriginX(50);

        formLinkTable.add(formFetchGroup);
        formLinkTable.add(chkDependent);
        formLinkTable.add(formGraph);
        formLinkTable.add(formFieldOrdering);

        formOneToMany.add(chkDeleteOrphans);

        labTitleMM.setName("labTitleMM");
        setupMMTitle(labTitleMM);
        formMM.add(labTitleMM);
        formMM.add(formFetchGroupMM);
        formMM.add(formFieldOrderingMM);
        formMM.add(chkDependentMM);

        labTitleMMinv.setName("labTitleMMinv");
        setupMMTitle(labTitleMMinv);
        formMMinv.add(labTitleMMinv);
        formMMinv.add(formFetchGroupMMinv);
        formMMinv.add(formFieldOrderingMMinv);
        formMMinv.add(chkDependentMMinv);

        formMMholder.add(formMM);
        formMMholder.add(formMMinv);

        formMMholder.setName("formMMholder");
        formManyToMany.add(formMMholder);

        add(formMapping);
    }

    private JRadioButton createRad(String name, String text, String tip) {
        JRadioButton b = new JRadioButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        b.addActionListener(this);
        return b;
    }

    private void setupMMTitle(JLabel label) {
        label.setBackground(UIManager.getColor("textHighlight"));
        label.setForeground(UIManager.getColor("textHighlightText"));
        label.setOpaque(true);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        formLinkTable.setModel(getModel());
        formOneToMany.setModel(getModel());
        formManyToMany.setModel(getModel());
        formMapping.setModel(getModel());
        formFetchGroup.setModel(getModel());
        formFieldOrdering.setModel(getModel());
        formFetchGroupMM.setModel(getModel());
        formFieldOrderingMM.setModel(getModel());
        formFetchGroupMMinv.setModel(getModel());
        formFieldOrderingMMinv.setModel(getModel());
        formElementType.setModel(getModel());
        formScoFactory.setModel(getModel());
    }

    /**
     * Decide how our field is mapped by looking at the meta data. In the case
     * of a many-to-many this will also set fieldMM and fieldMMinv before
     * returning. If not many-to-many then fieldMM and fieldMMinv are set
     * to null.
     */
    private int analyzeFieldMapping() {
        fieldMM = fieldMMinv = null;
        switch (field.getPersistenceModifierInt()) {
            case MDStatics.PERSISTENCE_MODIFIER_PERSISTENT:
                if (forceMapping != 0) return forceMapping;
                if (field.getInverseStr() != null) {
                    MdField mm = field.getInverseField();
                    if (mm != null && mm.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                        fieldMM = mm;
                        fieldMMinv = field;
                        return MANY_TO_MANY;
                    } else {
                        return ONE_TO_MANY;
                    }
                } else {
                    // we might be referenced as an inverse for a many-to-many
                    MdField f = field.getFieldWeAreInverseFor();
                    if (f != null && f.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                        fieldMM = field;
                        fieldMMinv = f;
                        return MANY_TO_MANY;
                    }
                }
                return LINK_TABLE;
            case MDStatics.PERSISTENCE_MODIFIER_NONE:
                return NOT_PERSISTENT;
            case MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL:
                return TRANSACTIONAL;
        }
        return LINK_TABLE;
    }

    /**
     * Update this form to match our field.
     */
    public void updateSetup() {

        MdField newField = getMdField();
        if (field != newField) {
            field = newField;
            forceMapping = 0;
        }

        boolean pcElementType = field.getElementTypeMdClass() != null;
        boolean array = field.getCategory() == MDStatics.CATEGORY_ARRAY;
        radOneToMany.setEnabled(pcElementType);
        radManyToMany.setEnabled(pcElementType && !array);

        // persistence and mapping
        int mapping = analyzeFieldMapping();
        switch (mapping) {
            case LINK_TABLE:
                radLinkTable.setSelected(true);
                updateSetupLinkTable();
                setActive(formLinkTable);
                break;
            case ONE_TO_MANY:
                radOneToMany.setSelected(true);
                updateSetupOneToMany();
                setActive(formOneToMany);
                break;
            case MANY_TO_MANY:
                radManyToMany.setSelected(true);
                updateSetupManyToMany();
                setActive(formManyToMany);
                break;
            case NOT_PERSISTENT:
                radNotPersistent.setSelected(true);
                setActive(null);
                break;
            case TRANSACTIONAL:
                radTransactional.setSelected(true);
                setActive(null);
                break;
        }
    }

    private void moveComponents(FormPanel dest, Component[] a) {
        boolean changed = false;
        for (int i = 0; i < a.length; i++) {
            Component c = a[i];
            if (c.getParent() != dest) {
                dest.add(c);
                changed = true;
            }
            if (!c.isVisible()) {
                c.setVisible(true);
                changed = true;
            }
        }
        if (changed) {
            Config cfg = config.getSubConfig(dest.getName());
            // load the layout
            FormLayout l = (FormLayout)dest.getLayout();
            l.load(cfg);
            dest.revalidate(); // reload layout etc.

        }
    }

    private void updateSetupLinkTable() {

        // grab shared components
        moveComponents(formLinkTable, new Component[]{
            formGraph, formFetchGroup, formFieldOrdering, chkDependent,
            formElementType, formScoFactory});

        hideComponentsIfArray();

        formFetchGroup.updateSetup(field);
        formFieldOrdering.updateSetup(field, true);
        updateChkDependent();

        // link table
        if (linkTable != null) {
            field.initMdLinkTable(linkTable);
        }

        // refresh diagram
        if (isReloadingClasses()) {
            formGraph.getGraph().setEnabled(false);
        } else {
            formGraph.getDiagram().clear();
            addToDiagram(linkTable, formGraph.getDiagram());
            formGraph.getGraph().refresh();
            formGraph.getGraph().setEnabled(true);
        }
    }

    private void hideComponentsIfArray() {
        if (field.getCategory() == MDStatics.CATEGORY_ARRAY) {
            formElementType.setVisible(false);
            formScoFactory.setVisible(false);
            chkManaged.setVisible(false);
        }
    }

    private void updateChkDependent() {
        chkDependent.setText("Delete objects in " +
                (field.getCategory() == MDStatics.CATEGORY_ARRAY
                ? "array"
                : "collection") +
                " when owning " + field.getMdClass().getName() + " is deleted");
        chkDependent.setEnabled(field.getElementTypeMdClass() != null);
        chkDependent.setSelected(
                field.getDependentBool() || !chkDependent.isEnabled());
    }

    private void updateSetupOneToMany() {

        // grab shared components
        moveComponents(formOneToMany, new Component[]{
            formGraph, formFetchGroup, formFieldOrdering, chkDependent,
            chkIndexInverse, chkManaged, formElementType, formScoFactory});

        hideComponentsIfArray();

        formFetchGroup.updateSetup(field);
        formFieldOrdering.updateSetup(field, false);
        updateChkDependent();

        MdClass elementType = field.getElementTypeMdClass();
        MdField inv = field.getInverseField();
        String etn;
        if (elementType != null) {
            etn = elementType.getName();
        } else {
            etn = field.getElementTypeStr();
            if (etn == null) etn = "element-type";
        }

        // index on inverse field
        chkIndexInverse.setText("Create an index on " +
                (inv == null ? "inverse field" : inv.getMiniQName()));
        chkIndexInverse.setSelected(!JdoExtension.NO_VALUE.equals(
                field.getInverseJdbcIndexStr()));

        // managed
        if (inv == null || inv instanceof FakeOne2ManyField) {
            chkManaged.setVisible(false);
        } else {
            chkManaged.setSelected(field.isManaged());
            chkManaged.setText("Update " + inv.getMiniQName() + " when elements are " +
                    "added/removed from the collection");
            chkManaged.setVisible(true);
        }

        // delete-orphans
        StringBuffer s = new StringBuffer();
        s.append("Delete ");
        s.append(etn);
        s.append(" instances with null ");
        List list = elementType == null ? null : elementType.getRefFieldsUsedAsInverse();
        if (list == null || list.isEmpty()) {
            s.append("inverse field");
        } else if (list.size() == 1) {
            s.append(((MdField)list.get(0)).getName());
            s.append(" field");
        } else {
            int n = list.size();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    if (i < n - 1) {
                        s.append(", ");
                    } else {
                        s.append(" and ");
                    }
                }
                s.append(((MdField)list.get(i)).getName());
            }
            s.append(" fields");
        }
        s.append(" on commit (orphans)");
        if (inv != null) {
            chkDeleteOrphans.setText(s.toString());
            chkDeleteOrphans.setEnabled(inv != null);
            chkDeleteOrphans.setSelected(elementType != null
                    && "true".equals(elementType.getDeleteOrphansStr()));
            chkDeleteOrphans.setVisible(true);
        } else {
            chkDeleteOrphans.setVisible(false);
        }

        // setup owner and element tables with reference from inverse field
        MdClass owner = field.getMdClass();
        ownerTable.init(owner);
        ownerTable.addCol(DotDotDotColumn.INSTANCE);
        boolean self = elementType != null && elementType.isSameTable(owner);
        if (elementType != null && inv != null) {
            if (!self) elementTable.init(elementType);
            JdbcField invJdbcField = inv.getJdbcField();
            ref.init(inv.getElement(), self ? ownerTable : elementTable, owner,
                    ownerTable, owner.getMdDataStore(), owner.getMdPackage(),
                    invJdbcField == null ? null : invJdbcField.mainTableCols);
            ref.setOneToMany(true);
            if (self) {
                ref.addColsToTable(ownerTable);
                ownerTable.addCol(DotDotDotColumn.INSTANCE);
                ownerTable.addRef(ref);
            } else {
                elementTable.addCol(DotDotDotColumn.INSTANCE);
                ref.addColsToTable(elementTable);
                elementTable.addCol(DotDotDotColumn.INSTANCE);
                elementTable.addRef(ref);
            }
        }

        // refresh diagram
        if (isReloadingClasses()) {
            formGraph.getGraph().setEnabled(false);
        } else {
            formGraph.getDiagram().clear();
            formGraph.getDiagram().addTable(ownerTable);
            if (elementType != null && !self) {
                formGraph.getDiagram().addTable(elementTable);
            }
            formGraph.getGraph().refresh();
            formGraph.getGraph().setEnabled(true);
        }
    }

    public boolean hasValidElementType() {
        return field.getColElementTypeMdClass() != null;
    }

    private String getMMCollectionTitle(MdField f) {
        return " " + f.getMiniQName() + " " + f.getShortTypeStr();
    }

    private void updateSetupManyToMany() {

        // grab shared components
        moveComponents(formManyToMany, new Component[]{
            formGraph, chkIndexInverse, chkManaged, formElementType,
            formScoFactory});

        labTitleMM.setText(getMMCollectionTitle(fieldMM));
        formFetchGroupMM.updateSetup(fieldMM);
        formFieldOrderingMM.updateSetup(fieldMM, true);
        chkDependentMM.setSelected(fieldMM.getDependentBool());

        labTitleMMinv.setText(getMMCollectionTitle(fieldMMinv));
        formFetchGroupMMinv.updateSetup(fieldMMinv);
        formFieldOrderingMMinv.updateSetup(fieldMMinv, fieldMM);
        chkDependentMMinv.setSelected(fieldMMinv.getDependentBool());

        // index on inverse field
        chkIndexInverse.setText("Create an index on link table columns for " +
                fieldMMinv.getMdClass().getName());
        chkIndexInverse.setSelected(!JdoExtension.NO_VALUE.equals(
                fieldMMinv.getInverseJdbcIndexStr()));

        // managed
        chkManaged.setSelected(fieldMM.isManaged());
        chkManaged.setText("Update " + fieldMMinv.getMiniQName() + " when elements are " +
                "added/removed from " + fieldMM.getMiniQName());

        // make sure MM or MMinv is on the left depending on which is
        // the active field
        Component left = formMMholder.getComponent(0);
        if (field == fieldMM) {
            if (left != formMM) {
                formMMholder.removeAll();
                formMMholder.add(formMM);
                formMMholder.add(formMMinv);
            }
        } else {
            if (left != formMMinv) {
                formMMholder.removeAll();
                formMMholder.add(formMMinv);
                formMMholder.add(formMM);
            }
        }

        // link table
        fieldMM.initMdLinkTable(linkTable);
        linkTable.setComment("Link or join table shared by both fields holding references " +
                "to classes on each side of the many-to-many");

        // refresh diagram
        if (isReloadingClasses()) {
            formGraph.getGraph().setEnabled(false);
        } else {
            formGraph.getDiagram().clear();
            addToDiagram(linkTable, formGraph.getDiagram());
            formGraph.getGraph().refresh();
            formGraph.getGraph().setEnabled(true);
        }
    }

    public boolean actionPerformedImp(Object o) throws Exception {
        if (o == chkDependent) {
            field.setDependentStr(chkDependent.isSelected() ? "true" : null);
        } else if (o == chkDependentMM) {
            fieldMM.setDependentStr(
                    chkDependentMM.isSelected() ? "true" : null);
        } else if (o == chkDependentMMinv) {
            fieldMMinv.setDependentStr(
                    chkDependentMMinv.isSelected() ? "true" : null);
        } else if (o == chkDeleteOrphans) {
            MdClass et = field.getColElementTypeMdClass();
            if (et != null) {
                et.setDeleteOrphansStr(
                        chkDeleteOrphans.isSelected() ? "true" : null);
            }
        } else if (o == radLinkTable) {
            changeToLinkTable();
        } else if (o == radOneToMany) {
            return changeToOneToMany();
        } else if (o == radManyToMany) {
            return changeToManyToMany();
        } else if (o == radNotPersistent) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_NONE);
            forceMapping = 0;
        } else if (o == radTransactional) {
            field.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL);
            forceMapping = 0;
        } else if (o == chkIndexInverse) {
            String s = chkIndexInverse.isSelected() ? null : JdoExtension.NO_VALUE;
            if (fieldMMinv != null) {
                fieldMMinv.setInverseJdbcIndexStr(s);
            } else {
                field.setInverseJdbcIndexStr(s);
            }
        } else if (o == chkManaged) {
            field.setManaged(chkManaged.isSelected());
        } else if (o == radExternalize) {
            field.setExternalizerStr(SerializedExternalizer.SHORT_NAME);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Break the one-to-many or many-to-many inverse link.
     */
    private void changeToLinkTable() {
        if (fieldMMinv != null) {
            fieldMMinv.setInverseStr(null);
        } else {
            field.setInverseStr(null);
        }
        field.setPersistenceModifierInt(MDStatics.NOT_SET);
        forceMapping = 0;
    }

    /**
     * Make sure there is a suitable inverse field (reference to us) that has
     * not already been used for another one-to-many mapping. If the mapping
     * is currently many-to-many then break inverse link between the fields.
     */
    private boolean changeToOneToMany() {
        MdClass ec = field.getElementTypeMdClass();
        if (ec == null) return false;
        if (fieldMMinv != null || field.getInverseStr() == null) {
            List pos = field.getPossibleOneToManyInverseFields();
            String newInvFieldName = FieldMetaData.NO_FIELD_TEXT;
            for (Iterator i = pos.iterator(); i.hasNext();) {
                MdField f = (MdField)i.next();
                MdField inv = f.getInverseField();
                if (inv == null || inv.getFieldWeAreInverseFor() == field) {
                    newInvFieldName = f.getName();
                    break;
                }
            }
//            if (newInvFieldName == null) {
//                StringBuffer s = new StringBuffer();
//                s.append("<html><body><table width=\"500\"><tr><td>");
//                String ocn = field.getMdClass().getName();
//                String ecn = ec.getName();
//                s.append("<p>The element type class (");
//                s.append(ecn);
//                s.append(") does not have any reference fields of type ");
//                s.append(ocn);
//                s.append(
//                        " available for use in a one-to-many mapping. You need to add a ");
//                s.append("field like this to ");
//                s.append(ecn);
//                s.append(":</p><p><code>private ");
//                s.append(ocn);
//                s.append(" parent;</code></p>");
//                int n = pos.size();
//                if (n > 0) {
//                    s.append("<p>The following suitable reference field");
//                    s.append(n == 1 ? "" : "s");
//                    s.append(
//                            "are already being used by other one-to-many collections:<br><ul>");
//                    for (int i = 0; i < n; i++) {
//                        MdField f = (MdField)pos.get(i);
//                        s.append("<li><code>");
//                        s.append(f.getMiniQName());
//                        s.append("</code> used by <code>");
//                        MdField fi = f.getFieldWeAreInverseFor();
//                        s.append(fi == null ? "(null?)" : fi.getMiniQName());
//                        s.append("</code>");
//                    }
//                    s.append("</ul></p>");
//                }
//                s.append("</td></tr></table></body></html>");
//                JOptionPane.showMessageDialog(this, s.toString());
//                return false;
//            }
            if (fieldMMinv != null) {
                // currently many-to-many so get rid of inverse
                fieldMMinv.setInverseStr(null);
            }
            field.setInverseStr(newInvFieldName);
        }
        field.setPersistenceModifierInt(MDStatics.NOT_SET);
        forceMapping = 0;
        return true;
    }


    /**
     * Make sure there is a suitable inverse field (collection of us) that has
     * not already been used for another many-to-many mapping. If the mapping
     * is currently one-to-many then break inverse link between the fields.
     */
    private boolean changeToManyToMany() {
        MdClass ec = field.getColElementTypeMdClass();
        if (ec == null) return false;
        MdField inv = field.getInverseField();
        boolean oneToMany = inv != null && inv.getCategory() == MDStatics.CATEGORY_REF;
        if (field.getInverseStr() == null || oneToMany) {
            List pos = getPossibleManyToManyInverseFields();
            boolean foundNone = true;
            for (Iterator i = pos.iterator(); i.hasNext();) {
                MdField f = (MdField)i.next();
                inv = f.getInverseField();
                boolean invFake = inv instanceof FakeOne2ManyField;
                if (inv == null || invFake || inv.getFieldWeAreInverseFor() == field) {
                    if(invFake){
                        f.setInverseStr(null);
                    }
                    field.setInverseStr(f.getName());
                    foundNone = false;
                    break;
                }
            }
            if (foundNone) {
                StringBuffer s = new StringBuffer();
                s.append("<html><body><table width=\"500\"><tr><td>");
                String ocn = field.getMdClass().getName();
                String ecn = ec.getName();
                s.append("<p>The element type class (");
                s.append(ecn);
                s.append(") does not have any Collection fields with " +
                        "element-type ");
                s.append(ocn);
                s.append(" available for use in a many-to-many mapping.</p>");
                int n = pos.size();
                if (n > 0) {
                    s.append("<p>The following suitable Collection field");
                    s.append(n == 1 ? "" : "s");
                    s.append(" are already being used by other many-to-many " +
                            "collections:<br><ul>");
                    for (int i = 0; i < n; i++) {
                        MdField f = (MdField)pos.get(i);
                        s.append("<li><code>");
                        s.append(f.getMiniQName());
                        s.append("</code> used by <code>");
                        MdField fi = f.getFieldWeAreInverseFor();
                        s.append(fi == null ? "(null?)" : fi.getMiniQName());
                        s.append("</code>");
                    }
                    s.append("</ul></p>");
                }
                s.append("</td></tr></table></body></html>");
                JOptionPane.showMessageDialog(this, s.toString());
                return false;
            }
        }
        field.setPersistenceModifierInt(MDStatics.NOT_SET);
        forceMapping = 0;
        return true;
    }

    /**
     * Find all the fields that could be used as our inverse for a many-to-many
     * mapping. This will include fields that have already been used as
     * an inverse for another field.
     */
    private List getPossibleManyToManyInverseFields() {
        ArrayList ans = new ArrayList();
        MdClass ourClass = field.getMdClass();
        for (MdClass et = field.getColElementTypeMdClass(); et != null;
             et = et.getPcSuperclassMdClass()) {
            List list = et.getFieldList();
            int n = list.size();
            for (int i = 0; i < n; i++) {
                MdField f = (MdField)list.get(i);
                if (f.getCategory() == MDStatics.CATEGORY_COLLECTION
                        && f.getColElementTypeMdClass() == ourClass
                        && f != field) {
                    ans.add(f);
                }
            }
        }
        return ans;
    }

    public MdField getBottomXmlField() {
        if (fieldMM == null) {
            // for one-to-many
            MdField inverseField = field.getInverseField();
            if (inverseField instanceof FakeOne2ManyField) {
                return null;
            }
            return inverseField;
        } else {
            if (field == fieldMM) {
                return fieldMMinv;
            } else {
                return fieldMM;
            }
        }
    }

    public String getTabTitle() {
        return "Collection";
    }
}
