
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
package com.versant.core.jdo.tools.workbench;

import za.co.hemtech.gui.editor.ValueEditSite;
import za.co.hemtech.gui.editor.ValueParser;
import za.co.hemtech.gui.editor.StringValueEditor;
import za.co.hemtech.gui.exp.ExpTableModel;
import za.co.hemtech.gui.DecoratedGrid;
import za.co.hemtech.gui.FlatButton;
import za.co.hemtech.gui.Icons;
import za.co.hemtech.gui.painter.FastGridCellPainterBroker;
import za.co.hemtech.gui.util.PopupPanel;
import za.co.hemtech.gui.util.PopupUtils;
import za.co.hemtech.config.Config;
import com.versant.core.jdo.tools.workbench.model.MdValue;
import com.versant.core.metadata.parser.JdoExtension;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.awt.*;
import java.util.List;

/**
 * This edits an MdValue. It allows a value to be selected from a drop down
 * (i.e. like a choice) but still allows the text to be edited directly.
 * @keep-all
 */
public class MdValueEditor extends StringValueEditor
        implements ValueParser, PopupMenuListener, ActionListener {

    private Config config = Config.getInstance(getClass());
    private MdValue value;
    private boolean defaultSelected;
    private boolean lastKeyForGrid;
    private int stopping;

    private ExpTableModel listModel = new ExpTableModel("listModel");
    private DecoratedGrid grid = new DecoratedGrid("grid");
    private PopupPanel popup = new PopupPanel();
    private JPanel holder = new JPanel(new BorderLayout());

    private FlatButton btnRestore = new FlatButton();
    private FlatButton btnNo = new FlatButton();
    private JToolBar toolbar = new JToolBar();

    private static final String RESTORE_AC = "restore";
    private static final String NO_AC = "no";

    public MdValueEditor() throws Exception {
        setParser(this);
        listModel.setConfig(config);
        grid.setModel(listModel);
        grid.setBorder(null);
        FastGridCellPainterBroker broker = new FastGridCellPainterBroker();
        broker.setCursorPainter(null);  // do not paint cursor
        grid.setCellPainterBroker(broker);
        holder.add(grid, BorderLayout.CENTER);
        popup.addPopupMenuListener(this);
        grid.addActionListener(this);
        grid.addKeyListener(this);
        grid.getCursorModel().setMoveCursorOnEnter(0);
        grid.setSingleClickAction(true);
        grid.getGrid().setRequestFocusOnClick(false);
        grid.setFont(new Font("Monospaced", 0, 12));

        btnRestore.setIcon(Icons.getIcon("default.gif"));
        btnRestore.addActionListener(this);
        btnRestore.setActionCommand(RESTORE_AC);
        btnRestore.setToolTipText("Restore the default value");

        btnNo.setIcon(Icons.getIcon("no.gif"));
        btnNo.addActionListener(this);
        btnNo.setActionCommand(NO_AC);
        btnNo.setToolTipText("Set the field to " + JdoExtension.NO_VALUE);

        toolbar.setFloatable(false);
        toolbar.add(btnRestore);
        toolbar.add(btnNo);
        toolbar.setMargin(new Insets(0, 0, 0, 0));
        toolbar.setBorder(null);

        holder.add(toolbar, BorderLayout.SOUTH);
        popup.add(holder, BorderLayout.CENTER);
    }

    public void startEditing(ValueEditSite site, int x, int y, int width,
            int height, InputEvent e) {
        this.site = site;
        defaultSelected = false;
        value = (MdValue)site.getEditValue();
        if (value.isReadOnly()) {
            site.stopEditing(false);
        } else {
            int maxlen = buildList(value);
            if (maxlen > 0) {
                listModel.getColumnModel().setColumnWidth(0, maxlen + 4);
                defaultSelected = false;
                lastKeyForGrid = false;
                grid.setVisible(true);
            } else {
                grid.setVisible(false);
            }
            super.startEditing(site, x, y, width, height, e);
            if (maxlen > 0 || btnRestore.isVisible() || btnNo.isVisible()) {
                displayDropDown();
                if (value != null){
                    String text = value.getText();
                    if (text == null){
                        text = value.getDefText();
                    }
                    if (!selectComponent(text)){
                        grid.setSelectedIndex(0);
                    }
                }
            }
        }
    }

    /**
     * Build a list of MdValue's consisting of the default value (if any)
     * and values constructed from the String's in the pick list. Returns
     * the length of the longest string in the list or 0 if the list is
     * empty.
     */
    private int buildList(MdValue v) {
        int maxlen = 0;
        List list = listModel.getList().getList();
        list.clear();
        List pl = v.getPickList();
        int n = pl == null ? 0 : pl.size();
        String dv = v.getDefText();
        btnRestore.setVisible(dv != null);
        btnNo.setVisible(false);
        for (int i = 0; i < n; i++) {
            String s = (String)pl.get(i);
            if (s.equals(JdoExtension.NO_VALUE)) {
                btnNo.setVisible(true);
            } else {
                s = v.formatPickListEntry(s);
                list.add(s);
                int len = s.length();
                if (len > maxlen) maxlen = len;
            }
        }
        listModel.listUpdated();
        return maxlen;
    }

    public Object parseValue(Class type, String s) throws Exception {
        return s;
    }

    protected void setEditValue(Object o) throws Exception {
        if (defaultSelected){
            value.setText(null);
        } else {
            String str = (String) o;
            if (!value.isCaseSensitive() && value.isWarningOnError()){
                if (str != null){
                    int index = str.indexOf(' ');
                    if (index != -1){
                        str = str.substring(0, index);
                    }
                }
            }
            value.setText(str);

        }
        site.setEditValue(value);
    }

    public String formatValue(Object o) {
        if (o instanceof MdValue) {
            MdValue v = (MdValue)o;
            String s = v.getText();
            if (s == null) s = v.getDefText();
            return s;
        } else {
            return (String)o;
        }
    }

    /**
     * Forward up and down to our grid. Enter is only sent to the grid if the
     * last key was also sent to the grid.
     */
    protected void handleKeyEvent(KeyEvent e) {
        int c = e.getKeyCode();
        if (c == KeyEvent.VK_UP || c == KeyEvent.VK_DOWN || (lastKeyForGrid
                    && (e.getKeyChar() == '\n' || c == KeyEvent.VK_ENTER))) {
            lastKeyForGrid = true;
            grid.getGrid().processKey(e);
            e.consume();
        } else {
            if (value != null && value.getPickList() != null && !value.getPickList().isEmpty()){
                if (e.getSource() instanceof JTextField){
                    String text = ((JTextField) e.getSource()).getText();
                    selectComponent(text);
                }
            }
            lastKeyForGrid = false;
            super.handleKeyEvent(e);
        }
    }

    /**
     * Set the selected component in the grid, this will take into acount if it is case sensitive
     * @param text
     * @return true if it was set
     */
    private boolean selectComponent(String text) {
        boolean caseSensitive = value.isCaseSensitive();
        if (text == null){
            return false;
        }
        if (!caseSensitive){
            text = text.toLowerCase();
        }
        boolean hasBeenSet = false;
        TableModel model = grid.getTableModel();
        int rows = model.getRowCount();
        String row = null;
        for (int i = 0; i < rows; i++) {
            if (caseSensitive) {
                row = (String) model.getValueAt(i, 0);
            } else {
                row = ((String) model.getValueAt(i, 0)).toLowerCase();
            }
            if (row.startsWith(text)){
                grid.setSelectedIndex(i);
                hasBeenSet = true;
                break;
            }
        }
        return hasBeenSet;

    }

    /**
     * Display our drop down grid.
     */
    protected void displayDropDown() {
        int pr = listModel.getRowCount();
        if (pr > 20) pr = 20;
        grid.setPreferredRows(pr);
        holder.revalidate();
        PopupUtils.showPopupContainingComponent(
            getEditComponent().getBounds(), popup, site.getEditContainer(),
            holder);
        if (grid.getSelectedIndex() < 0) grid.setSelectedIndex(0);
    }

    /**
     * Stop editing keeping changes if our textComponent loses focus and our
     * drop down grid is not visible.
     */
    public void focusLost(FocusEvent e) {
        if (!e.isTemporary() && stopping == 0) {
            site.stopEditing(isAutoSaveChanges());
        }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    /**
     * This method is called before the popup menu becomes invisible.
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        if (stopping == 0) site.stopEditing(false);
    }

    /**
     * This method is called when the popup menu is canceled.
     */
    public void popupMenuCanceled(PopupMenuEvent e) {
        if (stopping == 0) site.stopEditing(false);
    }

    /**
     * The user has selected something in our grid or clicked the restore
     * default or disable buttons. Get the selected object from our list and
     * stop editing. Set a flag if the default value is selected.
     */
    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if (ac != null) {
            if (ac.equals(RESTORE_AC)) {
                defaultSelected = true;
                site.stopEditing(true);
                return;
            } else if (ac.equals(NO_AC)) {
                textComponent.setText(JdoExtension.NO_VALUE);
                site.stopEditing(true);
                return;
            }
        }
        Object o = grid.getSelectedObject();
        if (o != null) {
            textComponent.setText(value.parsePickListEntry((String)o));
            site.stopEditing(true);
        } else {  // grid is probably empty
            site.stopEditing(false);
        }
    }

    /**
     * Make sure our drop down goes away.
     */
    public void stopEditing(boolean saveChanges) throws Exception {
        try {
            stopping++;
            sendMouseExit(btnNo);
            sendMouseExit(btnRestore);
            popup.setVisible(false);
            super.stopEditing(saveChanges);
        } finally {
            stopping--;
        }
    }

    private void sendMouseExit(JComponent c) {
        MouseEvent e = new MouseEvent(c, MouseEvent.MOUSE_EXITED,
            System.currentTimeMillis(), 0, 0, 0, 0, false);
        c.dispatchEvent(e);
    }

    /**
     * Must editing on a cell with this editor start immediately after a mouse
     * click? Normally the first click will move the cursor/focus to the cell
     * and the second click starts editing. This is usefull for checkbox
     * type editors.
     */
    public boolean isQuickEditor() {
        return false;
    }
}

