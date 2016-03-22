
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
import za.co.hemtech.gui.exp.ExpColumn;
import com.versant.core.jdo.tools.workbench.WorkbenchPanel;

import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Select one or more sources.
 * @keep-all
 */
public class ChooseSourceFileForm extends WorkbenchPanel implements TreeModel{

    public static final int SHOW_AS_FLAT_TREE = 0;
    public static final int SHOW_AS_FULL_TREE = 1;
    public static final int SHOW_AS_LIST = 2;

    private ExpTableModel modelSourceFiles = new ExpTableModel("modelSourceFiles");
    private JTree tree = new JTree();
    private JTextField searchField = new JTextField();
    private List packages;
    private int showAs;
    private List lastSearchResult;
    private List sources;

    private JRadioButton radFlat;
    private JRadioButton radTree;
    private JRadioButton radList;

    public ChooseSourceFileForm(List sourceFiles) throws Exception {
        tree.setScrollsOnExpand(true);
        searchField.setText("");
        this.sources = sourceFiles;
        int a = SHOW_AS_FULL_TREE;
//        switch (getSettings().getChooseClassView()) {
        switch (a) {
            case SHOW_AS_FULL_TREE:
                radTree.setSelected(true);
                setShowAsFullTree();
                break;
            case SHOW_AS_LIST:
                radList.setSelected(true);
                setShowAsList();
                break;
            default:
                radFlat.setSelected(true);
                setShowAsFlatTree();
                break;
        }
        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setName("tree");
        modelSourceFiles.setConfig(getConfig());
        setModel(modelSourceFiles);
        int pr = sourceFiles.size();
        if (pr < 10) pr = 10;
        else if (pr > 25) pr = 25;
        add(treeScrollPane);
        searchField.setName("searchField");
        add(searchField);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        searchSrc(false);
                    }
                });
            }
        });
        Collections.sort(sourceFiles);
        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchSrc(true);
            }
        }, "", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), WHEN_IN_FOCUSED_WINDOW);
        modelSourceFiles.getList().setList(sourceFiles);
    }

    private void searchSrc(boolean b){
        String toSearch = searchField.getText();
        toSearch = toSearch.toLowerCase();
        if(toSearch != null && toSearch.length() > 0){
            List list = findObject(tree.getModel(), toSearch, packages, new ArrayList(), b?lastSearchResult:new ArrayList());
            if (list != null) {
                TreePath path = new TreePath(list.toArray());
                tree.expandPath(path);
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                lastSearchResult = list;
            }
        }
    }

    private List findObject(TreeModel model, String toSearch, Object o, ArrayList list, List last){
        list.add(o);
        if(model.isLeaf(o)){
            String os = o.toString().toLowerCase();
            if(showAs == SHOW_AS_LIST){
                os = os.substring(os.lastIndexOf('.')+1);
            }
            if(os.startsWith(toSearch) && last.isEmpty()){
                return list;
            }
            last.remove(o);
        }else{
            last.remove(o);
            int count = model.getChildCount(o);
            if(count > 0){
                for(int x = 0; x < count; x++){
                    Object child = model.getChild(o, x);
                    List l = findObject(model, toSearch, child, list, last);
                    if(l != null){
                        return l;
                    }
                }
            }
        }
       list.remove(o);
        return null;
    }

    private List buildPackages(List sources) {
        int size = sources.size();
        HashMap packages = new HashMap();
        for (int i = 0; i < size; i++) {
            String fullName = (String) sources.get(i);
            List tokens = getTokens(fullName);
            int size2 = tokens.size()-1;
            HashMap p = packages;
            for (int j = 0; j < size2; j++) {
                String s = (String) tokens.get(j);
                HashMap sp = (HashMap) p.get(s);
                if(sp == null){
                    sp = new HashMap();
                    p.put(s, sp);
                }
                p = sp;
            }
            String srcName = (String) tokens.get(size2);
            p.put(srcName, srcName);
        }
        NamedList list = buildLists(packages, "Sources");
        return list;
    }

    private NamedList buildLists(HashMap map, String name){
        NamedList list = new NamedList(name);
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = map.get(key);
            if(value instanceof String){
                list.add(value);
            }
            if(value instanceof HashMap){
                list.add(buildLists((HashMap)value, key));
            }
        }
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                if(o1 instanceof NamedList && !(o2 instanceof NamedList)){
                    return -1;
                }
                if(o2 instanceof NamedList && !(o1 instanceof NamedList)){
                    return 1;
                }
                return o1.toString().compareTo(o2.toString());
            }
        });
        return list;
    }

    private List getTokens(String fullName) {
        List tokens = new ArrayList();
        switch(showAs){
            case SHOW_AS_LIST:
                tokens.add(fullName);
                break;
            case SHOW_AS_FULL_TREE:
                StringTokenizer tokenizer = new StringTokenizer(fullName, ".", false);
                while(tokenizer.hasMoreTokens()){
                    tokens.add(tokenizer.nextToken());
                }
                break;
            default:
                int index = fullName.lastIndexOf('.');
                tokens.add(fullName.substring(0, index<0?0:index));
                tokens.add(fullName.substring(index+1));
        }
        return tokens;
    }

    public String getTitle() {
        return "Select Persistent Classes(s)";
    }

    public List getSelectedSources() {
        int rows[] = tree.getSelectionRows();
        if(rows == null){
            return new ArrayList(0);
        }
        List selected = new ArrayList(rows.length);
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            TreePath path = tree.getPathForRow(row);
            Object o = path.getLastPathComponent();
            StringBuffer fullName = new StringBuffer();
            if (o instanceof String) {
                while(path != null){
                    fullName.insert(0, path.getLastPathComponent());
                    fullName.insert(0, '.');
                    path = path.getParentPath();
                }
                selected.add(fullName.substring(2+packages.toString().length()));
            }
        }
        return selected;
    }

    public int getShowAs() {
        return showAs;
    }

    public void setShowAsFlatTree() {
        setShowAs(SHOW_AS_FLAT_TREE);
    }

    public void setShowAsFullTree() {
        setShowAs(SHOW_AS_FULL_TREE);
        Object o = packages;
        int count = tree.getModel().getChildCount(o);
        int row = 1;
        while(count == 1){
            tree.expandRow(row);
            row++;
            o = tree.getModel().getChild(o, 0);
            count = tree.getModel().getChildCount(o);
        }
    }

    public void setShowAsList() {
        setShowAs(SHOW_AS_LIST);
    }

    public void setShowAs(int showAs) {
        this.showAs = showAs;
//        getSettings().setChooseClassView(showAs);
        packages = buildPackages(sources);
        tree.setRootVisible(showAs != SHOW_AS_LIST);
        tree.setModel(null);
        tree.setModel(this);
        lastSearchResult = new ArrayList();
        searchSrc(false);
    }

    public boolean isOK(){
        return tree.getSelectionRows() != null;
    }

    public void setRadFlat(JRadioButton radFlat) {
        this.radFlat = radFlat;
    }

    public void setRadTree(JRadioButton radTree) {
        this.radTree = radTree;
    }

    public void setRadList(JRadioButton radList) {
        this.radList = radList;
    }

    // imlp TreeModel =============================================================================================
    public Object getRoot() {
        return packages;
    }

    public Object getChild(Object parent, int index) {
        if(parent != null && parent instanceof List){
            return ((List)parent).get(index);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if(parent != null && parent instanceof List){
            return ((List)parent).size();
        }
        return 0;
    }

    public boolean isLeaf(Object node) {
        if(node != null && node instanceof List){
            return false;
        }
        return true;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        if(parent != null && parent instanceof List){
            return ((List)parent).indexOf(child);
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    // NamedList ================================================================================

    class NamedList extends ArrayList{
        String name;

        public NamedList(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }


}

