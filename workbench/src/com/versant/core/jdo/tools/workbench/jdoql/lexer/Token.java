
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
package com.versant.core.jdo.tools.workbench.jdoql.lexer;

/**
 * @keep-all
 * A generic token class.
 */
public abstract class Token{

	/**
	 * A unique ID for this type of token. 
	 * Typically, ID numbers for each type will
	 * be static variables of the Token class.
	 * 
	 * @return an ID for this type of token.
	 */
	public abstract int getID();
	
	/**
	 * A description of this token.  The description should
	 * be appropriate for syntax highlighting.  For example
	 * "comment" might be returned for a comment.  This should
	 * make it easy to do html syntax highlighting.  Just use
	 * style sheets to define classes with the same name as 
	 * the description and write the token in the html file
	 * with that css class name.
     *
	 * @return a description of this token.
	 */
	public abstract String getDescription();
	
	/**
	 * The actual meat of the token.
	 *
	 * @return a string representing the text of the token.
	 */
	public abstract String getContents();
	
	/**
	 * Determine if this token is a comment.  Sometimes comments should be
	 * ignored (compiling code) other times they should be used
	 * (syntax highlighting).  This provides a method to check
	 * in case you feel you should ignore comments.
	 *
	 * @return true if this token represents a comment.
	 */
	public abstract boolean isComment();
	
	/**
	 * Determine if this token is whitespace.  Sometimes whitespace should be
	 * ignored (compiling code) other times they should be used
	 * (code beautification).  This provides a method to check
	 * in case you feel you should ignore whitespace.
	 *
	 * @return true if this token represents whitespace.
	 */
	public abstract boolean isWhiteSpace();
	
	/**
	 * Determine if this token is an error.  Lets face it, not all code 
	 * conforms to spec. The lexer might know about an error
	 * if a string literal is not closed, for example. 
	 *
	 * @return true if this token is an error.
	 */
  	public abstract boolean isError();
	
	/** 
     * get the line number of the input on which this token started
     * 
     * @return the line number of the input on which this token started
     */
    public abstract int getLineNumber();

    /** 
     * get the offset into the input in characters at which this token started
     *
     * @return the offset into the input in characters at which this token started
     */
    public abstract int getCharBegin();

    /** 
     * get the offset into the input in characters at which this token ended
     *
     * @return the offset into the input in characters at which this token ended
     */
    public abstract int getCharEnd();
	
	/**
     * get a String that explains the error, if this token is an error.
     * 
     * @return a  String that explains the error, if this token is an error, null otherwise.
     */
    public abstract String errorString();

    /**
     * The state of the tokenizer is undefined.
     */
    public static final int UNDEFINED_STATE = -1;

    /**
     * The initial state of the tokenizer.
     * Anytime the tokenizer returns to this state,
     * the tokenizer could be restarted from that point
     * with side effects.
     */
    public static final int INITIAL_STATE = 0;

    /**
     * Get an integer representing the state the tokenizer is in after
     * returning this token.
     * Those who are interested in incremental tokenizing for performance
     * reasons will want to use this method to figure out where the tokenizer
     * may be restarted.  The tokenizer starts in Token.INITIAL_STATE, so
     * any time that it reports that it has returned to this state, the
     * tokenizer may be restarted from there.
     */
    public abstract int getState();

}
