
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
 * BeanShell (www.beanshell.org) token marker.
 * @keep-all
 * @author Slava Pestov
 */
public class BeanShellTokenMarker extends CTokenMarker {
    // private members
    private static KeywordMap bshKeywords;
	public BeanShellTokenMarker() {
		super(false,false,getKeywords());
	}

	public static KeywordMap getKeywords() {
		if(bshKeywords == null) {
			bshKeywords = new KeywordMap(false);
			bshKeywords.add("import",Token.KEYWORD1);
			bshKeywords.add("byte",Token.KEYWORD1);
			bshKeywords.add("char",Token.KEYWORD1);
			bshKeywords.add("short",Token.KEYWORD1);
			bshKeywords.add("int",Token.KEYWORD1);
			bshKeywords.add("long",Token.KEYWORD1);
			bshKeywords.add("float",Token.KEYWORD1);
			bshKeywords.add("double",Token.KEYWORD1);
			bshKeywords.add("boolean",Token.KEYWORD1);
			bshKeywords.add("void",Token.KEYWORD1);
			bshKeywords.add("break",Token.KEYWORD1);
			bshKeywords.add("case",Token.KEYWORD1);
			bshKeywords.add("continue",Token.KEYWORD1);
			bshKeywords.add("default",Token.KEYWORD1);
			bshKeywords.add("do",Token.KEYWORD1);
			bshKeywords.add("else",Token.KEYWORD1);
			bshKeywords.add("for",Token.KEYWORD1);
			bshKeywords.add("if",Token.KEYWORD1);
			bshKeywords.add("instanceof",Token.KEYWORD1);
			bshKeywords.add("new",Token.KEYWORD1);
			bshKeywords.add("return",Token.KEYWORD1);
			bshKeywords.add("switch",Token.KEYWORD1);
			bshKeywords.add("while",Token.KEYWORD1);
			bshKeywords.add("throw",Token.KEYWORD1);
			bshKeywords.add("try",Token.KEYWORD1);
			bshKeywords.add("catch",Token.KEYWORD1);
			bshKeywords.add("finally",Token.KEYWORD1);
            bshKeywords.add("class", Token.KEYWORD1);
			bshKeywords.add("this",Token.LITERAL2);
			bshKeywords.add("null",Token.LITERAL2);
			bshKeywords.add("true",Token.LITERAL2);
			bshKeywords.add("false",Token.LITERAL2);
		}
		return bshKeywords;
	}


}
