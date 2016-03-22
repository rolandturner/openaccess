
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
package util;

import model.Subject;
import model.Module;
import model.Student;
import model.Registration;

import javax.jdo.PersistenceManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;
import java.util.List;
import java.io.*;

/**
 * Insert sample data for the application.
 */
public class InsertTestData {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pm();

            pm.currentTransaction().begin();

            // make a bunch of subjects and modules from the list below
            ArrayList subjects = new ArrayList();
            String[] a = new String[]{
                "JAV", "Java",
                "CPP", "C++",
                "ASM", "Assembly (x86)",
                "PYT", "Python",
                "RUB", "Ruby",
                "BSH", "Bash scripting",
                "JDO", "Java Data Objects",
                "GNI", "Using JDO Genie",
                "ANT", "Apache Ant",
                "LNX", "Linux",
                "WIN", "Windows",
                "BSD", "Free BSD",
                "OSX", "Mac OSX",
            };
            for (int year = 1; year <= 4; year++) {
                for (int i = 0; i < a.length; i += 2) {
                    Subject sub = new Subject(a[i] + year * 100);
                    sub.setName(a[i + 1] + " " + roman(year));
                    for (int j = 1; j <= year + 1; j++) {
                        Module mod = new Module();
                        mod.setSubject(sub);
                        mod.setTitle("" + (char)('A' + j));
                        mod.setDescription("Module " + mod.getTitle() + " for " +
                                sub.getName());
                        sub.addModule(mod);
                    }
                    subjects.add(sub);
                }
            }

            // make some students and register them for a random selection
            // of subjects and modules
            ArrayList students = new ArrayList();
            Random rnd = new Random(123);
            for (Iterator i = readSurnames().iterator(); i.hasNext(); ) {
                Student student = new Student();
                student.setName((String)i.next());
                int n = rnd.nextInt(10);
                int ns = subjects.size();
                for (int j = 0; j < n; j++) {
                    Subject sub = (Subject)subjects.get(rnd.nextInt(ns));
                    if (student.getRegistration(sub) != null) continue;
                    Registration r = student.register(sub);
                    for (Iterator k = sub.getModules().iterator(); k.hasNext(); ) {
                        Module mod = (Module)k.next();
                        if (rnd.nextBoolean()) r.addModule(mod);
                    }
                }
                students.add(student);
            }

            pm.makePersistentAll(subjects);
            pm.makePersistentAll(students);

            pm.currentTransaction().commit();

            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    private static String roman(int i) {
        switch (i) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
        }
        return Integer.toString(i);
    }

    private static List readSurnames() throws IOException {
        String res = "/surnames.txt";
        InputStream is = Sys.class.getResourceAsStream(res);
        if (is == null) throw new IOException("Resource not found: " + res);
        ArrayList a = new ArrayList();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String data = r.readLine();
        r.close();
        int pos = 0;
        for (;;) {
            int i = data.indexOf(' ', pos);
            String name = data.substring(pos, i);
            a.add(name);
            i = data.indexOf(' ', i + 1);
            i = data.indexOf(' ', i + 1);
            i = data.indexOf(' ', i + 1);
            if (i < 0) break;
            pos = i + 1;
        }
        return a;
    }

}

