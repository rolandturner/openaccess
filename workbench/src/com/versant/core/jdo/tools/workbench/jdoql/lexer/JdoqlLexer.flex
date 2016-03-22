
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
package za.co.hemtech.jdo.tools.workbench.jdoql.lexer;

import java.io.*;

import za.co.hemtech.jdo.tools.workbench.model.MdClass;
import za.co.hemtech.jdo.tools.workbench.model.MdField;
import za.co.hemtech.jdo.tools.workbench.jdoql.insight.*;
import za.co.hemtech.jdo.metadata.MDStatics;

import java.util.*;

/**
 * @keep-all
 * JdoqlLexer is a java lexer.  Created with JFlex.
 * The tokens returned should comply with the JDOQL Language Specification
 * @see JdoqlToken
 * @version $Id: JdoqlLexer.flex,v 1.1 2005/03/08 08:42:16 david Exp $
%%

%public
%class JdoqlLexer
%implements Lexer
%function getNextToken
%type Token

%{
    private StringBuffer commentText = new StringBuffer();
    private int commentStart;
    private int commentLineStart;
    private JdoqlToken lastToken;
    private String lastTokenImage;
    private boolean inComment = false;
    private MdClass mdClass;
    private MdClass currentMdClass;
    private HashMap mapVars = new HashMap();
    private HashMap mapParams = new HashMap();
    private HashMap classNames = new HashMap();
    private boolean thisFlag = false;
    private int bracketCount = 0;
    private int castCount = 0;
    private String castName;

    /**
     * next Token method that allows you to control if whitespace and comments are
     * returned as tokens.
     */
    public Token getNextToken(boolean returnComments, boolean returnWhiteSpace)
            throws IOException {
        Token t = getNextToken();
        while (t != null && ((!returnWhiteSpace && t.isWhiteSpace()) || (!returnComments && t.isComment()))) {
            t = getNextToken();
        }
        return (t);
    }

    //
    private int getType(MdField field) {
        Class clazz = field.getField().getType();

        if (clazz.isArray()) {
            return JdoqlToken.TYPE_ARRAY;
        } else if (clazz.isPrimitive()) {
            if (clazz.getName().equals("boolean")) {
                return JdoqlToken.TYPE_BOOLEAN;
            } else {
                return JdoqlToken.TYPE_NUMBER;
            }
        } else {
            if (field.getCategory() == MDStatics.CATEGORY_REF) {
                return JdoqlToken.TYPE_PC;
            } else if (field.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                return JdoqlToken.TYPE_COLLECTION;
            } else if (field.getCategory() == MDStatics.CATEGORY_MAP) {
                return JdoqlToken.TYPE_MAP;
            } else if (clazz.equals(String.class)) {
                return JdoqlToken.TYPE_STRING;
            } else {
                if (clazz.equals(Boolean.class)) {
                    return JdoqlToken.TYPE_BOOLEAN;
                } else if (clazz.equals(Byte.class) ||
                        clazz.equals(Integer.class) ||
                        clazz.equals(Short.class) ||
                        clazz.equals(Character.class) ||
                        clazz.equals(Double.class) ||
                        clazz.equals(Float.class) ||
                        clazz.equals(Long.class) ||
                        clazz.equals(java.util.Date.class) ||
                        clazz.equals(java.math.BigDecimal.class) ||
                        clazz.equals(java.math.BigInteger.class)) {
                    return JdoqlToken.TYPE_NUMBER;

                } else {
                    return JdoqlToken.TYPE_OBJECT;
                }
            }
        }
    }

    private int getVarType(String field) {
        String type = (String)mapVars.get(field);
        if (type.endsWith("[]")) {
            return JdoqlToken.TYPE_ARRAY;
        } else if (type.equals("String")) {
            return JdoqlToken.TYPE_STRING;
        } else if (type.equals("int") ||
                type.equals("byte") ||
                type.equals("short") ||
                type.equals("char") ||
                type.equals("float") ||
                type.equals("double") ||
                type.equals("long") ||
                type.equals("Byte") ||
                type.equals("Integer") ||
                type.equals("Short") ||
                type.equals("Character") ||
                type.equals("Double") ||
                type.equals("Float") ||
                type.equals("Long") ||
                type.equals("Date") ||
                type.equals("BigDecimal") ||
                type.equals("BigInteger")) {
            return JdoqlToken.TYPE_NUMBER;
        } else if (type.equals("boolean") ||
                type.equals("Boolean")) {
            return JdoqlToken.TYPE_BOOLEAN;
        } else {
            return JdoqlToken.TYPE_OBJECT;
        }

    }

    private int getParamType(String field) {
        String type = (String)mapParams.get(field);
        if (type.endsWith("[]")) {
            return JdoqlToken.TYPE_ARRAY;
        } else if (type.equals("String")) {
            return JdoqlToken.TYPE_STRING;
        } else if (type.equals("int") ||
                type.equals("byte") ||
                type.equals("short") ||
                type.equals("char") ||
                type.equals("float") ||
                type.equals("double") ||
                type.equals("long") ||
                type.equals("Byte") ||
                type.equals("Integer") ||
                type.equals("Short") ||
                type.equals("Character") ||
                type.equals("Double") ||
                type.equals("Float") ||
                type.equals("Long") ||
                type.equals("Date") ||
                type.equals("BigDecimal") ||
                type.equals("BigInteger")) {
            return JdoqlToken.TYPE_NUMBER;
        } else if (type.equals("boolean") ||
                type.equals("Boolean")) {
            return JdoqlToken.TYPE_BOOLEAN;
        } else {
            return JdoqlToken.TYPE_OBJECT;
        }

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
        InputStream in;
        try {
            if (args.length > 0) {
                File f = new File(args[0]);
                if (f.exists()) {
                    if (f.canRead()) {
                        in = new FileInputStream(f);
                    } else {
                        throw new IOException("Could not open " + args[0]);
                    }
                } else {
                    throw new IOException("Could not find " + args[0]);
                }
            } else {
                in = System.in;
            }
            JdoqlLexer shredder = new JdoqlLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != JdoqlToken.WHITE_SPACE) {
                    System.out.println(t);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
     * @param reader   The new input.
     * @param yyline   The line number of the first token.
     * @param yychar   The position (relative to the start of the stream) of the first token.
     * @param yycolumn The position (relative to the line) of the first token.
     * @throws IOException if an IOExecption occurs while switching readers.
     */
    public void reset(java.io.Reader reader, int yyline, int yychar,
            int yycolumn) throws IOException {
        yyreset(reader);
        this.yyline = yyline;
        this.yychar = yychar;
        this.yycolumn = yycolumn;
        currentMdClass = mdClass;
        thisFlag = false;

    }

    public JdoqlLexer() {}

    public JdoqlLexer(String s) {
        this(new StringReader(s));
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public MdClass getCurrentMdClass() {
        if (currentMdClass == null) {
            return mdClass;
        }
        return currentMdClass;
    }

    public void setMdClass(MdClass mdClass) {
        this.mdClass = mdClass;
        this.currentMdClass = mdClass;
        if (mdClass != null) {
            List list = mdClass.getMdProject().getAllClasses();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                MdClass clazz = (MdClass)iter.next();
                classNames.put(clazz.getName(), clazz);
            }
        }
    }

    public void setReader(java.io.Reader in) {
        this.yy_reader = in;
    }

    public void setVars(HashMap map) {
        this.mapVars = map;
    }

    public void setParams(HashMap map) {
        this.mapParams = map;
    }

    private boolean isNow(JdoqlToken t, int currentPosition) {
        if (((t.getCharBegin()) <= currentPosition
                && (t.getCharEnd()) >= currentPosition)) {
            return true;
        } else {
            return false;
        }
    }

    public BodyDataWrapper getClassData(MdClass mdClassCurrent) {
        FieldDisplay.BIGGEST_LENGHT = 0;
        HashSet dataSet = new HashSet();

        if (mdClassCurrent == null) return null;
        List list = mdClassCurrent.getFieldList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MdField f = (MdField)iter.next();
            if (f.getField() != null && f.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                DisplayField disp = new DisplayField(f.getField().getName(),
                        getShortName(f.getField().getType().getName()), null);
                disp.setLenght();
                dataSet.add(disp);
            }
        }

        if (mdClassCurrent.equals(mdClass) && !thisFlag) {
            Iterator iter = mapVars.keySet().iterator();
            while (iter.hasNext()) {
                String name = (String)iter.next();
                Object o = mapVars.get(name);
                if (o instanceof MdClass) {
                    DisplayVariable var = new DisplayVariable(name,
                            ((MdClass)o).getName(), null);
                    var.setLenght();
                    dataSet.add(var);
                } else {
                    DisplayVariable var = new DisplayVariable(name, (String)o,
                            null);
                    var.setLenght();
                    dataSet.add(var);
                }
            }
            iter = mapParams.keySet().iterator();
            while (iter.hasNext()) {
                String name = (String)iter.next();
                Object o = mapParams.get(name);
                if (o instanceof MdClass) {
                    DisplayParam param = new DisplayParam(name,
                            ((MdClass)o).getName(), null);
                    param.setLenght();
                    dataSet.add(param);
                } else {
                    DisplayParam param = new DisplayParam(name, (String)o,
                            null);
                    param.setLenght();
                    dataSet.add(param);
                }
            }
        }

        HashSet setToFill = new HashSet();
        fillInheritedFields(mdClassCurrent, setToFill);
        dataSet.addAll(setToFill);


        // and .sql
        ArrayList returnList = new ArrayList(dataSet);

        DisplayMethod dispSql = new DisplayMethod("sql()", "void",
                "String exp");
        dispSql.setLenght();
        returnList.add(dispSql);

        Collections.sort(returnList);
        return new BodyDataWrapper(getShortName(mdClassCurrent.getName()),
                returnList.toArray());
    }

    private String getShortName(String longName) {
        if (longName.lastIndexOf('.') > 0) {
            return longName.substring(longName.lastIndexOf('.') + 1,
                    longName.length());
        } else {
            return longName;
        }
    }

    private void fillInheritedFields(MdClass mdClassCurrent,
            HashSet listToFill) {
        String superClassString = mdClassCurrent.getPcSuperclassStr();
        if (superClassString == null) return;
        MdClass mdClass = mdClassCurrent.getMdPackage().findClass(
                superClassString);
        if (mdClass == null) return;
        List list = mdClass.getFieldList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MdField f = (MdField)iter.next();
            if (f.getField() != null && f.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                DisplayInheritedField disp = new DisplayInheritedField(
                        f.getField().getName(),
                        getShortName(f.getField().getType().getName()), null);
                disp.setLenght();
                listToFill.add(disp);
            }
        }
        fillInheritedFields(mdClass, listToFill);
    }

    public class BodyDataWrapper {

        public Object[] data;
        public String className;

        public BodyDataWrapper(String className, Object[] data) {
            this.className = className;
            this.data = data;
        }
    }

    private MdField findField(MdClass mdClass, String fieldName) {
        if (mdClass == null) return null;
        MdField field = mdClass.findField(fieldName);
        if (field == null) {
            String superClassString = mdClass.getPcSuperclassStr();
            if (superClassString == null) {
                return null;
            } else {
                MdClass mdClassCurrent = mdClass.getMdPackage().findClass(
                        superClassString);
                if (mdClass == null) return null;
                MdField newfield = mdClassCurrent.findField(fieldName);
                if (newfield != null && newfield.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                    return newfield;
                } else {
                    return findField(mdClassCurrent, fieldName);
                }
            }

        } else {
            return field;
        }

    }

    /**
     * Before this gets called we must first reset the reader.
     *
     * @param currentPosition position of our Caret
     * @return
     */
    public BodyDataWrapper getBodyDataWrapper(int currentPosition) {
        JdoqlToken t = null;
        try {
            while ((t = (JdoqlToken)getNextToken(true, false)) != null) {

                if (isNow(t, currentPosition)) {     // we have to return something

                    if (t.isIdentifier()) {
                        if (t.getID() == JdoqlToken.IDENTIFIER) {
                            return getClassData(getCurrentMdClass());
                        } else if (t.getID() == JdoqlToken.IDENTIFIER_COLLECTION) {
                            MdField field = findField(getCurrentMdClass(),
                                    lastTokenImage);
                            return new BodyDataWrapper("Collection",
                                    getCollection(
                                            field.getElementType().getText()));
                        } else if (t.getID() == JdoqlToken.IDENTIFIER_MAP) {
                            MdField field = findField(getCurrentMdClass(),
                                    lastTokenImage);
                            return new BodyDataWrapper("Map",
                                    getMap(field.getElementType().getText(),
                                            field.getKeyType().getText()));
                        } else if (t.getID() == JdoqlToken.IDENTIFIER_STRING) {
                            return new BodyDataWrapper("String",
                                    getAllString());
                        } else {
                            return null;
                        }

                    } else if (t.getID() == JdoqlToken.SEPARATOR_PERIOD) {
                        if (t.specialID == JdoqlToken.SPECIAL_ID_IDENTIFIER) {
                            return getClassData(getCurrentMdClass());
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_SQL) {
                            return new BodyDataWrapper("SQL Expression",
                                    getSQL());   // todo
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_COLLECTION) {
                            MdField field = findField(getCurrentMdClass(),
                                    lastTokenImage);
                            return new BodyDataWrapper("Collection",
                                    getCollection(
                                            field.getElementType().getText()));
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_MAP) {
                            MdField field = findField(getCurrentMdClass(),
                                    lastTokenImage);
                            return new BodyDataWrapper("Map",
                                    getMap(field.getElementType().getText(),
                                            field.getKeyType().getText()));
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_STRING) {
                            return new BodyDataWrapper("String",
                                    getAllString());
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_STRING_LOWER) {
                            return new BodyDataWrapper("String", getString());
                        } else if (t.specialID == JdoqlToken.SPECIAL_ID_THIS) {
                            return getClassData(getCurrentMdClass());
                        } else {
                            return null;
                        }
                    } else if (t.isError()) {
                        JdoqlToken prev = t.getPrevToken();
                        if (t.getID() == JdoqlToken.ERROR_INVALID_SEPERATOR) {
                            return null;
                        }

                        if (prev != null) {

                            if (prev.getID() == JdoqlToken.SEPARATOR_PERIOD) {
                                if (prev.specialID == JdoqlToken.SPECIAL_ID_IDENTIFIER) {
                                    return getClassData(getCurrentMdClass());
                                } else if (prev.specialID == JdoqlToken.SPECIAL_ID_SQL) {
                                    return new BodyDataWrapper(
                                            "SQL Expression", getSQL());   // todo
                                } else if (prev.specialID == JdoqlToken.SPECIAL_ID_COLLECTION) {
                                    MdField field = findField(
                                            getCurrentMdClass(),
                                            lastTokenImage);
                                    return new BodyDataWrapper("Collection",
                                            getCollection(
                                                    field.getElementType().getText()));
                                } else if (prev.specialID == JdoqlToken.SPECIAL_ID_MAP) {
                                    MdField field = findField(
                                            getCurrentMdClass(),
                                            lastTokenImage);
                                    return new BodyDataWrapper("Map",
                                            getMap(
                                                    field.getElementType().getText(),
                                                    field.getKeyType().getText()));
                                } else if (prev.specialID == JdoqlToken.SPECIAL_ID_STRING) {
                                    return new BodyDataWrapper("String",
                                            getAllString());
                                } else if (prev.specialID == JdoqlToken.SPECIAL_ID_STRING_LOWER) {
                                    return new BodyDataWrapper("String",
                                            getString());
                                } else {
                                    return null;
                                }
                            } else if (prev.isComment()) {
                                return getClassData(getCurrentMdClass());
                            } else if (prev.isSeparator()) {
                                return getClassData(getCurrentMdClass());
                            } else if (prev.isOperator()) {
                                return getClassData(getCurrentMdClass());
                            } else if (t.getID() == 3907) {
                                return getClassData(getCurrentMdClass());
                            } else {
                                return null;
                            }
                        } else {
                            return getClassData(getCurrentMdClass());
                        }
                    } else if (t.isSeparator()) {
                        return getClassData(getCurrentMdClass());
                    } else {
                        if (t.getID() == 1024) {
                            return getClassData(getCurrentMdClass());
                        } else {
                            return null;
                        }
                    }

                }
            }
            return getClassData(getCurrentMdClass());
        } catch (IOException e) {
            return getClassData(getCurrentMdClass());
        }
    }

    private Object[] getCollection(String containsType) {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] col = new Object[3];
        DisplayMethod con = new DisplayMethod("contains()", "boolean",
                containsType + " elem");
        con.setLenght();
        col[0] = con;
        DisplayMethod is = new DisplayMethod("isEmpty()", "boolean", null);
        is.setLenght();
        col[1] = is;
        DisplayMethod sql = new DisplayMethod("sql()", "void", "String exp");
        sql.setLenght();
        col[2] = sql;
        return col;
    }

    private Object[] getMap(String containsType, String containsKey) {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] col = new Object[4];
        DisplayMethod con = new DisplayMethod("contains()", "boolean",
                containsType + " value");
        con.setLenght();
        col[0] = con;
        DisplayMethod key = new DisplayMethod("containsKey()", "boolean",
                containsKey + " key");
        key.setLenght();
        col[1] = key;
        DisplayMethod is = new DisplayMethod("isEmpty()", "boolean", null);
        is.setLenght();
        col[2] = is;
        DisplayMethod sql = new DisplayMethod("sql()", "void", "String exp");
        sql.setLenght();
        col[3] = sql;

        return col;
    }

    private Object[] getAllString() {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] string = new Object[4];
        DisplayMethod end = new DisplayMethod("endsWith()", "boolean",
                "String suffix");
        end.setLenght();
        string[0] = end;
        DisplayMethod start = new DisplayMethod("startsWith()", "boolean",
                "String prefix");
        start.setLenght();
        string[1] = start;
        DisplayMethod lower = new DisplayMethod("toLowerCase()", "String",
                null);
        lower.setLenght();
        string[2] = lower;
        DisplayMethod sql = new DisplayMethod("sql()", "void", "String exp");
        sql.setLenght();
        string[3] = sql;

        return string;
    }

    private Object[] getSQL() {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] string = new Object[1];
        DisplayMethod sql = new DisplayMethod("sql()", "void", "String exp");
        sql.setLenght();
        string[0] = sql;

        return string;
    }

    private Object[] getString() {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] string = new Object[3];
        DisplayMethod end = new DisplayMethod("endsWith()", "boolean",
                "String suffix");
        end.setLenght();
        string[0] = end;
        DisplayMethod start = new DisplayMethod("startsWith()", "boolean",
                "String prefix");
        start.setLenght();
        string[1] = start;
        DisplayMethod sql = new DisplayMethod("sql()", "void", "String exp");
        sql.setLenght();
        string[2] = sql;

        return string;
    }


%}

%line
%char
%full

HexDigit=([0-9a-fA-F])
Digit=([0-9])
OctalDigit=([0-7])
TetraDigit=([0-3])
NonZeroDigit=([1-9])
Letter=([a-zA-Z_$])
BLANK=([ ])
TAB=([\t])
FF=([\f])
EscChar=([\\])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF}|{EOL})
AnyNonSeparator=([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\'])

OctEscape1=({EscChar}{OctalDigit})
OctEscape2=({EscChar}{OctalDigit}{OctalDigit})
OctEscape3=({EscChar}{TetraDigit}{OctalDigit}{OctalDigit})
OctEscape=({OctEscape1}|{OctEscape2}|{OctEscape3})

UnicodeEscape=({EscChar}[u]{HexDigit}{HexDigit}{HexDigit}{HexDigit})

Escape=({EscChar}([r]|[n]|[b]|[f]|[t]|[\\]|[\']|[\"]))
JavaLetter=({Letter}|{UnicodeEscape})
Identifier=({JavaLetter}({JavaLetter}|{Digit})*)
ErrorIdentifier=({AnyNonSeparator}+)

Comment=("//"[^\r\n]*)
TradCommentBegin=("/*")
DocCommentBegin =("/**")
NonTermStars=([^\*\/]*[\*]+[^\*\/])
TermStars=([\*]+[\/])
CommentText=((([^\*]*[\/])|{NonTermStars})*)
CommentEnd=([^\*]*{TermStars})
TradComment=({TradCommentBegin}{CommentText}{CommentEnd})
DocCommentEnd1=([^\/\*]{CommentText}{CommentEnd})
DocCommentEnd2=({NonTermStars}{CommentText}{CommentEnd})
DocComment=({DocCommentBegin}({DocCommentEnd1}|{DocCommentEnd2}|{TermStars}|[\/]))
OpenComment=({TradCommentBegin}{CommentText}([^\*]*)([\*]*))

Sign=([\+]|[\-])
LongSuffix=([l]|[L])
DecimalNum=(([0]|{NonZeroDigit}{Digit}*))
OctalNum=([0]{OctalDigit}*)
HexNum=([0]([x]|[X]){HexDigit}{HexDigit}*)
DecimalLong=({DecimalNum}{LongSuffix})
OctalLong=({OctalNum}{LongSuffix})
HexLong=({HexNum}{LongSuffix})

SignedInt=({Sign}?{Digit}+)
Expo=([e]|[E])
ExponentPart=({Expo}{SignedInt})
FloatSuffix=([f]|[F])
DoubleSuffix=([d]|[D])
FloatDouble1=({Digit}+[\.]{Digit}*{ExponentPart}?)
FloatDouble2=([\.]{Digit}+{ExponentPart}?)
FloatDouble3=({Digit}+{ExponentPart})
FloatDouble4=({Digit}+)
Double1=({FloatDouble1}{DoubleSuffix}?)
Double2=({FloatDouble2}{DoubleSuffix}?)
Double3=({FloatDouble3}{DoubleSuffix}?)
Double4=({FloatDouble4}{DoubleSuffix})
Float1=({FloatDouble1}{FloatSuffix})
Float2=({FloatDouble2}{FloatSuffix})
Float3=({FloatDouble3}{FloatSuffix})
Float4=({FloatDouble4}{FloatSuffix})
Float=({Float1}|{Float2}|{Float3}|{Float4})
Double=({Double1}|{Double2}|{Double3}|{Double4})

ZeroFloatDouble1=([0]+[\.][0]*{ExponentPart}?)
ZeroFloatDouble2=([\.][0]+{ExponentPart}?)
ZeroFloatDouble3=([0]+{ExponentPart})
ZeroFloatDouble4=([0]+)
ZeroDouble1=({ZeroFloatDouble1}{DoubleSuffix}?)
ZeroDouble2=({ZeroFloatDouble2}{DoubleSuffix}?)
ZeroDouble3=({ZeroFloatDouble3}{DoubleSuffix}?)
ZeroDouble4=({ZeroFloatDouble4}{DoubleSuffix})
ZeroFloat1=({ZeroFloatDouble1}{FloatSuffix})
ZeroFloat2=({ZeroFloatDouble2}{FloatSuffix})
ZeroFloat3=({ZeroFloatDouble3}{FloatSuffix})
ZeroFloat4=({ZeroFloatDouble4}{FloatSuffix})
ZeroFloat=({ZeroFloat1}|{ZeroFloat2}|{ZeroFloat3}|{ZeroFloat4})
ZeroDouble=({ZeroDouble1}|{ZeroDouble2}|{ZeroDouble3}|{ZeroDouble4})

ErrorFloat=({Digit}({AnyNonSeparator}|[\.])*)

AnyChrChr=([^\'\n\r\\])
UnclosedCharacter=([\']({Escape}|{OctEscape}|{UnicodeEscape}|{AnyChrChr}))
Character=({UnclosedCharacter}[\'])
MalformedUnclosedCharacter=([\']({AnyChrChr}|({EscChar}[^\n\r]))*)
MalformedCharacter=([\'][\']|{MalformedUnclosedCharacter}[\'])

AnyStrChr=([^\"\n\r\\])
UnclosedString=([\"]({Escape}|{OctEscape}|{UnicodeEscape}|{AnyStrChr})*)
String=({UnclosedString}[\"])
MalformedUnclosedString=([\"]({EscChar}|{AnyStrChr})*)
MalformedString=({MalformedUnclosedString}[\"])

%%

<YYINITIAL> "(" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_LPAREN, yytext(), yyline, yychar, yychar+1));
    bracketCount++;
    if (lastToken != null && lastToken.getID() == JdoqlToken.SEPARATOR_LPAREN){
        castCount = 2;
    } else {
        castCount++;
    }
    thisFlag = false;
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_LEFT_SEPERATOR;
    currentMdClass = mdClass;
    lastToken = t;
    return (t);
    }
<YYINITIAL> ")" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_RPAREN,yytext(),yyline,yychar,yychar+1));
    castCount --;
    bracketCount --;
    thisFlag = false;
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_RIGHT_SEPERATOR;
    currentMdClass = mdClass;
    lastToken = t;
    return (t);
}

<YYINITIAL> "[" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_LBRACKET,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.setPrevToken(lastToken);
    currentMdClass = mdClass;
    lastToken = t;
    return (t);
}
<YYINITIAL> "]" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_RBRACKET,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.setPrevToken(lastToken);
    currentMdClass = mdClass;
    lastToken = t;
    return (t);
}

<YYINITIAL> "," {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_COMMA,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "." {
                  // "."
              if (lastToken != null){

                  if (castCount == 0 && castName != null && lastToken.getID() == JdoqlToken.SEPARATOR_RPAREN) {
                      // now we have to do a cast
                      currentMdClass = (MdClass)classNames.get(castName);
                      castName = null;
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      t.specialID = JdoqlToken.SPECIAL_ID_IDENTIFIER;
                      lastToken = t;
                      return (t);
                  }


                  if (lastToken.getID() == JdoqlToken.IDENTIFIER){
                      MdField field = findField(currentMdClass,lastTokenImage);
                      if (field != null){
                          if (field.getCategory() == MDStatics.CATEGORY_REF){
                              //PC
                              currentMdClass = field.getRefClass();
                              JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                              t.setPrevToken(lastToken);
                              t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                              t.specialID = JdoqlToken.SPECIAL_ID_IDENTIFIER;
                              lastToken = t;
                              return (t);
                          } else if (field.getCategory() == MDStatics.CATEGORY_COLLECTION){
                              //Collection
                              JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                              t.specialID = JdoqlToken.SPECIAL_ID_COLLECTION;
                              t.setPrevToken(lastToken);
                              t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                              lastToken = t;
                              return (t);

                          } else if (field.getCategory() == MDStatics.CATEGORY_MAP){
                              //Map
                              JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                              t.specialID = JdoqlToken.SPECIAL_ID_MAP;
                              t.setPrevToken(lastToken);
                              t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                              lastToken = t;
                              return (t);

                          } else if (field.getField().getType().equals(String.class)){
                              //String
                              JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                              t.specialID = JdoqlToken.SPECIAL_ID_STRING;
                              t.setPrevToken(lastToken);
                              t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                              lastToken = t;
                              return (t);

                          } else {
                              //primative or something
                              JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                              t.specialID = JdoqlToken.SPECIAL_ID_SQL;
                              t.setPrevToken(lastToken);
                              t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                              lastToken = t;
                              return (t);
                          }
                      } else {
                          JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                          t.setPrevToken(lastToken);
                          t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                          lastToken = t;
                          return (t);
                      }
                  } else if (lastToken.getID() == JdoqlToken.IDENTIFIER_VAR_PC){
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      t.specialID = JdoqlToken.SPECIAL_ID_IDENTIFIER;
                      lastToken = t;
                      return (t);
                  } else if (lastToken.getID() == JdoqlToken.IDENTIFIER_VAR){
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      lastToken = t;
                      return (t);
                  } else if (lastToken.getID() == JdoqlToken.IDENTIFIER_STRING){
                      //String
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                      t.specialID = JdoqlToken.SPECIAL_ID_STRING;
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      lastToken = t;
                      return (t);
                  } else if (lastToken.getID() == JdoqlToken.RESERVED_WORD_THIS){
                      currentMdClass = mdClass;
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      t.specialID = JdoqlToken.SPECIAL_ID_THIS;
                      lastToken = t;
                      return (t);
                  } else {
                      if (lastToken.getID() == JdoqlToken.SEPARATOR_RPAREN && lastToken.getPrevToken() != null){
                          JdoqlToken right = lastToken.getPrevToken();
                          if (right.getID() == JdoqlToken.SEPARATOR_LPAREN && right.getPrevToken() != null){
                              JdoqlToken string  = right.getPrevToken();
                              if (string.getID() == JdoqlToken.IDENTIFIER_STRING && string.type == JdoqlToken.TYPE_STRING){
                                  JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                                  t.specialID = JdoqlToken.SPECIAL_ID_STRING_LOWER;
                                  t.setPrevToken(lastToken);
                                  t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                                  lastToken = t;
                                  return (t);
                              }

                          }
                      }
                      if (lastToken.getID() == JdoqlToken.SEPARATOR_RPAREN && lastToken.getPrevToken() != null) {
//                          JdoqlToken left = lastToken.getPrevToken();
//                          if (left.getID() == JdoqlToken.SEPARATOR_LPAREN && left.getPrevToken() != null) {
//                              // now we check if it is sql
//                              JdoqlToken sql = left.getPrevToken();
//                              if (sql.getContents().equals("sql")) {
//                                  JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR, yytext(), yyline,
//                                          yychar, yychar + 1));
//                                  t.setPrevToken(lastToken);
//                                  lastToken = t;
//                                  return (t);
//
//                              }
//
//                          } else if (left.isLiteral() && left.getPrevToken() != null){ // now we can check if it is the left param
//                              JdoqlToken realLeft = left.getPrevToken();
//                              if (realLeft.getID() == JdoqlToken.SEPARATOR_LPAREN && realLeft.getPrevToken() != null) {
//                                  // now we check if it is sql
//                                  JdoqlToken sql = realLeft.getPrevToken();
//                                  if (sql.getContents().equals("sql")) {
//                                      JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR, yytext(), yyline,
//                                              yychar, yychar + 1));
//                                      t.setPrevToken(lastToken);
//                                      lastToken = t;
//                                      return (t);
//
//                                  }
//                              }
//                          }

                          JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD, yytext(), yyline, yychar, yychar + 1));
                          t.specialID = JdoqlToken.SPECIAL_ID_SQL;
                          t.setPrevToken(lastToken);
                          t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                          lastToken = t;
                          return (t);
                      }

                      // this must always be last, just before the else part

                      JdoqlToken t = (new JdoqlToken(JdoqlToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                      t.setPrevToken(lastToken);
                      t.type = JdoqlToken.TYPE_DOT_SEPERATOR;
                      lastToken = t;
                      return (t);
                  }
              } else {
                  JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
                  t.setPrevToken(lastToken);
                  lastToken = t;
                  return (t);
              }
}

<YYINITIAL> ">" {

    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_GREATER_THAN,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "<" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_LESS_THAN,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "!" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_LOGICAL_NOT,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_BOOLEAN_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "~" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_BITWISE_COMPLIMENT,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "+" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_ADD,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_STRING_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "-" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_SUBTRACT,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "/" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_DIVIDE,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "*" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_MULTIPLY,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "&" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_BITWISE_AND,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_BOOLEAN_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "|" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_BITWISE_OR,yytext(),yyline,yychar,yychar+1));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_BOOLEAN_OPERATOR;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "==" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_EQUAL,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_ALL_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "<=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_LESS_THAN_OR_EQUAL,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_GREATER_THAN_OR_EQUAL,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_NUMBER_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "!=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_NOT_EQUAL,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_ALL_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "||" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_LOGICAL_OR,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_BOOLEAN_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "&&" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.OPERATOR_LOGICAL_AND,yytext(),yyline,yychar,yychar+2));
    thisFlag = false;
    t.type = JdoqlToken.TYPE_BOOLEAN_OPERATOR;
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "this" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.RESERVED_WORD_THIS, yytext(), yyline, yychar, yychar+4));
    thisFlag = true;
    t.type = JdoqlToken.TYPE_PC;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "null" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.RESERVED_WORD_NULL, yytext(), yyline, yychar, yychar+4));
    t.type = JdoqlToken.TYPE_OBJECT;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> "true" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.RESERVED_WORD_BOOLEAN, yytext(), yyline, yychar, yychar+4));
    t.type = JdoqlToken.TYPE_BOOLEAN;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "false" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.RESERVED_WORD_BOOLEAN, yytext(), yyline, yychar, yychar+5));
    t.type = JdoqlToken.TYPE_BOOLEAN;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "contains" {
    // "contains"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getCategory() == MDStatics.CATEGORY_COLLECTION){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_COLLECTION, yytext(), yyline, yychar, yychar+8));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_OBJECT;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                        return (t);
                    } else if  (field.getCategory() == MDStatics.CATEGORY_MAP){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_MAP, yytext(), yyline, yychar, yychar+8));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_OBJECT;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                       return (t);
                    }
                }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+8));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "containsKey" {
    // "containsKey"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getCategory() == MDStatics.CATEGORY_MAP){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_MAP, yytext(), yyline, yychar, yychar+11));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_OBJECT;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                       return (t);
                    }
                }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+11));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> "isEmpty" {
    // "isEmpty"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getCategory() == MDStatics.CATEGORY_COLLECTION){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_COLLECTION, yytext(), yyline, yychar, yychar+7));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_NONE;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                       return (t);
                    } else if  (field.getCategory() == MDStatics.CATEGORY_MAP){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_MAP, yytext(), yyline, yychar, yychar+7));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_NONE;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                       return (t);
                    }
                }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+7));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "endsWith" {
    // "endsWith"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getField().getType().equals(String.class)){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar+8));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_STRING;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                        return (t);
                    }
                } else if (prev.getID() == JdoqlToken.SEPARATOR_RPAREN && prev.getPrevToken() != null){
                   JdoqlToken right = prev.getPrevToken();
                   if (right.getID() == JdoqlToken.SEPARATOR_LPAREN && right.getPrevToken() != null){
                       JdoqlToken string  = right.getPrevToken();
                       if (string.getID() == JdoqlToken.IDENTIFIER_STRING && string.type == JdoqlToken.TYPE_STRING){
                           JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar+8));
                           t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_STRING;
                           t.setPrevToken(lastToken);
                           lastToken = t;
                           return (t);
                       }
                   }
               }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+8));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "startsWith" {
    // "startsWith"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getField().getType().equals(String.class)){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar+10));
                       t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_STRING;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                        return (t);
                    }
                } else if (prev.getID() == JdoqlToken.SEPARATOR_RPAREN && prev.getPrevToken() != null){
                   JdoqlToken right = prev.getPrevToken();
                   if (right.getID() == JdoqlToken.SEPARATOR_LPAREN && right.getPrevToken() != null){
                       JdoqlToken string  = right.getPrevToken();
                       if (string.getID() == JdoqlToken.IDENTIFIER_STRING && string.type == JdoqlToken.TYPE_STRING){
                           JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar+10));
                           t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_STRING;
                           t.setPrevToken(lastToken);
                           lastToken = t;
                           return (t);
                       }
                   }
               }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+10));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "toLowerCase" {
    // "toLowerCase"
    if (lastToken != null){
       if (lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD){
           JdoqlToken prev = lastToken.getPrevToken();
           if (prev != null){
               MdField field = findField(currentMdClass,prev.getContents());
               if (field != null){
                   if  (field.getField().getType().equals(String.class)){
                       JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar+11));
                       t.type = JdoqlToken.TYPE_STRING;
                       t.setPrevToken(lastToken);
                       lastToken = t;
                        return (t);
                    }
                }
            }
        }
    }
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+11));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "sql" {
              // "sql"
              if (lastToken != null && lastToken.getID() == JdoqlToken.SEPARATOR_PERIOD ) {
                  JdoqlToken prev = lastToken.getPrevToken();
                  if (prev != null && prev.isLiteral()){
                      JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar+3));
                      t.setPrevToken(lastToken);
                      lastToken = t;
                      return (t);
                  }
              }
              JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_STRING, yytext(), yyline, yychar, yychar + 3));
              t.type = JdoqlToken.TYPE_BOOLEAN_METHOD_STRING;
              t.setPrevToken(lastToken);
              lastToken = t;
              return (t);
}

<YYINITIAL> {Identifier} {
    // Identifier
    if (castCount == 2 &&  lastToken != null &&
        lastToken.getID() == JdoqlToken.SEPARATOR_LPAREN &&  classNames.containsKey(yytext())){
        castName = yytext();
        lastTokenImage = yytext();
        JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_CAST, yytext(), yyline, yychar, yychar + yytext().length()));
        t.type = JdoqlToken.TYPE_CAST;
        t.setPrevToken(lastToken);
        lastToken = t;
        return (t);
    } else {
        MdField field = findField(currentMdClass,yytext());
        if (field != null){
            lastTokenImage = yytext();
            JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
            t.type = getType(field);
            t.setPrevToken(lastToken);
            lastToken = t;
            return (t);
        } else if (mapVars.containsKey(yytext()) && currentMdClass.equals(mdClass) && !thisFlag){ // we have a declared var
            Object o = mapVars.get(yytext());
            if (o instanceof MdClass){
                currentMdClass = (MdClass)o;
                JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_VAR_PC, yytext(), yyline, yychar, yychar + yytext().length()));
                t.type = JdoqlToken.TYPE_PC;
                t.setPrevToken(lastToken);
                lastToken = t;
                return (t);
            } else {
                JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_VAR, yytext(), yyline, yychar, yychar + yytext().length()));
                t.type = getVarType(yytext());
                t.setPrevToken(lastToken);
                lastToken = t;
                return (t);
            }
        } else if (mapParams.containsKey(yytext()) && currentMdClass.equals(mdClass) && !thisFlag) { // we have a declared var
            Object o = mapParams.get(yytext());
            if (o instanceof MdClass) {
                currentMdClass = (MdClass) o;
                JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_PARAM_PC, yytext(), yyline, yychar, yychar + yytext().length()));
                t.type = JdoqlToken.TYPE_PC;
                t.setPrevToken(lastToken);
                lastToken = t;
                return (t);
            } else {
                JdoqlToken t = (new JdoqlToken(JdoqlToken.IDENTIFIER_PARAM, yytext(), yyline, yychar, yychar + yytext().length()));
                t.type = getParamType(yytext());
                t.setPrevToken(lastToken);
                lastToken = t;
                return (t);
            }
        } else {
            JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            return (t);
        }
    }
}



<YYINITIAL> {DecimalNum} {
    /* At this point, the number we found could still be too large.
     * If it is too large, we need to return an error.
     * Java has methods built in that will decode from a string
     * and throw an exception the number is too large
     */
    JdoqlToken t = null;
    try {
        /* bigger negatives are allowed than positives.  Thus
         * we have to be careful to make sure a neg sign is preserved
         */
        if (lastToken != null){
            if (lastToken.getID() == JdoqlToken.OPERATOR_SUBTRACT){
                Integer.decode('-' + yytext());
            } else {
                Integer.decode(yytext());
            }
        } else {
            Integer.decode(yytext());
        }
        t = (new JdoqlToken(JdoqlToken.LITERAL_INTEGER_DECIMAL, yytext(), yyline, yychar, yychar + yytext().length()));
    } catch (NumberFormatException e){
        t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_DECIMIAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
    }
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {OctalNum} {
    /* An Octal number cannot be too big.  After removing
     * initial zeros, It can have 11 digits, the first
     * of which must be 3 or less.
     */
    JdoqlToken t = null;
    int i;
    int length =yytext().length();
    for (i=1 ; i<length-11; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            t.type = JdoqlToken.TYPE_NUMBER;
            lastToken = t;
            return (t);
        }
    }
    if (length - i > 11){
        t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        t.type = JdoqlToken.TYPE_NUMBER;
        lastToken = t;
        return (t);
    } else if (length - i == 11){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' &&
            yytext().charAt(i) != '1' &&
            yytext().charAt(i) != '2' &&
            yytext().charAt(i) != '3'){


            t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            t.type = JdoqlToken.TYPE_NUMBER;
            lastToken = t;
            return (t);
        }
    }
    // Otherwise, it should be OK
    t = (new JdoqlToken(JdoqlToken.LITERAL_INTEGER_OCTAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {HexNum} {
    /* A Hex number cannot be too big.  After removing
     * initial zeros, It can have 8 digits
     */
    JdoqlToken t = null;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-8; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 8){
        t = (new JdoqlToken(JdoqlToken.ERROR_INTEGER_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    }
    t = (new JdoqlToken(JdoqlToken.LITERAL_INTEGER_HEXIDECIMAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {DecimalLong} {
    JdoqlToken t = null;
    try {
        if (lastToken != null){
            if (lastToken.getID() == JdoqlToken.OPERATOR_SUBTRACT){
                Long.decode('-' + yytext().substring(0,yytext().length()-1));
            } else {
                Long.decode(yytext().substring(0,yytext().length()-1));
            }
        } else {
            Long.decode(yytext().substring(0,yytext().length()-1));
        }
    } catch (NumberFormatException e){
        t = (new JdoqlToken(JdoqlToken.ERROR_LONG_DECIMIAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    }
    t = (new JdoqlToken(JdoqlToken.LITERAL_LONG_DECIMAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {OctalLong} {
    /* An Octal number cannot be too big.  After removing
     * initial zeros, It can have 23 digits, the first
     * of which must be 1 or less.  The last will be the L or l
     * at the end.
     */
    JdoqlToken t = null;
    int i;
    int length = yytext().length();
    for (i=1 ; i<length-23; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new JdoqlToken(JdoqlToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 23){
        t = (new JdoqlToken(JdoqlToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    } else if (length - i == 23){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' && yytext().charAt(i) != '1'){
            t = (new JdoqlToken(JdoqlToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    }
    // Otherwise, it should be OK
    t = (new JdoqlToken(JdoqlToken.LITERAL_LONG_OCTAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {HexLong} {
    /* A Hex long cannot be too big.  After removing
     * initial zeros, It can have 17 digits, the last of which is
     * the L or l
     */
    JdoqlToken t = null;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-17; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new JdoqlToken(JdoqlToken.ERROR_LONG_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 17){
        t = (new JdoqlToken(JdoqlToken.ERROR_LONG_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    }
    t = (new JdoqlToken(JdoqlToken.LITERAL_LONG_HEXIDECIMAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {ZeroFloat} {
    /* catch the case of a zero in parsing, so that we do not incorrectly
     * give an error that a number was rounded to zero
     */
    JdoqlToken t = (new JdoqlToken(JdoqlToken.LITERAL_FLOATING_POINT, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {ZeroDouble} {

    JdoqlToken t = (new JdoqlToken(JdoqlToken.LITERAL_DOUBLE, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {Float} {
    /* Sun s java has a few bugs here.  Their MAX_FLOAT and MIN_FLOAT do not
     * quite match the spec.  Its not far off, so we will deal.  If they fix
     * then we are fixed.  So all good.
     */
    Float f;
    JdoqlToken t = null;
    try {
        f = Float.valueOf(yytext());
        if (f.isInfinite() || f.compareTo(new Float(0f)) == 0){
            t = (new JdoqlToken(JdoqlToken.ERROR_FLOAT_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        } else {
            t = (new JdoqlToken(JdoqlToken.LITERAL_FLOATING_POINT, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    } catch (NumberFormatException e){
        t = (new JdoqlToken(JdoqlToken.ERROR_FLOAT_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    }
}
<YYINITIAL> {Double} {
    Double d;
    JdoqlToken t = null;
    try {
        d = Double.valueOf(yytext());
        if (d.isInfinite() || d.compareTo(new Double(0d)) == 0){
            t = (new JdoqlToken(JdoqlToken.ERROR_DOUBLE_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        } else {
            t = (new JdoqlToken(JdoqlToken.LITERAL_DOUBLE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = JdoqlToken.TYPE_NUMBER;
            return (t);
        }
    } catch (NumberFormatException e){

        t = (new JdoqlToken(JdoqlToken.ERROR_DOUBLE_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = JdoqlToken.TYPE_NUMBER;
        return (t);
    }
}


<YYINITIAL> {Character} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.LITERAL_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {String} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.LITERAL_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_STRING;
    lastToken = t;
    return (t);
}

<YYINITIAL> ({WhiteSpace}+) {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.WHITE_SPACE, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> {Comment} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.COMMENT_END_OF_LINE, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {DocComment} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.COMMENT_DOCUMENTATION, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {TradComment} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.COMMENT_TRADITIONAL, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> {UnclosedString} {
    /* most of these errors have to be caught down near the end of the file.
     * This way, previous expressions of the same length have precedence.
     * This is really useful for catching anything bad by just allowing it
     * to slip through the cracks.
     */
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_UNCLOSED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedUnclosedString} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_MALFORMED_UNCLOSED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedString} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_MALFORMED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {UnclosedCharacter} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_UNCLOSED_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedUnclosedCharacter} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_MALFORMED_UNCLOSED_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedCharacter} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.LITERAL_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = JdoqlToken.TYPE_STRING;
    return (t);
}
<YYINITIAL> {ErrorFloat} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_FLOAT, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = JdoqlToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {ErrorIdentifier} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {OpenComment} {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_UNCLOSED_COMMENT, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> "{" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "}" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ";" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "?" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ":" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "++" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "--" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "<<" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>>" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "+=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "-=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "*=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "/=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "&=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "|=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "^=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "%=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "<<=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>>=" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+4));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "^" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "%" {
    JdoqlToken t = (new JdoqlToken(JdoqlToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}



