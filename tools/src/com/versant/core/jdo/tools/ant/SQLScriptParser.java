
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
package com.versant.core.jdo.tools.ant;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Use this class like this:
 *
 * SQLScriptParser shredder = new SQLScriptParser();
 * ArrayList list = shredder.parse(sql,true);
 * for (Iterator iter = list.iterator(); iter.hasNext();) {
 *     SQLScriptPart scriptPart = (SQLScriptPart) iter.next();
 *     System.out.println("start = "+ scriptPart.getStart());
 *     System.out.println("end   = "+ scriptPart.getEnd());
 *     System.out.println( scriptPart.getSql());
 * }
 * @keep-all
 * SQLScriptParser is a sql lexer.  Created with JFlex.
 * The tokens returned should comply with the sql Language Specification
 *
 *
 */
public class SQLScriptParser {

    /** This character denotes the end of file */
    final public static int YYEOF = -1;

    /** initial size of the lookahead buffer */
    final private static int YY_BUFFERSIZE = 16384;

    /** lexical states */
    final public static int YYINITIAL = 0;
    final public static int COMMENT = 1;

    /**
     * Translates characters to character classes
     */
    final private static char[] yycmap = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 13, 0, 4, 13, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        4, 10, 6, 10, 8, 10, 10, 12, 9, 9, 11, 19, 9, 20, 17, 21,
        16, 16, 7, 7, 7, 7, 7, 7, 7, 7, 9, 1, 10, 10, 10, 10,
        10, 0, 15, 0, 0, 18, 0, 2, 0, 0, 0, 0, 0, 0, 0, 3,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 14, 9, 10, 5,
        10, 0, 15, 0, 0, 18, 0, 2, 0, 0, 0, 0, 0, 0, 0, 3,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 10, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * Translates a state to a row index in the transition table
     */
    final private static int yy_rowMap [] = {
        0, 22, 44, 66, 88, 110, 66, 132, 154, 66,
        66, 176, 198, 220, 242, 264, 286, 308, 330, 44,
        352, 374, 154, 176, 66, 66, 396, 418, 440, 66,
        462, 484, 308, 66, 66, 506, 176, 528, 550, 66,
        572, 594, 506, 616, 550, 528, 66, 638
    };

    /**
     * The packed transition table of the DFA (part 0)
     */
    final private static String yy_packed0 =
            "\1\3\1\4\1\5\1\3\1\6\2\7\1\10\1\11" +
            "\1\12\1\13\1\12\1\14\1\6\1\3\1\15\1\10" +
            "\1\16\1\3\1\13\1\17\1\20\13\21\1\22\11\21" +
            "\1\23\1\3\1\0\2\3\1\0\1\3\1\0\1\3" +
            "\6\0\3\3\1\0\1\3\31\0\1\3\1\0\1\3" +
            "\1\24\1\0\1\3\1\0\1\3\6\0\3\3\1\0" +
            "\1\3\7\0\1\6\10\0\1\6\17\0\1\10\10\0" +
            "\1\10\1\25\1\26\12\0\1\27\10\0\1\27\5\0" +
            "\14\30\1\31\1\32\1\33\7\30\1\3\1\0\2\3" +
            "\1\0\1\3\1\0\1\3\4\0\1\34\1\0\3\3" +
            "\1\0\1\3\12\0\1\25\10\0\1\25\31\0\1\35" +
            "\14\0\1\36\12\0\13\21\1\37\11\21\1\40\13\21" +
            "\1\41\11\21\1\42\13\21\1\43\11\21\1\40\7\0" +
            "\1\25\10\0\1\25\1\0\1\26\26\0\2\44\1\0" +
            "\14\30\1\45\1\32\1\33\7\30\14\46\1\47\1\50" +
            "\1\51\1\46\1\52\5\46\15\35\1\0\10\35\13\21" +
            "\1\37\11\21\1\0\13\21\1\0\11\21\1\40\7\0" +
            "\1\53\10\0\1\53\5\0\14\46\1\47\1\50\1\51" +
            "\1\46\1\54\5\46\14\55\1\47\1\0\2\55\1\0" +
            "\5\55\14\46\1\56\1\50\1\51\1\46\1\54\5\46" +
            "\14\54\1\57\1\50\1\60\1\54\1\52\21\54\1\0" +
            "\1\50\1\60\24\54\1\50\1\60\7\54";

    /**
     * The transition table of the DFA
     */
    final private static int yytrans [] = yy_unpack();


    /* error codes */
    final private static int YY_UNKNOWN_ERROR = 0;
    final private static int YY_ILLEGAL_STATE = 1;
    final private static int YY_NO_MATCH = 2;
    final private static int YY_PUSHBACK_2BIG = 3;

    /* error messages for the codes above */
    final private static String YY_ERROR_MSG[] = {
        "Unkown internal scanner error",
        "Internal error: unknown state",
        "Error: could not match input",
        "Error: pushback value was too large"
    };

    /**
     * YY_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
     */
    private final static byte YY_ATTRIBUTE[] = {
        1, 1, 1, 9, 1, 1, 9, 1, 1, 9, 9, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 0, 1, 0, 9, 9, 0, 0, 1, 9, 0, 0,
        0, 9, 9, 0, 1, 0, 1, 9, 0, 0, 1, 0, 0, 1, 9, 0
    };

    /** the input device */
    private java.io.Reader yy_reader;

    /** the current state of the DFA */
    private int yy_state;

    /** the current lexical state */
    private int yy_lexical_state = YYINITIAL;

    /** this buffer contains the current text to be matched and is
     the source of the yytext() string */
    private char yy_buffer[] = new char[YY_BUFFERSIZE];

    /** the textposition at the last accepting state */
    private int yy_markedPos;

    /** the textposition at the last state to be included in yytext */
    private int yy_pushbackPos;

    /** the current text position in the buffer */
    private int yy_currentPos;

    /** startRead marks the beginning of the yytext() string in the buffer */
    private int yy_startRead;

    /** endRead marks the last character in the buffer, that has been read
     from input */
    private int yy_endRead;

    /** number of newlines encountered up to the start of the matched text */
    private int yyline;

    /** the number of characters up to the start of the matched text */
    private int yychar;

    /**
     * the number of characters from the last newline up to the start of the
     * matched text
     */
    private int yycolumn;

    /**
     * yy_atBOL == true <=> the scanner is currently at the beginning of a line
     */
    private boolean yy_atBOL = true;

    /** yy_atEOF == true <=> the scanner is at the EOF */
    private boolean yy_atEOF;

    /* user code: */
    private int lastToken;
    private int nextState = YYINITIAL;
    private StringBuffer commentBuffer = new StringBuffer();
    private int commentNestCount = 0;
    private int commentStartLine = 0;
    private int commentStartChar = 0;

    /**
     * next Token method that allows you to control if whitespace and comments are
     * returned as tokens.
     */
    public Token getNextToken(boolean returnComments, boolean returnWhiteSpace) throws IOException {
        Token t = getNextToken();
        while (t != null && ((!returnWhiteSpace && t.isWhiteSpace()) || (!returnComments && t.isComment()))) {
            t = getNextToken();
        }
        return (t);
    }

    public SQLScriptParser() {
    }

    /**
     * Prints out tokens from a file or System.in.
     * If no arguments are given, System.in will be used for input.
     * If more arguments are given, the first argument will be used as
     * the name of the file to use as input
     *
     * @param args program arguments, of which the first is a filename
     */
    public static void main(String[] args) {
        String sql =
                "123456789            ;-- kdlfhjkl; sdfghjdl;fkhkdfjghl fghldfkklh flhkdfhj \n" +
                " lkh"+
                "CREATE TABLE     temp_abstract1_a (\n" +
                " ';;;;;\\n'" +
                "    abstract1_id INTEGER NOT NULL,          /* <pk> */\n" +
                "    abs1 VARCHAR(25),                       /* abs1 */\n" +
                "    jdo_class INTEGER NOT NULL,             /* <class-id> */\n" +
                "    jdo_version SMALLINT NOT NULL,          /* <opt-lock> */\n" +
                "    abs2 VARCHAR(190),                      /* Abstract2.abs2 */\n" +
                "    conc1 VARCHAR(190),                     /* Concrete1.conc1 */\n" +
                "    conc2 VARCHAR(190)                      /* Concrete2.conc2 */\n" +
                ");\n" +
                "INSERT INTO temp_abstract1_a (abstract1_id, abs1, jdo_class, jdo_version, abs2, conc1, conc2) \n" +
                "SELECT abstract1_id, \n" +
                "       CAST(abs1 AS VARCHAR(25)), \n" +
                "       jdo_class, \n" +
                "       jdo_version, \n" +
                "       abs2, \n" +
                "       conc1, \n" +
                "       conc2\n" +
                "  FROM abstract1;\n" +
                "DROP TABLE abstract1;\n" +
                "CREATE TABLE abstract1 (\n" +
                "    abstract1_id INTEGER NOT NULL,          /* <pk> */\n" +
                "    abs1 VARCHAR(25),                       /* abs1 */\n" +
                "    jdo_class INTEGER NOT NULL,             /* <class-id> */\n" +
                "    jdo_version SMALLINT NOT NULL,          /* <opt-lock> */\n" +
                "    abs2 VARCHAR(190),                      /* Abstract2.abs2 */\n" +
                "    conc1 VARCHAR(190),                     /* Concrete1.conc1 */\n" +
                "    conc2 VARCHAR(190),                     /* Concrete2.conc2 */\n" +
                "    CONSTRAINT pk_abstract1 PRIMARY KEY (abstract1_id)\n" +
                ");\n" +
                "INSERT INTO abstract1 (abstract1_id, abs1, jdo_class, jdo_version, abs2, conc1, conc2) \n" +
                "SELECT abstract1_id, \n" +
                "       abs1, \n" +
                "       jdo_class, \n" +
                "       jdo_version, \n" +
                "       abs2, \n" +
                "       conc1, \n" +
                "       conc2\n" +

                "  FROM temp_abstract1_a;\n" +
                "DROP TABLE temp_abstract1_a;\n" +
                "\n" +
                "ALTER TABLE emp_super_person3 ADD CONSTRAINT ref_emp_super_person3_person FOREIGN KEY (person_id) REFERENCES person(person_id);\n" +
                "\n" +
                "ALTER TABLE person3_person ADD CONSTRAINT ref_person3_person_person FOREIGN KEY (person_id) REFERENCES person(person_id);\n" +
                "";

        try {
            SQLScriptParser shredder = new SQLScriptParser();
            ArrayList list = shredder.parse(sql, true);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                SQLScriptPart scriptPart = (SQLScriptPart) iter.next();
                System.out.println("start = " + scriptPart.getStart());
                System.out.println("end   = " + scriptPart.getEnd());
                System.out.println(scriptPart.getSql());
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList parse(String sql, boolean eraseNewLine) throws IOException {
        StringReader in = new StringReader(sql+ "\n;");
        return parse(in, eraseNewLine);
    }

    private ArrayList parse(Reader sql, boolean eraseNewLine) throws IOException {
        SQLScriptParser shredder = new SQLScriptParser(sql);
        Token t;
        boolean first = true;
        ArrayList list = new ArrayList();
        SQLScriptPart part = new SQLScriptPart();
        while ((t = shredder.getNextToken()) != null) {

            switch (t.getID()) {
                case Token.WHITE_SPACE:
                    if (!first) {
                        if (eraseNewLine) {
                            part.addSql(' ');
                        } else {
                            part.addSql(t.getContents());
                        }
                    }
                    break;

                case Token.COMMENT_END_OF_LINE:
                case Token.COMMENT_TRADITIONAL:
                    break;

                case Token.RESERVED_WORD:
                    if (!first) {
                        list.add(part);
                        part = new SQLScriptPart();
                        first = true;
                    }
                    break;

                default:
                    if (first) {
                        part.setStart(t.getCharBegin());
                        first = false;
                    }
                    part.addSql(t.getContents());
                    part.setEnd(t.getCharEnd());
                    break;
            }
        }


        return list;
    }

    /**
     * @keep-all
     */
    public static class SQLScriptPart {
        private int start;
        private int end;
        private StringBuffer sql = new StringBuffer();

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getSql() {
            return sql.toString().trim();
        }

        public void addSql(String sql) {
            this.sql.append(sql);
        }

        public void addSql(char sql) {
            this.sql.append(sql);
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }
    }

    /**
     * @keep-all
     * A Token is a token that is returned by a lexer that is lexing an SQL
     * source file.  It has several attributes describing the token:
     * The type of token, the text of the token, the line number on which it
     * occurred, the number of characters into the input at which it started, and
     * similarly, the number of characters into the input at which it ended. <br>
     */
    public static class Token {
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
         * A reserved word (keyword)
         */
        public final static int RESERVED_WORD = 0x100;

        /**
         * A variable, name, or other identifier
         */
        public final static int IDENTIFIER = 0x200;

        /**
         * A string literal
         */
        public final static int LITERAL_STRING = 0x300;
        /**
         * A bit-string
         */
        public final static int LITERAL_BIT_STRING = 0x310;
        /**
         * An integer
         */
        public final static int LITERAL_INTEGER = 0x320;
        /**
         * A floating point
         */
        public final static int LITERAL_FLOAT = 0x330;

        /**
         * A separator
         */
        public final static int SEPARATOR = 0x400;

        /**
         * A separator
         */
        public final static int DOT_SEPARATOR = 0x410;

        /**
         * An operator
         */
        public final static int OPERATOR = 0x500;

        /**
         * C style comment, (except possibly nested)
         */
        public final static int COMMENT_TRADITIONAL = 0xD00;

        /**
         * a -- to end of line comment.
         */
        public final static int COMMENT_END_OF_LINE = 0xD10;

        /**
         * White space
         */
        public final static int WHITE_SPACE = 0xE00;

        /**
         * An error
         */
        public final static int ERROR = 0xF00;
        /**
         * An comment start embedded in an operator
         */
        public final static int ERROR_UNCLOSED_COMMENT = 0xF02;
        /**
         * An comment start embedded in an operator
         */
        public final static int ERROR_UNCLOSED_STRING = 0xF03;
        /**
         * An comment start embedded in an operator
         */
        public final static int ERROR_UNCLOSED_BIT_STRING = 0xF04;
        /**
         * An comment start embedded in an operator
         */
        public final static int ERROR_BAD_BIT_STRING = 0xF05;

        private int ID;
        private String contents;
        private int lineNumber;
        private int charBegin;
        private int charEnd;
        private int state;

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
        public Token(int ID, String contents, int lineNumber, int charBegin, int charEnd) {
            this(ID, contents, lineNumber, charBegin, charEnd, Token.UNDEFINED_STATE);
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
        public Token(int ID, String contents, int lineNumber, int charBegin, int charEnd, int state) {
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
        public int getState() {
            return state;
        }

        /**
         * get the ID number of this token
         *
         * @return the id number of the token
         */
        public int getID() {
            return ID;
        }

        /**
         * get the contents of this token
         *
         * @return A string representing the text of the token
         */
        public String getContents() {
            return (new String(contents));
        }

        /**
         * get the line number of the input on which this token started
         *
         * @return the line number of the input on which this token started
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * get the offset into the input in characters at which this token started
         *
         * @return the offset into the input in characters at which this token started
         */
        public int getCharBegin() {
            return charBegin;
        }

        /**
         * get the offset into the input in characters at which this token ended
         *
         * @return the offset into the input in characters at which this token ended
         */
        public int getCharEnd() {
            return charEnd;
        }

        /**
         * Checks this token to see if it is a reserved word.
         * Reserved words are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
         * Language Specification</A>.
         *
         * @return true if this token is a reserved word, false otherwise
         */
        public boolean isReservedWord() {
            return ((ID >> 8) == 0x1);
        }

        /**
         * Checks this token to see if it is an identifier.
         * Identifiers are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
         * Language Specification</A>.
         *
         * @return true if this token is an identifier, false otherwise
         */
        public boolean isIdentifier() {
            return ((ID >> 8) == 0x2);
        }

        /**
         * Checks this token to see if it is a literal.
         * Literals are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
         * Language Specification</A>.
         *
         * @return true if this token is a literal, false otherwise
         */
        public boolean isLiteral() {
            return ((ID >> 8) == 0x3);
        }

        /**
         * Checks this token to see if it is a Separator.
         * Separators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
         * Language Specification</A>.
         *
         * @return true if this token is a Separator, false otherwise
         */
        public boolean isSeparator() {
            return ((ID >> 8) == 0x4);
        }

        /**
         * Checks this token to see if it is a Operator.
         * Operators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
         * Language Specification</A>.
         *
         * @return true if this token is a Operator, false otherwise
         */
        public boolean isOperator() {
            return ((ID >> 8) == 0x5);
        }

        /**
         * Checks this token to see if it is a comment.
         *
         * @return true if this token is a comment, false otherwise
         */
        public boolean isComment() {
            return ((ID >> 8) == 0xD);
        }

        /**
         * Checks this token to see if it is White Space.
         * Usually tabs, line breaks, form feed, spaces, etc.
         *
         * @return true if this token is White Space, false otherwise
         */
        public boolean isWhiteSpace() {
            return ((ID >> 8) == 0xE);
        }

        /**
         * Checks this token to see if it is an Error.
         * Unfinished comments, numbers that are too big, unclosed strings, etc.
         *
         * @return true if this token is an Error, false otherwise
         */
        public boolean isError() {
            return ((ID >> 8) == 0xF);
        }

        /**
         * A description of this token.  The description should
         * be appropriate for syntax highlighting.  For example
         * "comment" is returned for a comment.
         *
         * @return a description of this token.
         */
        public String getDescription() {
            if (isReservedWord()) {
                return ("reservedWord");
            } else if (isIdentifier()) {
                return ("identifier");
            } else if (isLiteral()) {
                return ("literal");
            } else if (isSeparator()) {
                return ("separator");
            } else if (isOperator()) {
                return ("operator");
            } else if (isComment()) {
                return ("comment");
            } else if (isWhiteSpace()) {
                return ("whitespace");
            } else if (isError()) {
                return ("error");
            } else {
                return ("unknown");
            }
        }

        /**
         * get a String that explains the error, if this token is an error.
         *
         * @return a  String that explains the error, if this token is an error, null otherwise.
         */
        public String errorString() {
            String s;
            if (isError()) {
                s = "Error on line " + lineNumber + ": ";
                switch (ID) {
                    case ERROR:
                        s += "Unexpected token: " + contents;
                        break;
                    case ERROR_UNCLOSED_COMMENT:
                        s += "Unclosed comment: " + contents;
                        break;
                    case ERROR_UNCLOSED_STRING:
                        s += "Unclosed string literal: " + contents;
                        break;
                    case ERROR_UNCLOSED_BIT_STRING:
                        s += "Unclosed bit-string literal: " + contents;
                        break;
                    case ERROR_BAD_BIT_STRING:
                        s += "Bit-strings can only contain 0 and 1: " + contents;
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
                    lineNumber + " from " + charBegin + " to " + charEnd + " : " + contents);
        }
    }


    /**
     * Closes the current input stream, and resets the scanner to read from a new input stream.
     * All internal variables are reset, the old input stream  cannot be reused
     * (content of the internal buffer is discarded and lost).
     * The lexical state is set to the initial state.
     * Subsequent tokens read from the lexer will start with the line, char, and column
     * values given here.
     *
     * @param reader The new input.
     * @param yyline The line number of the first token.
     * @param yychar The position (relative to the start of the stream) of the first token.
     * @param yycolumn The position (relative to the line) of the first token.
     * @throws IOException if an IOExecption occurs while switching readers.
     */
    public void reset(java.io.Reader reader, int yyline, int yychar, int yycolumn) throws IOException {
        yyreset(reader);
        this.yyline = yyline;
        this.yychar = yychar;
        this.yycolumn = yycolumn;
    }

    public void setReader(java.io.Reader in) {
        this.yy_reader = in;
    }


    /**
     * Creates a new scanner
     * There is also a java.io.InputStream version of this constructor.
     *
     * @param   in  the java.io.Reader to read input from.
     */
    public SQLScriptParser(java.io.Reader in) {
        this.yy_reader = in;
    }

    /**
     * Creates a new scanner.
     * There is also java.io.Reader version of this constructor.
     *
     * @param   in  the java.io.Inputstream to read input from.
     */
    public SQLScriptParser(java.io.InputStream in) {
        this(new java.io.InputStreamReader(in));
    }

    /**
     * Unpacks the split, compressed DFA transition table.
     *
     * @return the unpacked transition table
     */
    private static int[] yy_unpack() {
        int[] trans = new int[660];
        int offset = 0;
        offset = yy_unpack(yy_packed0, offset, trans);
        return trans;
    }

    /**
     * Unpacks the compressed DFA transition table.
     *
     * @param packed   the packed transition table
     * @return         the index of the last entry
     */
    private static int yy_unpack(String packed, int offset, int[] trans) {
        int i = 0;       /* index in packed string  */
        int j = offset;  /* index in unpacked array */
        int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            int value = packed.charAt(i++);
            value--;
            do trans[j++] = value; while (--count > 0);
        }
        return j;
    }


    /**
     * Refills the input buffer.
     *
     * @return      <code>false</code>, iff there was new input.
     *
     * @exception   IOException  if any I/O-Error occurs
     */
    private boolean yy_refill() throws java.io.IOException {

        /* first: make room (if you can) */
        if (yy_startRead > 0) {
            System.arraycopy(yy_buffer, yy_startRead,
                    yy_buffer, 0,
                    yy_endRead - yy_startRead);

            /* translate stored positions */
            yy_endRead -= yy_startRead;
            yy_currentPos -= yy_startRead;
            yy_markedPos -= yy_startRead;
            yy_pushbackPos -= yy_startRead;
            yy_startRead = 0;
        }

        /* is the buffer big enough? */
        if (yy_currentPos >= yy_buffer.length) {
            /* if not: blow it up */
            char newBuffer[] = new char[yy_currentPos * 2];
            System.arraycopy(yy_buffer, 0, newBuffer, 0, yy_buffer.length);
            yy_buffer = newBuffer;
        }

        /* finally: fill the buffer with new input */
        int numRead = yy_reader.read(yy_buffer, yy_endRead,
                yy_buffer.length - yy_endRead);

        if (numRead < 0) {
            return true;
        } else {
            yy_endRead += numRead;
            return false;
        }
    }


    /**
     * Closes the input stream.
     */
    final public void yyclose() throws java.io.IOException {
        yy_atEOF = true;            /* indicate end of file */
        yy_endRead = yy_startRead;  /* invalidate buffer    */

        if (yy_reader != null)
            yy_reader.close();
    }


    /**
     * Closes the current stream, and resets the
     * scanner to read from a new input stream.
     *
     * All internal variables are reset, the old input stream
     * <b>cannot</b> be reused (internal buffer is discarded and lost).
     * Lexical state is set to <tt>YY_INITIAL</tt>.
     *
     * @param reader   the new input stream
     */
    final public void yyreset(java.io.Reader reader) throws java.io.IOException {
        yyclose();
        yy_reader = reader;
        yy_atBOL = true;
        yy_atEOF = false;
        yy_endRead = yy_startRead = 0;
        yy_currentPos = yy_markedPos = yy_pushbackPos = 0;
        yyline = yychar = yycolumn = 0;
        yy_lexical_state = YYINITIAL;
    }


    /**
     * Returns the current lexical state.
     */
    final public int yystate() {
        return yy_lexical_state;
    }


    /**
     * Enters a new lexical state
     *
     * @param newState the new lexical state
     */
    final public void yybegin(int newState) {
        yy_lexical_state = newState;
    }


    /**
     * Returns the text matched by the current regular expression.
     */
    final public String yytext() {
        return new String(yy_buffer, yy_startRead, yy_markedPos - yy_startRead);
    }


    /**
     * Returns the character at position <tt>pos</tt> from the
     * matched text.
     *
     * It is equivalent to yytext().charAt(pos), but faster
     *
     * @param pos the position of the character to fetch.
     *            A value from 0 to yylength()-1.
     *
     * @return the character at position pos
     */
    final public char yycharat(int pos) {
        return yy_buffer[yy_startRead + pos];
    }


    /**
     * Returns the length of the matched text region.
     */
    final public int yylength() {
        return yy_markedPos - yy_startRead;
    }


    /**
     * Reports an error that occured while scanning.
     *
     * In a wellformed scanner (no or only correct usage of
     * yypushback(int) and a match-all fallback rule) this method
     * will only be called with things that "Can't Possibly Happen".
     * If this method is called, something is seriously wrong
     * (e.g. a JFlex bug producing a faulty scanner etc.).
     *
     * Usual syntax/scanner level error handling should be done
     * in error fallback rules.
     *
     * @param   errorCode  the code of the errormessage to display
     */
    private void yy_ScanError(int errorCode) {
        String message;
        try {
            message = YY_ERROR_MSG[errorCode];
        } catch (ArrayIndexOutOfBoundsException e) {
            message = YY_ERROR_MSG[YY_UNKNOWN_ERROR];
        }

        throw new Error(message);
    }


    /**
     * Pushes the specified amount of characters back into the input stream.
     *
     * They will be read again by then next call of the scanning method
     *
     * @param number  the number of characters to be read again.
     *                This number must not be greater than yylength()!
     */
    private void yypushback(int number) {
        if (number > yylength())
            yy_ScanError(YY_PUSHBACK_2BIG);

        yy_markedPos -= number;
    }


    /**
     * Resumes scanning until the next regular expression is matched,
     * the end of input is encountered or an I/O-Error occurs.
     *
     * @return      the next token
     * @exception   IOException  if any I/O-Error occurs
     */
    public Token getNextToken() throws java.io.IOException {
        int yy_input;
        int yy_action;

        // cached fields:
        int yy_currentPos_l;
        int yy_startRead_l;
        int yy_markedPos_l;
        int yy_endRead_l = yy_endRead;
        char[] yy_buffer_l = yy_buffer;
        char[] yycmap_l = yycmap;

        int[] yytrans_l = yytrans;
        int[] yy_rowMap_l = yy_rowMap;
        byte[] yy_attr_l = YY_ATTRIBUTE;

        while (true) {
            yy_markedPos_l = yy_markedPos;

            yychar += yy_markedPos_l - yy_startRead;

            boolean yy_r = false;
            for (yy_currentPos_l = yy_startRead; yy_currentPos_l < yy_markedPos_l;
                 yy_currentPos_l++) {
                switch (yy_buffer_l[yy_currentPos_l]) {
                    case '\u000B':
                    case '\u000C':
                    case '\u0085':
                    case '\u2028':
                    case '\u2029':
                        yyline++;
                        yy_r = false;
                        break;
                    case '\r':
                        yyline++;
                        yy_r = true;
                        break;
                    case '\n':
                        if (yy_r)
                            yy_r = false;
                        else {
                            yyline++;
                        }
                        break;
                    default:
                        yy_r = false;
                }
            }

            if (yy_r) {
                // peek one character ahead if it is \n (if we have counted one line too much)
                boolean yy_peek;
                if (yy_markedPos_l < yy_endRead_l)
                    yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
                else if (yy_atEOF)
                    yy_peek = false;
                else {
                    boolean eof = yy_refill();
                    yy_markedPos_l = yy_markedPos;
                    yy_buffer_l = yy_buffer;
                    if (eof)
                        yy_peek = false;
                    else
                        yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
                }
                if (yy_peek) yyline--;
            }
            yy_action = -1;

            yy_startRead_l = yy_currentPos_l = yy_currentPos =
                    yy_startRead = yy_markedPos_l;

            yy_state = yy_lexical_state;


            yy_forAction: {
                while (true) {

                    if (yy_currentPos_l < yy_endRead_l)
                        yy_input = yy_buffer_l[yy_currentPos_l++];
                    else if (yy_atEOF) {
                        yy_input = YYEOF;
                        break yy_forAction;
                    } else {
                        // store back cached positions
                        yy_currentPos = yy_currentPos_l;
                        yy_markedPos = yy_markedPos_l;
                        boolean eof = yy_refill();
                        // get translated positions and possibly new buffer
                        yy_currentPos_l = yy_currentPos;
                        yy_markedPos_l = yy_markedPos;
                        yy_buffer_l = yy_buffer;
                        yy_endRead_l = yy_endRead;
                        if (eof) {
                            yy_input = YYEOF;
                            break yy_forAction;
                        } else {
                            yy_input = yy_buffer_l[yy_currentPos_l++];
                        }
                    }
                    int yy_next = yytrans_l[yy_rowMap_l[yy_state] + yycmap_l[yy_input]];
                    if (yy_next == -1) break yy_forAction;
                    yy_state = yy_next;

                    int yy_attributes = yy_attr_l[yy_state];
                    if ((yy_attributes & 1) == 1) {
                        yy_action = yy_state;
                        yy_markedPos_l = yy_currentPos_l;
                        if ((yy_attributes & 8) == 8) break yy_forAction;
                    }

                }
            }

            // store back cached position
            yy_markedPos = yy_markedPos_l;

            switch (yy_action) {

                case 6:
                case 11:
                case 17:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.ERROR;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 49:
                    break;
                case 46:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.LITERAL_BIT_STRING;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 50:
                    break;
                case 8:
                case 10:
                case 14:
                case 15:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.OPERATOR;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 51:
                    break;
                case 25:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.ERROR_UNCLOSED_STRING;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 52:
                    break;
                case 38:
                case 45:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.ERROR_UNCLOSED_BIT_STRING;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 53:
                    break;
                case 34:
                    {
                        nextState = COMMENT;
                        commentBuffer.append(yytext());
                        commentNestCount++;
                        yybegin(nextState);
                    }
                case 54:
                    break;
                case 9:
                case 13:
                case 22:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.SEPARATOR;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 55:
                    break;
                case 2:
                case 4:
                case 12:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.IDENTIFIER;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 56:
                    break;
                case 3:
                case 19:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.RESERVED_WORD;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 57:
                    break;
                case 18:
                case 33:
                    {
                        commentNestCount--;
                        commentBuffer.append(yytext());
                        if (commentNestCount == 0) {
                            nextState = YYINITIAL;
                            lastToken = Token.COMMENT_TRADITIONAL;
                            Token t = (new Token(lastToken, commentBuffer.toString(), commentStartLine, commentStartChar, commentStartChar + commentBuffer.length(), nextState));
                            yybegin(nextState);
                            return (t);
                        }
                    }
                case 58:
                    break;
                case 20:
                case 42:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.LITERAL_FLOAT;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 59:
                    break;
                case 24:
                case 36:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.LITERAL_STRING;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 60:
                    break;
                case 29:
                    {
                        nextState = COMMENT;
                        commentBuffer.setLength(0);
                        commentBuffer.append(yytext());
                        commentNestCount = 1;
                        commentStartLine = yyline;
                        commentStartChar = yychar;
                        yybegin(nextState);
                    }
                case 61:
                    break;
                case 28:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.COMMENT_END_OF_LINE;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 62:
                    break;
                case 39:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.ERROR_BAD_BIT_STRING;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 63:
                    break;
                case 1:
                case 16:
                    {
                        nextState = COMMENT;
                        commentBuffer.append(yytext());
                        yybegin(nextState);
                    }
                case 64:
                    break;
                case 0:
                case 5:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.WHITE_SPACE;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 65:
                    break;
                case 7:
                    {
                        nextState = YYINITIAL;
                        lastToken = Token.LITERAL_INTEGER;
                        String text = yytext();
                        Token t = (new Token(lastToken, text, yyline, yychar, yychar + text.length(), nextState));
                        yybegin(nextState);
                        return (t);
                    }
                case 66:
                    break;
                default:
                    if (yy_input == YYEOF && yy_startRead == yy_currentPos) {
                        yy_atEOF = true;
                        switch (yy_lexical_state) {
                            case COMMENT:
                                {
                                    nextState = YYINITIAL;
                                    lastToken = Token.ERROR_UNCLOSED_COMMENT;
                                    Token t = (new Token(lastToken, commentBuffer.toString(), commentStartLine, commentStartChar, commentStartChar + commentBuffer.length(), nextState));
                                    yybegin(nextState);
                                    return (t);
                                }
                            case 49:
                                break;
                            default:
                                return null;
                        }
                    } else {
                        yy_ScanError(YY_NO_MATCH);
                    }
            }
        }
    }


}

