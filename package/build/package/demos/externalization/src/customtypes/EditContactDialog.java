
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
package customtypes;

import customtypes.model.Contact;
import customtypes.model.PhoneNumber;
import customtypes.model.PngImage;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Dialog to edit a Contact.
 */
public class EditContactDialog extends JDialog implements ActionListener {

    private Main main;
    private Contact contact;
    private boolean ok;

    private JPanel fieldPanel = new JPanel(new GridBagLayout());
    private JTextField fieldName = new JTextField();
    private JTextField fieldEmail = new JTextField();
    private JTextField fieldPhone = new JTextField();

    private JLabel labImage = new JLabel();

    private static final Border IMAGE_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(8, 8, 8, 8));

    public EditContactDialog(Main main, Contact contact, String title) {
        super(main, title, true);
        this.main = main;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContact(contact);
        addField(0, "Name", fieldName);
        addField(1, "Email", fieldEmail);
        addField(2, "Phone", fieldPhone);
        labImage.setBorder(IMAGE_BORDER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton("Choose Image", "chooseImage"));
        buttonPanel.add(createButton("OK", "ok"));
        buttonPanel.add(createButton("Cancel", "dispose"));
        labImage.setMinimumSize(new Dimension(100, 100));
        getContentPane().add(labImage, BorderLayout.WEST);
        getContentPane().add(fieldPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addField(int y, String label, JComponent field) {
        Insets ins = new Insets(2, 2, 2, 2);
        fieldPanel.add(new JLabel(label),
            new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                    0, ins, 0, 0));
        fieldPanel.add(field,
            new GridBagConstraints(1, y, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL,ins, 0, 0));
    }

    /**
     * Display the dialog and return true if ok is pressed.
     */
    public boolean display() {
        ok = false;
        pack();
        setLocationRelativeTo(main);
        setVisible(true);
        return ok;
    }

    public void ok() {
        fillContact();
        ok = true;
        dispose();
    }

    public void chooseImage() throws Exception {
        JFileChooser dlg = new JFileChooser();
        dlg.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String n = f.getName().toLowerCase();
                return n.endsWith(".gif") || n.endsWith(".jpg")
                        || n.endsWith(".png");
            }
            public String getDescription() {
                return "Images (*.gif,*.jpg,*.png)";
            }
        });
        if (dlg.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = dlg.getSelectedFile();
        if (f == null) return;
        PngImage img = new PngImage(f.toURL());
        contact.setPngImage(img);
        labImage.setIcon(img);
        pack();
    }

    private void setContact(Contact contact) {
        this.contact = contact;
        fieldName.setText(contact.getName());
        fieldEmail.setText(contact.getEmail());
        fieldPhone.setText(contact.getPhone().toString());
        labImage.setIcon(contact.getPngImage());
    }

    private void fillContact() {
        String s = fieldName.getText();
        if (!s.equals(contact.getName())) contact.setName(s);
        s = fieldEmail.getText();
        if (!s.equals(contact.getEmail())) contact.setEmail(s);
        s = fieldPhone.getText();
        PhoneNumber phone = contact.getPhone();
        if (phone == null || !s.equals(phone.toString())) {
            contact.setPhone(new PhoneNumber(s));
        }
    }

    private JButton createButton(String text, String method) {
        JButton b = new JButton(text);
        b.addActionListener(this);
        b.setActionCommand(method);
        return b;
    }

    public void actionPerformed(ActionEvent e) {
        main.invoke(this, e.getActionCommand());
    }

}

