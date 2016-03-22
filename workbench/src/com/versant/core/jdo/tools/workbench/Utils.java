
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


import com.versant.core.jdo.tools.workbench.model.Logger;
import za.co.hemtech.gui.ActionBar;
import za.co.hemtech.gui.FlatButton;
import za.co.hemtech.gui.Icons;
import za.co.hemtech.gui.MethodAction;



import javax.jdo.JDOFatalUserException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Static utility methods.
 */
public class Utils {

    public static final boolean JDK13;

    private static Logger logger;


    static {
        String v = System.getProperty("java.version");
        JDK13 = v.startsWith("1.3");
    }

    private Utils() {
    }

    /**
     * Convert properties to a semicolon delimited String.
     */
    public static String toPropertyString(Properties p) {
        StringBuffer s = new StringBuffer();
        boolean first = true;
        for (Iterator i = p.entrySet().iterator(); i.hasNext();) {
            if (first) {
                first = false;
            } else {
                s.append(';');
            }
            Map.Entry e = (Map.Entry)i.next();
            s.append(e.getKey());
            s.append('=');
            s.append(e.getValue());
        }
        return s.toString();
    }

    /*
    public static void main(String[] args) {
        String[] a = new String[]{
            "/home/david/hemtech/jdo2/build.xml",
                "/home/david/hemtech/jdo2/build/compile",
            "/home/david/hemtech/jdo2/build/compile",
                "/home/david/hemtech/jdo2/etc/",
            "/home/david/hemtech/jdo2/build/classes",
                "/home/david/hemtech/jdo2/",
            "/home/david/hemtech/jdo2/build/classes",
                "/home/david/hemtech/jdo2/build/",
            "/home/david/hemtech/jdo2/build",
                "/home/david/hemtech/jdo2/build/",
            "/home/david/hemtech/jdo2/build/build.xml",
                "/home/david/hemtech/jdo2/etc/",
        };
        for (int i = 0; i < a.length; i += 2) {
            test(a[i], a[i + 1]);
        }
    }

    private static void test(String path, String dir) {
        System.out.println("p = " + path);
        System.out.println("d = " + dir);
        String rel = toRelativePath(path, dir);
        System.out.println("rel = '" + rel + "'");
        String abs = toAbsolutePath(rel, dir);
        System.out.println("abs = '" + abs + "'\n");
        if (!abs.equals(path)) {
            throw new IllegalStateException("Invalid");
        }
    }
    */

    /**
     * Populate a menu from an array of MethodAction's, JMenu's or JMenuItem's.
     * If an entry in the array is an action then the next two entries must
     * be the accelerator key and the mnemonic key.
     */

    public static JComponent populateMenu(JComponent m, Object[] a) {
        int n = a == null ? 0 : a.length;
        for (int i = 0; i < n;) {
            Object o = a[i++];
            if (o == null) {
                if (m instanceof JMenu) {
                    ((JMenu)m).addSeparator();
                } else if (m instanceof JPopupMenu) ((JPopupMenu)m).addSeparator();
                continue;
            }
            if (o instanceof JMenu) {
                m.add((JMenu)o);
                continue;
            }
            if (o instanceof JMenuItem) {
                m.add((JMenuItem)o);
                continue;
            }
            if (o instanceof JPopupMenu) {
                m.add((JPopupMenu)o);
                continue;
            }
            MethodAction ma = (MethodAction)o;
            if (ma.getIcon() == null) {
                ma.setIcon(Icons.EMPTY16);
            }
            m.add(createMenuItem(ma, (String)a[i++]));
        }
        return m;
    }


    /**
     * Create a menu item.
     */

    public static JMenuItem createMenuItem(MethodAction ma, String mnemonic) {
        JMenuItem mi = new JMenuItem(ma);
        KeyStroke acc = ma.getAccelerator();
        if (acc != null) {
            mi.setAccelerator(acc);
            mi.registerKeyboardAction(ma, acc,
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        if (mnemonic != null) mi.setMnemonic(mnemonic.charAt(0));
        mi.setToolTipText(ma.getToolTipText());
        return mi;
    }


    /**
     * Convert an Object into ClassName@addr String.
     */
    public static String tos(Object o) {
        if (o == null) return null;
        String s = o.getClass().getName();
        int i = s.lastIndexOf('.');
        if (i >= 0) s = s.substring(i + 1);
        return s + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    /**
     * Enables JDK 1.4 decoration of windows by the L&F in a JDK 1.3 safe way.
     */

    public static void setDefaultLookAndFeelDecorated() {
        boolean safeToTry = false;
        try {
            Class.forName("java.lang.StringBuilder");
        } catch (ClassNotFoundException e) {
            safeToTry = true;
        }
        if (safeToTry) {
            Method[] methods = JDialog.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if ("setDefaultLookAndFeelDecorated".equals(method.getName())) {
                    Object[] oArray = new Object[]{Boolean.TRUE};
                    try {
                        method.invoke(null, oArray);
                        return;
                    } catch (IllegalAccessException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (InvocationTargetException e) {
                    }
                }
            }
        }
    }


    /**
     * Populate the supplied toolbar with FlatButtons and Text created from all the
     * Action's. If an entry in a is null then a separator is added instead.
     */

    public static void populateToolbarWithText(Container bar, Action[] a) {
        boolean tb = bar instanceof JToolBar;
        boolean ab = bar instanceof ActionBar;
        int n = a.length;
        for (int i = 0; i < n; i++) {
            Action act = a[i];
            if (act == null) {
                if (ab) {
                    ((ActionBar)bar).addSeparator();
                } else if (tb) {
                    ((JToolBar)bar).addSeparator();
                }
            } else {
                bar.add(FlatButton.createForToolbarWithText(act));
            }
        }
    }


    /**
     * Are we running on windows
     */
    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith("Windows");
    }

    public static String fixPackageName(String name) {
        if (name == null || name.trim().length() == 0) return null;
        char[] chars = name.toCharArray();
        boolean lastDot = true;
        char aChar = chars[0];
        for (int i = 0; i < chars.length; i++) {
            aChar = Character.toLowerCase(chars[i]);
            chars[i] = aChar;
            boolean change = lastDot && (aChar == '.' || Character.isDigit(
                    aChar));
            change = change || aChar != '.' && !Character.isLetterOrDigit(
                    aChar);
            if (change) {
                char[] newChars = new char[chars.length - 1];
                System.arraycopy(chars, 0, newChars, 0, i);
                System.arraycopy(chars, i + 1, newChars, i,
                        newChars.length - i);
                chars = newChars;
                i--;
            }
            lastDot = aChar == '.';
        }
        return new String(chars);
    }

    public static String fixClassName(String name) {
        if (name == null || name.trim().length() == 0) return null;
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (!Character.isLetterOrDigit(aChar) || (i == 0 && Character.isDigit(
                    aChar))) {
                char[] newChars = new char[chars.length - 1];
                System.arraycopy(chars, 0, newChars, 0, i);
                System.arraycopy(chars, i + 1, newChars, i,
                        newChars.length - i);
                chars = newChars;
                i--;
            }
        }
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String camel(String name) {
        if (name == null) return null;
        boolean last_ = false;
        char[] chars = name.toCharArray();
        boolean notMixed = !isMixedCase(name);
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (last_) {
                aChar = Character.toUpperCase(aChar);
                chars[i] = aChar;
            } else {
                if (notMixed) {
                    aChar = Character.toLowerCase(aChar);
                    chars[i] = aChar;
                }
            }
            last_ = false;
            if (!Character.isLetterOrDigit(aChar) || (i == 0 && Character.isDigit(
                    aChar))) {
                last_ = true;
                char[] newChars = new char[chars.length - 1];
                System.arraycopy(chars, 0, newChars, 0, i);
                System.arraycopy(chars, i + 1, newChars, i,
                        newChars.length - i);
                chars = newChars;
                i--;
            }
        }
        return new String(chars);
    }

//    public static void main(String[] args) {
//        String ans = "SupplierOrderLine";
//        String ans2 = "supplierOrderLine";
//        try {
//            testCamel(ans, ans2, ans2, "SUPPLIER_ORDER_LINE");
//            testCamel(ans, ans2, ans2, "SUPPLIER ORDER LINE");
//            testCamel(ans, ans2, ans2, "SUPPLIER-ORDER-LINE");
//            testCamel(ans, ans2, ans2, "SUPPLIER$ORDER#LINE");
//            testCamel(ans, ans, ans2, "Supplier$Order#Line");
//            testCamel(ans, ans, ans2, "Supplier Order Line");
//            testCamel(ans, ans, ans2, "SupplierOrderLine");
//            testCamel(ans, ans, ans2, "*******Supplier_____Order$^%$#Line*******");
//            testCamel(ans, ans, ans2, "1SupplierOrderLine");
//            testCamel(ans, ans, ans2, "$SupplierOrderLine");
//            testCamel(ans, ans2, ans2, "supplier order line");
//            testCamel(ans, ans2, ans2, "supplier_order_line");
//            testCamel(ans, ans2, ans2, "supplier&order*line");
//            testCamel(ans, ans2, ans2, "supplier-order-line");
//            testCamel(ans, ans2, ans2, "supplier.order.line");
//        } catch (Exception e) {
//            e.printStackTrace(System.out);
//        }
//    }
//
//    private static void testCamel(String ans, String ans2, String ans3, String value) throws Exception {
//        String camel = camel(value);
//        if(!ans2.equals(camel)){
//            throw new Exception("camel broken from '"+value+"' to '"+camel+"' expected '"+ans2+"'");
//        }
//        if(!ans.equals(fixClassName(camel))){
//            throw new Exception("fixClassName broken from '"+value+"' to '"+fixClassName(camel)+"' expected '"+ans+"'");
//        }
//        if(!ans3.equals(fixFieldName(camel))){
//            throw new Exception("fixClassName broken from '"+value+"' to '"+fixFieldName(camel)+"' expected '"+ans3+"'");
//        }
//    }

    public static boolean isMixedCase(String text){
        if (text == null) return false;
        char[] chars = text.toCharArray();
        boolean lower = false;
        boolean upper = false;
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (Character.isLetter(aChar)){
                if (Character.isLowerCase(aChar)){
                    lower = true;
                }
                if (Character.isUpperCase(aChar)) {
                    upper = true;
                }
                if (lower && upper){
                    return true;
                }
            }
        }
        return false;
    }

    public static String fixFieldName(String name) {
        if (name == null || name.trim().length() == 0) return null;
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            boolean change = i == 0 && Character.isDigit(aChar);
            change = change || aChar != '_' && !Character.isLetterOrDigit(
                    aChar);
            if (!Character.isLetterOrDigit(aChar) || (i == 0 && Character.isDigit(
                    aChar))) {
                char[] newChars = new char[chars.length - 1];
                System.arraycopy(chars, 0, newChars, 0, i);
                System.arraycopy(chars, i + 1, newChars, i,
                        newChars.length - i);
                chars = newChars;
                i--;
            }
        }
        chars[0] = Character.toLowerCase(chars[0]);
        String s = String.valueOf(chars);
        if (s.equals("jdoVersion")) {
            s = "version";
        }
        if (s.equals("jdoClass")) {
            s = "classType";
        }
        return s;
    }

    public static void createFile(File file, boolean dir) throws IOException {
        if (!file.exists()) {
            File parent = getParentFile(file);
            if (parent != null && !parent.exists()) {
                createFile(parent, true);
            }
            if (dir) {
                file.mkdir();
            } else {

				file.createNewFile();


            }
        }
    }

	public static List singletonList(Object obj)
	{
		List li = new ArrayList();
		li.add(obj);
		return li;
	}

	public static java.io.File getParentFile(java.io.File file)
	{

		return file.getParentFile();


	}


    public static Window getWindow(Component component) {
        Window win = null;
        if (component instanceof Window) {
            win = (Window)component;
        } else {
            win = SwingUtilities.getWindowAncestor(component);
        }
        return win;
    }



    /**
     * Open a dialog.
     */

    public static boolean openDialog(Component c, WorkbenchPanel form, boolean modal, boolean hasOk,
            boolean hasCancel) throws Exception {
        return openDialog(c, form, modal, hasOk, hasCancel,
                form.getClass().getName(), true);
    }


    /**
     * Open a dialog and remember its size as name.
     */

    public static boolean openDialog(Component c, WorkbenchPanel form, boolean modal, boolean hasOk,
            boolean hasCancel, String name, boolean isEscapeToCancel)
            throws Exception {
        String[] buttons;
        if (hasOk) {
            if (hasCancel) {
                buttons = new String[]{
                    "OK", "Ok16.gif", "Accept changes and close the dialog",
                    "Cancel", "cancel.gif",
                    "Cancel changes"};
            } else {
                buttons = new String[]{
                    "OK", "Ok16.gif", "Accept changes and close the dialog"};
            }
        } else {
            if (hasCancel) {
                buttons = new String[]{
                    "Cancel", "cancel.gif", "Cancel changes"};
            } else {
                buttons = new String[]{};
            }
        }
        return "OK".equals(
                openDialog(c, form, modal, buttons, name, isEscapeToCancel));
    }



    public static String openDialog(Component c, WorkbenchPanel form, boolean modal, String[] buttons,
            String name, boolean isEscapeToCancel) throws Exception {
        DialogOpener dialogOpener = getDialogOpener(c, form, name);
        WorkbenchDialogRoot root = new WorkbenchDialogRoot(dialogOpener, form, buttons, isEscapeToCancel);
        dialogOpener.openDialog(root, modal);
        return root.getCommand();
    }



    private static DialogOpener getDialogOpener(final Component c, final WorkbenchPanel form, final String name) {
        for(Component temp = c; temp != null; temp = temp.getParent()){
            if(temp instanceof DialogOpenerProvider){
                return ((DialogOpenerProvider)temp).createDialogOpener();
            }
        }
        return new DialogOpener() {
            WorkbenchDialog dialog;
            public void openDialog(WorkbenchDialogRoot root, boolean modal) {
                dialog = createDialog(c, modal);
                dialog.setContentPane(root);
                try {
//                    ((MainFrame)form.getMainFrame()).getHelpAgent().registerHelpHotkey(root);
                } catch (Exception e) {
                    //Do Nothing
                }
                dialog.setTitle(root.getTitle());
                if (name != null) {
                    dialog.setUserConfig(WorkbenchSettings.getInstance().getDialogConfig());
                    dialog.setName(name);
                } else {
                    dialog.pack();
                    dialog.setLocationRelativeTo(dialog.getParent());
                }
                dialog.setVisible(true);
            }

            public void dispose() {
                dialog.dispose();
            }
        };
    }



    private static WorkbenchDialog createDialog(Component component, boolean modal) {
        WorkbenchDialog dialog;
        if (component == null) {
            dialog = new WorkbenchDialog((Frame)null, modal);
        } else {
            Window win = null;
            if (component instanceof Window) {
                win = (Window)component;
            } else {
                win = SwingUtilities.getWindowAncestor(component);
            }
            if (win instanceof Frame) {
                dialog = new WorkbenchDialog((Frame)win, modal);
            } else if (win instanceof Dialog) {
                dialog = new WorkbenchDialog((Dialog)win, modal);
            } else {
                dialog = new WorkbenchDialog((Frame)null, modal);
            }
        }
        return dialog;
    }



    public static Logger getLogger() {
        return logger;
    }



    public static void setLogger(Logger logger) {
        Utils.logger = logger;
    }

}

