
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
package com.versant.core.jdo.tools.workbench.jdoql.ordering;

import com.versant.core.jdo.tools.workbench.jdoql.lexer.Token;


/**
 * @keep-all
 */
public class OrderingToken extends Token {

    public final static int RESERVED_WORD = 0x128;

    public final static int RESERVED_WORD_THIS = 0x130;

    public final static int IDENTIFIER = 0x200;

    public final static int IDENTIFIER_STRING = 0x210;

    public final static int IDENTIFIER_COLLECTION = 0x211;

    public final static int IDENTIFIER_VAR = 0x212;

    public final static int IDENTIFIER_VAR_PC = 0x213;

    public final static int IDENTIFIER_MAP = 0x214;

    public final static int IDENTIFIER_PARAM = 0x201;

    public final static int IDENTIFIER_PARAM_PC = 0x202;

    public final static int LITERAL_INTEGER_DECIMAL = 0x310;

    public final static int LITERAL_INTEGER_OCTAL = 0x311;

    public final static int LITERAL_INTEGER_HEXIDECIMAL = 0x312;

    public final static int LITERAL_LONG_DECIMAL = 0x320;

    public final static int LITERAL_LONG_OCTAL = 0x321;

    public final static int LITERAL_LONG_HEXIDECIMAL = 0x322;

    public final static int LITERAL_FLOATING_POINT = 0x330;

    public final static int LITERAL_DOUBLE = 0x340;

    public final static int LITERAL_CHARACTER = 0x350;

    public final static int LITERAL_STRING = 0x360;

    public final static int SEPARATOR_LPAREN = 0x400;

    public final static int SEPARATOR_RPAREN = 0x401;

    public final static int SEPARATOR_LBRACKET = 0x420;

    public final static int SEPARATOR_RBRACKET = 0x421;

    public final static int SEPARATOR_COMMA = 0x440;

    public final static int SEPARATOR_PERIOD = 0x450;

    public final static int OPERATOR_GREATER_THAN = 0x500;

    public final static int OPERATOR_LESS_THAN = 0x501;

    public final static int OPERATOR_LESS_THAN_OR_EQUAL = 0x502;

    public final static int OPERATOR_GREATER_THAN_OR_EQUAL = 0x503;

    public final static int OPERATOR_EQUAL = 0x504;

    public final static int OPERATOR_NOT_EQUAL = 0x505;

    public final static int OPERATOR_LOGICAL_NOT = 0x510;

    public final static int OPERATOR_LOGICAL_AND = 0x511;

    public final static int OPERATOR_LOGICAL_OR = 0x512;

    public final static int OPERATOR_ADD = 0x520;

    public final static int OPERATOR_SUBTRACT = 0x521;

    public final static int OPERATOR_MULTIPLY = 0x522;

    public final static int OPERATOR_DIVIDE = 0x523;

    public final static int OPERATOR_BITWISE_COMPLIMENT = 0x530;

    public final static int OPERATOR_BITWISE_AND = 0x531;

    public final static int OPERATOR_BITWISE_OR = 0x532;

    public final static int COMMENT_TRADITIONAL = 0xD00;

    public final static int COMMENT_END_OF_LINE = 0xD10;

    public final static int COMMENT_DOCUMENTATION = 0xD20;

    public final static int WHITE_SPACE = 0xE00;

    public final static int ERROR_IDENTIFIER = 0xF00;

    public final static int ERROR_UNCLOSED_STRING = 0xF10;

    public final static int ERROR_MALFORMED_STRING = 0xF11;

    public final static int ERROR_MALFORMED_UNCLOSED_STRING = 0xF12;

    public final static int ERROR_UNCLOSED_CHARACTER = 0xF20;

    public final static int ERROR_MALFORMED_CHARACTER = 0xF21;

    public final static int ERROR_MALFORMED_UNCLOSED_CHARACTER = 0xF22;

    public final static int ERROR_INTEGER_DECIMIAL_SIZE = 0xF30;

    public final static int ERROR_INTEGER_OCTAL_SIZE = 0xF31;

    public final static int ERROR_INTEGER_HEXIDECIMAL_SIZE = 0xF32;

    public final static int ERROR_LONG_DECIMIAL_SIZE = 0xF33;

    public final static int ERROR_LONG_OCTAL_SIZE = 0xF34;

    public final static int ERROR_LONG_HEXIDECIMAL_SIZE = 0xF35;

    public final static int ERROR_FLOAT_SIZE = 0xF36;

    public final static int ERROR_DOUBLE_SIZE = 0xF37;

    public final static int ERROR_FLOAT = 0xF38;

    public final static int ERROR_UNCLOSED_COMMENT = 0xF40;

    public final static int ERROR_INVALID_OPERATOR = 0xF41;

    public final static int ERROR_INVALID_SEPERATOR = 0xF42;

    public final static int ERROR_INVALID_IDENTIFIER = 0xF43;

    public final static int ERROR_INVALID_RESERVED_WORD = 0xF44;

    public final static int ERROR_END_OFLINE = 0xF48;



    public final static int SPECIAL_ID_STRING = 5;
    public final static int SPECIAL_ID_COLLECTION = 10;
    public final static int SPECIAL_ID_IDENTIFIER = 15;
    public final static int SPECIAL_ID_THIS = 20;
    public final static int SPECIAL_ID_MAP = 25;
    public final static int SPECIAL_ID_STRING_LOWER = 30;


       //JdoqlToken.TYPE_NUMBER;

    public final static int TYPE_STRING = 5;
    public final static int TYPE_NUMBER = 10;
    public final static int TYPE_PC = 15;
    public final static int TYPE_BOOLEAN = 20;
    public final static int TYPE_BOOLEAN_METHOD_NONE = 21;
    public final static int TYPE_BOOLEAN_METHOD_STRING = 22;
    public final static int TYPE_BOOLEAN_METHOD_OBJECT = 23;
    public final static int TYPE_COLLECTION = 25;
    public final static int TYPE_OBJECT = 30;
    public final static int TYPE_ARRAY = 35;
    public final static int TYPE_MAP = 40;

    public final static int TYPE_BOOLEAN_OPERATOR = 100;
    public final static int TYPE_NUMBER_OPERATOR = 110;
    public final static int TYPE_STRING_NUMBER_OPERATOR = 120;
    public final static int TYPE_ALL_OPERATOR = 130;

    public final static int TYPE_DOT_SEPERATOR = 200;
    public final static int TYPE_LEFT_SEPERATOR = 210;
    public final static int TYPE_RIGHT_SEPERATOR = 220;





    private int ID;
    private String contents;
    private int lineNumber;
    private int charBegin;
    private int charEnd;
    private int state;
    public int specialID;
    public int type;

    public OrderingToken prevToken;

    /**
     * Create a new token.
     * The constructor is typically called by the lexer
     *
     * @param ID the id number of the token
     * @param contents A string representing the text of the token
     * @param lineNumber the line number of the input on which this token started
     * @param charBegin the offset into the input in characters at which this token started
     * @param charEnd the offset into the input in characters at which this token ended
     */
    public OrderingToken(int ID, String contents, int lineNumber, int charBegin, int charEnd){
        this (ID, contents, lineNumber, charBegin, charEnd, Token.UNDEFINED_STATE);
    }

    /**
     * Create a new token.
     * The constructor is typically called by the lexer
     *
     * @param ID the id number of the token
     * @param contents A string representing the text of the token
     * @param lineNumber the line number of the input on which this token started
     * @param charBegin the offset into the input in characters at which this token started
     * @param charEnd the offset into the input in characters at which this token ended
     * @param state the state the tokenizer is in after returning this token.
     */
    public OrderingToken(int ID, String contents, int lineNumber, int charBegin, int charEnd, int state){
        this.ID = ID;
        this.contents = new String(contents);
        this.lineNumber = lineNumber;
        this.charBegin = charBegin;
        this.charEnd = charEnd;
        this.state = state;
    }

    /**
     * Get an integer representing the state the tokenizer is in after
     * returning this token.
     * Those who are interested in incremental tokenizing for performance
     * reasons will want to use this method to figure out where the tokenizer
     * may be restarted.  The tokenizer starts in Token.INITIAL_STATE, so
     * any time that it reports that it has returned to this state, the
     * tokenizer may be restarted from there.
     */
    public int getState(){
        return state;
    }

    /**
     * get the ID number of this token
     *
     * @return the id number of the token
     */
    public int getID(){
        return ID;
    }

    /**
     * get the contents of this token
     *
     * @return A string representing the text of the token
     */
    public String getContents(){
        return (new String(contents));
    }

    /**
     * get the line number of the input on which this token started
     *
     * @return the line number of the input on which this token started
     */
    public int getLineNumber(){
        return lineNumber;
    }

    /**
     * get the offset into the input in characters at which this token started
     *
     * @return the offset into the input in characters at which this token started
     */
    public int getCharBegin(){
        return charBegin;
    }

    /**
     * get the offset into the input in characters at which this token ended
     *
     * @return the offset into the input in characters at which this token ended
     */
    public int getCharEnd(){
        return charEnd;
    }

    /**
     * Checks this token to see if it is a reserved word.
     * Reserved words are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
     * Language Specification</A>.
     *
     * @return true if this token is a reserved word, false otherwise
     */
    public boolean isReservedWord(){
        return((ID >> 8) == 0x1);
    }

    /**
     * Checks this token to see if it is an identifier.
     * Identifiers are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
     * Language Specification</A>.
     *
     * @return true if this token is an identifier, false otherwise
     */
    public boolean isIdentifier(){
        return((ID >> 8) == 0x2);
    }

    /**
     * Checks this token to see if it is a literal.
     * Literals are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
     * Language Specification</A>.
     *
     * @return true if this token is a literal, false otherwise
     */
    public boolean isLiteral(){
        return((ID >> 8) == 0x3);
    }

    /**
     * Checks this token to see if it is a Separator.
     * Separators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
     * Language Specification</A>.
     *
     * @return true if this token is a Separator, false otherwise
     */
    public boolean isSeparator(){
        return((ID >> 8) == 0x4);
    }

    /**
     * Checks this token to see if it is a Operator.
     * Operators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
     * Language Specification</A>.
     *
     * @return true if this token is a Operator, false otherwise
     */
    public boolean isOperator(){
        return((ID >> 8) == 0x5);
    }

    /**
     * Checks this token to see if it is a comment.
     *
     * @return true if this token is a comment, false otherwise
     */
    public boolean isComment(){
        return((ID >> 8) == 0xD);
    }

    /**
     * Checks this token to see if it is White Space.
     * Usually tabs, line breaks, form feed, spaces, etc.
     *
     * @return true if this token is White Space, false otherwise
     */
    public boolean isWhiteSpace(){
        return((ID >> 8) == 0xE);
    }

    /**
     * Checks this token to see if it is an Error.
     * Unfinished comments, numbers that are too big, unclosed strings, etc.
     *
     * @return true if this token is an Error, false otherwise
     */
    public boolean isError(){
        return((ID >> 8) == 0xF);
    }

    /**
     * A description of this token.  The description should
     * be appropriate for syntax highlighting.  For example
     * "comment" is returned for a comment.
     *
     * @return a description of this token.
     */
    public String getDescription(){
        if (ID == IDENTIFIER_PARAM_PC || ID == IDENTIFIER_PARAM){
            return ("param");
        } else if (ID == IDENTIFIER_VAR || ID == IDENTIFIER_VAR_PC){
            return("variable");
        } else if (isReservedWord()){
            return("reservedWord");
        } else if (isIdentifier()){
            return("identifier");
        } else if (ID == LITERAL_STRING){
            return("string");
        } else if (isLiteral()){
            return("literal");
        } else if (isSeparator()){
            return("separator");
        } else if (isOperator()){
            return("operator");
        } else if (isComment()){
            return("comment");
        } else if (isWhiteSpace()){
            return("whitespace");
        } else if (isError()){
            return("error");
        } else {
            return("unknown");
        }
    }

    /**
     * get a String that explains the error, if this token is an error.
     *
     * @return a  String that explains the error, if this token is an error, null otherwise.
     */
    public String errorString(){
        String s;
        if (isError()){
            s = "Error on line " + lineNumber + ": ";
            switch (ID){
                case ERROR_IDENTIFIER:
                    s += "Unrecognized Identifier: " + contents;
                    break;
                case ERROR_UNCLOSED_STRING:
                    s += "'\"' expected after " + contents;
                    break;
                case ERROR_MALFORMED_STRING:
                case ERROR_MALFORMED_UNCLOSED_STRING:
                    s += "Illegal character in " + contents;
                    break;
                case ERROR_UNCLOSED_CHARACTER:
                    s += "\"'\" expected after " + contents;
                    break;
                case ERROR_MALFORMED_CHARACTER:
                case ERROR_MALFORMED_UNCLOSED_CHARACTER:
                    s += "Illegal character in " + contents;
                    break;
                case ERROR_INTEGER_DECIMIAL_SIZE:
                case ERROR_INTEGER_OCTAL_SIZE:
                case ERROR_FLOAT:
                    s += "Illegal character in " + contents;
                    break;
                case ERROR_INTEGER_HEXIDECIMAL_SIZE:
                case ERROR_LONG_DECIMIAL_SIZE:
                case ERROR_LONG_OCTAL_SIZE:
                case ERROR_LONG_HEXIDECIMAL_SIZE:
                case ERROR_FLOAT_SIZE:
                case ERROR_DOUBLE_SIZE:
                    s += "Literal out of bounds: " + contents;
                    break;
                case ERROR_UNCLOSED_COMMENT:
                    s += "*/ expected after " + contents;
                    break;
            }

        } else {
            s = null;
        }
        return (s);
    }

    /**
     * get a representation of this token as a human readable string.
     * The format of this string is subject to change and should only be used
     * for debugging purposes.
     *
     * @return a string representation of this token
     */
    public String toString() {
        return ("Token #" + Integer.toHexString(ID) + ": " + getDescription() + " Line " +
                lineNumber + " from " +charBegin + " to " + charEnd + " : " + contents);
    }

    public OrderingToken getPrevToken(){
        return prevToken;
    }

    public void setPrevToken(OrderingToken prevToken) {
        this.prevToken = prevToken;
    }


}
