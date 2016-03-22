
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
package tut1.gui;

import tut1.model.Item;
import tut1.util.JDOSupport;

import javax.swing.*;
import javax.jdo.JDOHelper;
import java.awt.*;

/**
 * Dialog for adding new Items to the system or editing of existing Items.
 * Uses JDOHelper.isPersistent(Object) to check whether an Item is new or existing.
 *
 */
public class AddEditItemDialog extends DialogBase {
    private Item item;
    private JTextField fieldCode = new JTextField();
    private JTextField fieldDescription = new JTextField();
    private JTextField fieldCategory = new JTextField();


    public AddEditItemDialog(DialogBase owner,Item item) {
        /*
         Set our title depending on whether we are working
         with a new item or editing an existing one.
         */
        super(owner,JDOHelper.isPersistent(item) ? "Edit Item: "+item: "Add Item", true);
        JDOSupport.commit();
        this.item = item;

        JPanel fieldPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());

        fieldCode.setColumns(10);
        fieldCode.setText(item.getCode());

        fieldDescription.setColumns(10);
        fieldDescription.setText(item.getDescription());

        fieldCategory.setColumns(20);
        refreshCategoryField();
        fieldCategory.setEditable(false);

        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(createButton("OK", "ok"),new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        buttonPanel.add(createButton("Cancel", "cancel"),new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));

        fieldPanel.add(new JLabel("Code"),new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldCode,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(new JLabel("Description"),new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldDescription,new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        fieldPanel.add(new JLabel("Category"),new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.EAST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(fieldCategory,new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        fieldPanel.add(createButton("Change", "changeCategory"),new GridBagConstraints(3,2,1,1,0,0,GridBagConstraints.WEST,
                GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        fieldPanel.add(buttonPanel,new GridBagConstraints(0,3,4,1,1,1,GridBagConstraints.SOUTH,
                GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));

        getContentPane().add(fieldPanel, BorderLayout.CENTER);

        setSize(400, 400);
        setLocationRelativeTo(owner);
    }

    private void refreshCategoryField() {
        fieldCategory.setText(item.getCategory() == null ? "<CATEGORY NOT SET>" :item.getCategory().getName());
    }

    public Item getItem() {
        return item;
    }

    public void changeCategory() {
        ChooseCategoryDialog dlg = new ChooseCategoryDialog(this);
        dlg.setVisible(true);
        if (dlg.getCategory() == null) return;
        item.setCategory(dlg.getCategory());
        refreshCategoryField();
    }

    public void ok() {
        if (fieldDescription.getText() == null || fieldCode.getText() == null || item.getCategory() == null) return;
        item.setCode(fieldCode.getText());//@todo Only set if has been changed
        item.setDescription(fieldDescription.getText());

        /*
        Check if the item is a new 1 or an existing 1.
        If the item is a new 1, make it persitant.
        */
        if (!JDOHelper.isPersistent(item)) {
            JDOSupport.getPM().makePersistent(item);
        }
        //Save our changes made...
        JDOSupport.commit();
        dispose();
    }

    public void cancel() {
        //Discard any changes made...
        JDOSupport.rollback();
        item = null;
        dispose();
    }
}

