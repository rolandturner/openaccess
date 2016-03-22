
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
package com.versant.core.jdo.tools.workbench.editor;

/**
 * Java token marker.
 * @keep-all
 * @author Slava Pestov
 */
public class JavaTokenMarker extends CTokenMarker {
    public JavaTokenMarker() {
        super(false, true, getKeywords());
    }

    public static KeywordMap getKeywords() {
        if (javaKeywords == null) {
            javaKeywords = new KeywordMap(false);
            javaKeywords.add("package", Token.KEYWORD1);
            javaKeywords.add("import", Token.KEYWORD1);
            javaKeywords.add("byte", Token.KEYWORD3);
            javaKeywords.add("char", Token.KEYWORD3);
            javaKeywords.add("short", Token.KEYWORD3);
            javaKeywords.add("int", Token.KEYWORD3);
            javaKeywords.add("long", Token.KEYWORD3);
            javaKeywords.add("float", Token.KEYWORD3);
            javaKeywords.add("double", Token.KEYWORD3);
            javaKeywords.add("boolean", Token.KEYWORD3);
            javaKeywords.add("void", Token.KEYWORD3);
            javaKeywords.add("class", Token.KEYWORD3);
            javaKeywords.add("interface", Token.KEYWORD3);
            javaKeywords.add("abstract", Token.KEYWORD1);
            javaKeywords.add("assert", Token.KEYWORD1);
            javaKeywords.add("final", Token.KEYWORD1);
            javaKeywords.add("strictfp", Token.KEYWORD1);
            javaKeywords.add("private", Token.KEYWORD1);
            javaKeywords.add("protected", Token.KEYWORD1);
            javaKeywords.add("public", Token.KEYWORD1);
            javaKeywords.add("static", Token.KEYWORD1);
            javaKeywords.add("synchronized", Token.KEYWORD1);
            javaKeywords.add("native", Token.KEYWORD1);
            javaKeywords.add("volatile", Token.KEYWORD1);
            javaKeywords.add("transient", Token.KEYWORD1);
            javaKeywords.add("break", Token.KEYWORD1);
            javaKeywords.add("case", Token.KEYWORD1);
            javaKeywords.add("continue", Token.KEYWORD1);
            javaKeywords.add("default", Token.KEYWORD1);
            javaKeywords.add("do", Token.KEYWORD1);
            javaKeywords.add("else", Token.KEYWORD1);
            javaKeywords.add("for", Token.KEYWORD1);
            javaKeywords.add("if", Token.KEYWORD1);
            javaKeywords.add("instanceof", Token.KEYWORD1);
            javaKeywords.add("new", Token.KEYWORD1);
            javaKeywords.add("return", Token.KEYWORD1);
            javaKeywords.add("switch", Token.KEYWORD1);
            javaKeywords.add("while", Token.KEYWORD1);
            javaKeywords.add("throw", Token.KEYWORD1);
            javaKeywords.add("try", Token.KEYWORD1);
            javaKeywords.add("catch", Token.KEYWORD1);
            javaKeywords.add("extends", Token.KEYWORD1);
            javaKeywords.add("finally", Token.KEYWORD1);
            javaKeywords.add("implements", Token.KEYWORD1);
            javaKeywords.add("throws", Token.KEYWORD1);
            javaKeywords.add("this", Token.LITERAL2);
            javaKeywords.add("null", Token.LITERAL2);
            javaKeywords.add("super", Token.LITERAL2);
            javaKeywords.add("true", Token.LITERAL2);
            javaKeywords.add("false", Token.LITERAL2);
        }
        return javaKeywords;
    }

    // private members
    private static KeywordMap javaKeywords;
}
