
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
package com.versant.core.jdo.junit;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;

import java.io.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;



/**
 * This buffers sysout and syserr from each test and writes the output to
 * a separate file for each test. Output for failing tests is optionally
 * dumped on the console. It also attempts to cleanup after tests by
 * deleting objects left lying around in the database.
 * <p/>
 */
public class VersantResultFormatter extends PlainJUnitResultFormatter {

    public static PrintStream realSystemOut = System.out;

    private File testOutDir;
    private boolean dumpOnError;
    private boolean onlyFailedFiles;
    private boolean progressGui;
    private String startStr;
    private int lastTestStatus;


    private int passedCount;
    private int failedCount;
    private int errorCount;

    private ByteArrayOutputStream testOutBuf;
    private PrintStream testOut;
    private PrintStream saveOut;
    private PrintStream saveErr;

    private ProgressFrame progressFrame;



    // extended test statuses
    private static final int STATUS_PASSED = 1;
    private static final int STATUS_FAILED = 2;
    private static final int STATUS_ERROR = 3;
    private static final int STATUS_BROKEN = 4;
    private static final int STATUS_UNSUPPORTED = 5;

    private static final String TEST_OUT_DIR = "test.out.dir";
    private static final String ONLY_FAILED_FILES = "only.failed.files";
    private static final String DUMP_ON_ERROR = "dump.on.error";
    private static final String PROGRESS_GUI = "progress.gui";


    public VersantResultFormatter() {
        String p = System.getProperty(TEST_OUT_DIR);
        if (p == null) {
            throw new IllegalStateException(TEST_OUT_DIR + " " +
                    "system property not set");
        }
        testOutDir = new File(p);
        if (!testOutDir.isDirectory()) {
            if (!testOutDir.exists()) {
                throw new IllegalArgumentException(TEST_OUT_DIR + " '" + p
                        + "' does not exist");
            } else {
                throw new IllegalArgumentException(TEST_OUT_DIR +
                        " is not a directory");
            }
        }
        dumpOnError = Boolean.valueOf(System.getProperty(DUMP_ON_ERROR,
                "true")).booleanValue();
        onlyFailedFiles = Boolean.valueOf(System.getProperty(ONLY_FAILED_FILES,
                "true")).booleanValue();
        progressGui = Boolean.valueOf(System.getProperty(PROGRESS_GUI,
                "true")).booleanValue();

    }

    private static class ClassAndMethod {

        public String cls;
        public String method;
    }

    /**
     * Extract the name of the test class and method.
     */
    private ClassAndMethod parseTestName(Test t) {
        ClassAndMethod ans = new ClassAndMethod();
        String s = t.toString();
        try {
            int i = s.indexOf('(');
            String cls = s.substring(i + 1, s.length() - 1);
            ans.method = s.substring(4, i);
            i = cls.lastIndexOf('.');
            ans.cls = cls.substring(i + 1);
        } catch (Exception e) {
            ans.cls = s;
            ans.method = "unknown";
        }
        return ans;
    }

    private void showProgress(Test test) {
        if (!progressGui) return;
        if (progressFrame == null) {
            String msg;
            if (test instanceof VersantTestCase) {
                msg = ((VersantTestCase)test).getConfigurationSummary();
            } else {
                msg = test.toString();
            }
            progressFrame = new ProgressFrame(msg);
        }
        ClassAndMethod cm = parseTestName(test);
        String s = cm.cls + " >>> " + cm.method + " ... ";
        progressFrame.setLabelText(s);
    }

    public void startTestSuite(JUnitTest suite) {
        errorCount = failedCount = passedCount = 0;
        super.startTestSuite(suite);
    }

    public void startTest(Test test) {
//        if (!(test instanceof VersantTestCase)) {
//            throw new RuntimeException("All tests must extend VersantTestCase: " +
//                    test.getClass().getName() + ": " + test);
//        }
        VersantTestCase tc =
                test instanceof VersantTestCase ? (VersantTestCase)test : null;
        lastTestStatus = 0;
        ClassAndMethod cm = parseTestName(test);
        startStr = ">>> " + cm.cls + " >>> " + cm.method + " ... ";
        realSystemOut.print(startStr);
        realSystemOut.flush();
        System.out.print(startStr);
        // redirect test output to our buffer
        saveOut = System.out;
        saveErr = System.err;
        testOut = new PrintStream(testOutBuf = new ByteArrayOutputStream(),
                false);
        System.setOut(testOut);
        System.setErr(testOut);
        showProgress(test);

        super.startTest(test);
    }



    public void endTest(Test test) {
        super.endTest(test);
        VersantTestCase tc =
                test instanceof VersantTestCase ? (VersantTestCase)test : null;
        if (lastTestStatus < 0) return; // we have already been called
        if (lastTestStatus == 0) {
            if (tc != null && tc.isBroken()) {
                lastTestStatus = STATUS_BROKEN;
            } else if (tc != null && tc.isUnsupported()) {
                lastTestStatus = STATUS_UNSUPPORTED;
            } else {
                lastTestStatus = STATUS_PASSED;
            }
        }
        boolean failed = lastTestStatus == STATUS_FAILED
                || lastTestStatus == STATUS_ERROR;
        String stat = getLastTestStatusStr();
        realSystemOut.println(stat);
        saveOut.println(stat);

        // get the output from our buffer and write it to a file
        System.setOut(saveOut);
        System.setErr(saveErr);
        testOut.close();
        String text = "";
        try {
            text = testOutBuf.toString();
        } catch (Exception e) {
            text = e.getMessage();
            if (text == null){
                e.printStackTrace(realSystemOut);
            }
        }
        if (failed || !onlyFailedFiles) {
            ClassAndMethod cm = parseTestName(test);
            File f = new File(testOutDir, cm.cls + "-" + cm.method + ".out");
            try {
                FileWriter fw = new FileWriter(f);
                fw.write(text != null ? text : "[null]");
                fw.close();
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        if (failed && dumpOnError) {
            realSystemOut.println(text);
        }
        if (failed) {
            String cs = tc == null ? test.toString() : tc.getConfigurationSummary();
            realSystemOut.println(cs);
            saveOut.println(cs);
        }

        // update counts
        switch (lastTestStatus) {
            case STATUS_BROKEN:
                break;
            case STATUS_ERROR:
                errorCount++;
                break;
            case STATUS_FAILED:
                failedCount++;
                break;
            case STATUS_UNSUPPORTED:
                break;
            case STATUS_PASSED:
                passedCount++;
                break;
        }



        lastTestStatus = -1;
    }



    private String getLastTestStatusStr() {
        switch (lastTestStatus) {
            case STATUS_PASSED:
                return "ok";
            case STATUS_FAILED:
                return "FAILED";
            case STATUS_ERROR:
                return "ERROR";
            case STATUS_BROKEN:
                return "(broken)";
            case STATUS_UNSUPPORTED:
                return "(unsupported)";
        }
        return "UNKNOWN(" + lastTestStatus + ")";
    }

    public void endTestSuite(JUnitTest suite) throws BuildException {
        super.endTestSuite(suite);
        if (progressFrame != null) {
            progressFrame.dispose();
            progressFrame = null;
        }

    }

    private void print(Throwable t) {
        System.out.println(JUnitTestRunner.getFilteredTrace(t));
    }

    public void addFailure(Test test, AssertionFailedError t) {
        lastTestStatus = STATUS_FAILED;
        print(t);
        super.addFailure(test, t);
    }

    public void addError(Test test, Throwable t) {
        lastTestStatus = STATUS_ERROR;
        print(t);
        super.addError(test, t);
    }



}


