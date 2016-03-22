
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
package za.co.hemtech.jdo.tools.workbench.jdoql.ordering;

import java.io.*;

import za.co.hemtech.jdo.tools.workbench.model.MdClass;
import za.co.hemtech.jdo.tools.workbench.model.MdField;
import za.co.hemtech.jdo.tools.workbench.jdoql.insight.*;
import za.co.hemtech.jdo.tools.workbench.jdoql.lexer.Lexer;
import za.co.hemtech.jdo.tools.workbench.jdoql.lexer.Token;
import za.co.hemtech.jdo.metadata.MDStatics;
import za.co.hemtech.gui.model.ObservableList;

import java.util.*;

/**
 * @keep-all
 * OrderingLexer is a java lexer.  Created with JFlex.
 * The tokens returned should comply with the JDOQL Language Specification
 * @see OrderingToken
 * @version $Id: OrderingLexer.flex,v 1.1 2005/03/08 08:42:16 david Exp $
%%

%public
%class OrderingLexer
%implements Lexer
%function getNextToken
%type Token

%{
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        private StringBuffer commentText = new StringBuffer();
    private OrderingToken lastToken;
    private String lastTokenImage;
    private String classType;
    private MdClass mdClass;
    private MdClass currentMdClass;





    /**
     * next Token method that allows you to control if whitespace and comments are
     * returned as tokens.
     */
    public Token getNextToken(boolean returnComments, boolean returnWhiteSpace)throws IOException{
        Token t = getNextToken();
        while (t != null && ((!returnWhiteSpace && t.isWhiteSpace()) || (!returnComments && t.isComment()))){
            t = getNextToken();
        }
        return (t);
    }
                   //
    private int getType(MdField field){
        Class clazz = field.getField().getType();

        if (clazz.isArray()){
            return OrderingToken.TYPE_ARRAY;
        } else if (clazz.isPrimitive()){
            if (clazz.getName().equals("boolean")){
                return OrderingToken.TYPE_BOOLEAN;
            } else {
                return OrderingToken.TYPE_NUMBER;
            }
        } else {
            if (field.getCategory() == MDStatics.CATEGORY_REF){
                return OrderingToken.TYPE_PC;
            } else if (field.getCategory() == MDStatics.CATEGORY_COLLECTION){
                return OrderingToken.TYPE_COLLECTION;
            } else if (field.getCategory() == MDStatics.CATEGORY_MAP){
                return OrderingToken.TYPE_MAP;
            } else if (clazz.equals(String.class)){
                return OrderingToken.TYPE_STRING;
            } else {
                if (clazz.equals(Boolean.class)){
                    return OrderingToken.TYPE_BOOLEAN;
                } else if (clazz.equals(Byte.class) ||
                        clazz.equals(Integer.class) ||
                        clazz.equals(Short.class) ||
                        clazz.equals(Character.class) ||
                        clazz.equals(Double.class) ||
                        clazz.equals(Float.class) ||
                        clazz.equals(Long.class) ||
                        clazz.equals(java.util.Date.class) ||
                        clazz.equals(java.math.BigDecimal.class) ||
                        clazz.equals(java.math.BigInteger.class)){
                    return OrderingToken.TYPE_NUMBER;

                } else {
                    return OrderingToken.TYPE_OBJECT;
                }
            }
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
            if (args.length > 0){
                File f = new File(args[0]);
                if (f.exists()){
                    if (f.canRead()){
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
            OrderingLexer shredder = new OrderingLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != OrderingToken.WHITE_SPACE){
                    System.out.println(t);
                }
            }
        } catch (IOException e){
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
     * @param reader The new input.
     * @param yyline The line number of the first token.
     * @param yychar The position (relative to the start of the stream) of the first token.
     * @param yycolumn The position (relative to the line) of the first token.
     * @throws IOException if an IOExecption occurs while switching readers.
     */
    public void reset(java.io.Reader reader, int yyline, int yychar, int yycolumn) throws IOException{
        yyreset(reader);
        this.yyline = yyline;
        this.yychar = yychar;
        this.yycolumn = yycolumn;
        currentMdClass = mdClass;
    }

    public OrderingLexer() {}

    public OrderingLexer(String s) {
        this(new  StringReader(s));
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public MdClass getCurrentMdClass() {
	    if (currentMdClass == null){
			return mdClass;
	    }
        return currentMdClass;
    }

    public void setMdClass(MdClass mdClass) {
        this.mdClass = mdClass;
        this.currentMdClass = mdClass;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public void setReader(java.io.Reader in){
        this.yy_reader = in;
    }


    private boolean isNow(OrderingToken t,int currentPosition){
        if (((t.getCharBegin() ) <= currentPosition
                && (t.getCharEnd() ) >= currentPosition)){
            return true;
        } else {
            return false;
        }
    }

    public BodyDataWrapper getClassData(MdClass mdClassCurrent){
        FieldDisplay.BIGGEST_LENGHT = 0;
        HashSet dataSet = new HashSet();


        if (mdClassCurrent == null) return null;
        ObservableList list = mdClassCurrent.getFieldList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MdField f = (MdField) iter.next();
            if (f.getField() != null &&
                    f.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT &&
                    f.getCategory() != MDStatics.CATEGORY_COLLECTION &&
                    f.getCategory() != MDStatics.CATEGORY_MAP &&
                    f.getCategory() != MDStatics.CATEGORY_ARRAY){
                DisplayField disp = new DisplayField(f.getField().getName(),getShortName(f.getField().getType().getName()),null);
                disp.setLenght();
                dataSet.add(disp);
            }
        }



        HashSet setToFill = new HashSet();
        fillInheritedFields(mdClassCurrent,setToFill);
        dataSet.addAll(setToFill);

        ArrayList returnList = new ArrayList(dataSet);
        if (returnList.isEmpty()){
            return new BodyDataWrapper("No ordering fields found",new  Object[0]);
        }

        Collections.sort(returnList);
        return new BodyDataWrapper(getShortName(mdClassCurrent.getName()),returnList.toArray());
    }

    private String getShortName(String longName){
        if (longName.lastIndexOf('.') > 0){
            return longName.substring(longName.lastIndexOf('.')+1,longName.length());
        } else {
            return longName;
        }
    }

    private void fillInheritedFields(MdClass mdClassCurrent, HashSet listToFill){
        String superClassString = mdClassCurrent.getPcSuperclassStr();
        if (superClassString == null)return;
        MdClass mdClass = mdClassCurrent.getMdPackage().findClass(superClassString);
        if (mdClass == null)return;
        ObservableList list = (ObservableList)mdClass.getFieldList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MdField f = (MdField) iter.next();
            if (f.getField() != null &&
                    f.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT &&
                    f.getCategory() != MDStatics.CATEGORY_COLLECTION &&
                    f.getCategory() != MDStatics.CATEGORY_MAP &&
                    f.getCategory() != MDStatics.CATEGORY_ARRAY) {
                DisplayInheritedField disp = new DisplayInheritedField(f.getField().getName(),getShortName(f.getField().getType().getName()),null);
                disp.setLenght();
                listToFill.add(disp);
            }
        }
        fillInheritedFields(mdClass,listToFill);
    }

    public class BodyDataWrapper {
        public Object[] data;
        public String className;

        public BodyDataWrapper(String className, Object[] data) {
            this.className = className;
            this.data = data;
        }
    }

    private MdField findField(MdClass mdClass ,String fieldName){
        if (mdClass == null) return null;
        MdField field = mdClass.findField(fieldName);
        if (field == null){
            String superClassString = mdClass.getPcSuperclassStr();
            if (superClassString == null){
                return null;
            } else {
                MdClass mdClassCurrent = mdClass.getMdPackage().findClass(superClassString);
                if (mdClass == null)return null;
                MdField newfield = mdClassCurrent.findField(fieldName);
                if (newfield != null &&
                        newfield.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT &&
                        newfield.getCategory() != MDStatics.CATEGORY_COLLECTION &&
                        newfield.getCategory() != MDStatics.CATEGORY_MAP &&
                        newfield.getCategory() != MDStatics.CATEGORY_ARRAY) {
                    return newfield;
                } else {
                    return findField(mdClassCurrent,fieldName);
                }
            }

        } else {
            return field;
        }

    }

    /**
     * Before this gets called we must first reset the reader.
     * @param currentPosition position of our Caret
     * @return
     */
    public BodyDataWrapper getBodyDataWrapper(int currentPosition){
        OrderingToken t = null;
        boolean isWhiteSpace = false;
        try {
            while ((t = (OrderingToken)getNextToken(false,true)) != null) {

                if (isNow(t,currentPosition)){     // we have to return something
                    isWhiteSpace = false;


                    if (getCurrentMdClass() == null && classType != null){
                        if (t.getID() == OrderingToken.RESERVED_WORD_THIS){
                            return new BodyDataWrapper(classType, getThis());
                        }
                        if (t.isWhiteSpace()) {
                            t = t.getPrevToken();
                            if (t.getID() == OrderingToken.RESERVED_WORD_THIS){
                                return new BodyDataWrapper("Ordering", getReserved());
                            }
                        }
                    }


                    if (t.isWhiteSpace()) {
                        t = t.getPrevToken();
                        isWhiteSpace = true;
                        if (t == null) {
                            return getClassData(getCurrentMdClass());
                        } else if (t.isWhiteSpace()){
                            return getClassData(getCurrentMdClass());
                        }
                    }


                    if (t.isIdentifier()){
                        if (t.getID() == OrderingToken.IDENTIFIER &&
                                t.type == OrderingToken.TYPE_PC &&
                                !isWhiteSpace){
                            return getClassData(getCurrentMdClass());
                        } else if (t.getID() == OrderingToken.IDENTIFIER &&
                                t.type != OrderingToken.TYPE_PC
                                && isWhiteSpace){
                            return new BodyDataWrapper("Ordering", getReserved());
                        } else if (t.getID() == OrderingToken.IDENTIFIER &&
                                !isWhiteSpace) {
                            return getClassData(getCurrentMdClass());
                        } else {
                            return null;
                        }

                    } else if (t.getID() == OrderingToken.SEPARATOR_PERIOD){
                        if (t.specialID == OrderingToken.SPECIAL_ID_IDENTIFIER){
                            return getClassData(getCurrentMdClass());
                        } else {
                            return null;
                        }
                    } else if (t.getID() == OrderingToken.SEPARATOR_COMMA) {
                        return getClassData(mdClass);
                    } else if (t.isReservedWord()) {
                        if (!isWhiteSpace){
                            return new BodyDataWrapper("Ordering", getReserved());
                        } else {
                            return null;
                        }
                    } else if (t.isError()){
                        OrderingToken prev = t.getPrevToken();

                        if (prev != null){


                            if (prev.getID() == OrderingToken.SEPARATOR_PERIOD){
                                if (prev.specialID == OrderingToken.SPECIAL_ID_IDENTIFIER){
                                    return getClassData(getCurrentMdClass());
                                } else {
                                    return null;
                                }
                            } else if (prev.isComment()){
                                return getClassData(getCurrentMdClass());
                            } else if (prev.isSeparator()){
                                return getClassData(getCurrentMdClass());
                            } else if (prev.isOperator()){
                                return getClassData(getCurrentMdClass());
                            } else if (t.getID() == 3907){
                                return getClassData(getCurrentMdClass());
                            } else {

                                return null;
                            }
                        } else {

                            return getClassData(getCurrentMdClass());
                        }
                    } else {
                        if (t.getID() == 1024){
                            return getClassData(getCurrentMdClass());
                        } else {
                            return null;
                        }
                    }

                }
            }
            if (getCurrentMdClass() == null && classType != null){
                return new BodyDataWrapper(classType, getThis());
            } else {
                return getClassData(getCurrentMdClass());
            }
        } catch (IOException e) {
            return getClassData(getCurrentMdClass());
        }
    }


    private Object[] getReserved() {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] string = new Object[2];
        DisplayReserved end = new DisplayReserved("ascending", "", "");
        end.setLenght();
        string[0] = end;
        DisplayReserved start = new DisplayReserved("descending", "", "");
        start.setLenght();
        string[1] = start;

        return string;
    }

    private Object[] getThis() {
        FieldDisplay.BIGGEST_LENGHT = 0;
        Object[] string = new Object[1];
        DisplayReserved end = new DisplayReserved("this", "", "");
        end.setLenght();
        string[0] = end;
        return string;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////





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

<YYINITIAL> "," {
    OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_COMMA,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "." {
    if (lastToken != null){
        if (lastToken.getID() == OrderingToken.IDENTIFIER){
            MdField field = findField(currentMdClass,lastTokenImage);
            if (field != null){
                if (field.getCategory() == MDStatics.CATEGORY_REF){
                    //PC
                    currentMdClass = field.getRefClass();
                    OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                    t.setPrevToken(lastToken);
                    t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                    t.specialID = OrderingToken.SPECIAL_ID_IDENTIFIER;
                    lastToken = t;
                    return (t);
                } else if (field.getCategory() == MDStatics.CATEGORY_COLLECTION){
                    //Collection
                    OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                    t.specialID = OrderingToken.SPECIAL_ID_COLLECTION;
                    t.setPrevToken(lastToken);
                    t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                    lastToken = t;
                    return (t);

                } else if (field.getCategory() == MDStatics.CATEGORY_MAP){
                    //Map
                    OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                    t.specialID = OrderingToken.SPECIAL_ID_MAP;
                    t.setPrevToken(lastToken);
                    t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                    lastToken = t;
                    return (t);

                } else if (field.getField().getType().equals(String.class)){
                    //String
                    OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                    t.specialID = OrderingToken.SPECIAL_ID_STRING;
                    t.setPrevToken(lastToken);
                    t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                    lastToken = t;
                    return (t);

                } else {
                    //Object or something

                    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
                    t.setPrevToken(lastToken);
                    t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                    lastToken = t;
                    return (t);
                }
            } else {
                OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                t.setPrevToken(lastToken);
                t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                lastToken = t;
                return (t);
            }
        } else if (lastToken.getID() == OrderingToken.IDENTIFIER_VAR_PC){
            OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_DOT_SEPERATOR;
            t.specialID = OrderingToken.SPECIAL_ID_IDENTIFIER;
            lastToken = t;
            return (t);
        } else if (lastToken.getID() == OrderingToken.IDENTIFIER_VAR){
            OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_DOT_SEPERATOR;
            lastToken = t;
            return (t);
        } else if (lastToken.getID() == OrderingToken.IDENTIFIER_STRING){
            //String
            OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
            t.specialID = OrderingToken.SPECIAL_ID_STRING;
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_DOT_SEPERATOR;
            lastToken = t;
            return (t);
        } else {
            if (lastToken.getID() == OrderingToken.SEPARATOR_RPAREN && lastToken.getPrevToken() != null){
                    OrderingToken right = lastToken.getPrevToken();
                    if (right.getID() == OrderingToken.SEPARATOR_LPAREN && right.getPrevToken() != null){
                        OrderingToken string  = right.getPrevToken();
                        if (string.getID() == OrderingToken.IDENTIFIER_STRING && string.type == OrderingToken.TYPE_STRING){
                            OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
                            t.specialID = OrderingToken.SPECIAL_ID_STRING_LOWER;
                            t.setPrevToken(lastToken);
                            t.type = OrderingToken.TYPE_DOT_SEPERATOR;
                            lastToken = t;
                            return (t);
                        }
                    }
                }
            OrderingToken t = (new OrderingToken(OrderingToken.SEPARATOR_PERIOD,yytext(),yyline,yychar,yychar+1));
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_DOT_SEPERATOR;
            lastToken = t;
            return (t);
        }
    } else {
        OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
        t.setPrevToken(lastToken);
        lastToken = t;
        return (t);
    }
}

<YYINITIAL> "ascending" {
    OrderingToken t = (new OrderingToken(OrderingToken.RESERVED_WORD, yytext(), yyline, yychar, yychar+9));
    t.type = OrderingToken.TYPE_BOOLEAN;
    t.setPrevToken(lastToken);
    lastToken = t;
    currentMdClass = mdClass;
    return (t);
}
<YYINITIAL> "descending" {
    OrderingToken t = (new OrderingToken(OrderingToken.RESERVED_WORD, yytext(), yyline, yychar, yychar+10));
    t.type = OrderingToken.TYPE_BOOLEAN;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "this" {
    OrderingToken t = (new OrderingToken(OrderingToken.RESERVED_WORD_THIS, yytext(), yyline, yychar, yychar+4));
    t.type = OrderingToken.RESERVED_WORD_THIS;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> {Identifier} {
    MdField field = findField(currentMdClass,yytext());
    if (field != null){
        lastTokenImage = yytext();
        OrderingToken t = (new OrderingToken(OrderingToken.IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
        t.type = getType(field);
        t.setPrevToken(lastToken);
        lastToken = t;
        return (t);
    } else {
        OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        return (t);
    }
}





<YYINITIAL> {DecimalNum} {
    /* At this point, the number we found could still be too large.
     * If it is too large, we need to return an error.
     * Java has methods built in that will decode from a string
     * and throw an exception the number is too large
     */
    OrderingToken t = null;
    try {
        /* bigger negatives are allowed than positives.  Thus
         * we have to be careful to make sure a neg sign is preserved
         */
        if (lastToken != null){
            if (lastToken.getID() == OrderingToken.OPERATOR_SUBTRACT){
                Integer.decode('-' + yytext());
            } else {
                Integer.decode(yytext());
            }
        } else {
            Integer.decode(yytext());
        }
        t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    } catch (NumberFormatException e){
        t = (new OrderingToken(OrderingToken.ERROR_INTEGER_DECIMIAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
    }
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {OctalNum} {
    /* An Octal number cannot be too big.  After removing
     * initial zeros, It can have 11 digits, the first
     * of which must be 3 or less.
     */
    OrderingToken t = null;
    int i;
    int length =yytext().length();
    for (i=1 ; i<length-11; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new OrderingToken(OrderingToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_NUMBER;
            lastToken = t;
            return (t);
        }
    }
    if (length - i > 11){
        t = (new OrderingToken(OrderingToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        t.type = OrderingToken.TYPE_NUMBER;
        lastToken = t;
        return (t);
    } else if (length - i == 11){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' &&
            yytext().charAt(i) != '1' &&
            yytext().charAt(i) != '2' &&
            yytext().charAt(i) != '3'){


            t = (new OrderingToken(OrderingToken.ERROR_INTEGER_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            t.type = OrderingToken.TYPE_NUMBER;
            lastToken = t;
            return (t);
        }
    }
    // Otherwise, it should be OK
    t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {HexNum} {
    /* A Hex number cannot be too big.  After removing
     * initial zeros, It can have 8 digits
     */
    OrderingToken t = null;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-8; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new OrderingToken(OrderingToken.ERROR_INTEGER_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 8){
        t = (new OrderingToken(OrderingToken.ERROR_INTEGER_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    }
    t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {DecimalLong} {
    OrderingToken t = null;
    try {
        if (lastToken != null){
            if (lastToken.getID() == OrderingToken.OPERATOR_SUBTRACT){
                Long.decode('-' + yytext().substring(0,yytext().length()-1));
            } else {
                Long.decode(yytext().substring(0,yytext().length()-1));
            }
        } else {
            Long.decode(yytext().substring(0,yytext().length()-1));
        }
    } catch (NumberFormatException e){
        t = (new OrderingToken(OrderingToken.ERROR_LONG_DECIMIAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    }
    t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {OctalLong} {
    /* An Octal number cannot be too big.  After removing
     * initial zeros, It can have 23 digits, the first
     * of which must be 1 or less.  The last will be the L or l
     * at the end.
     */
    OrderingToken t = null;
    int i;
    int length = yytext().length();
    for (i=1 ; i<length-23; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new OrderingToken(OrderingToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 23){
        t = (new OrderingToken(OrderingToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    } else if (length - i == 23){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' && yytext().charAt(i) != '1'){
            t = (new OrderingToken(OrderingToken.ERROR_LONG_OCTAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    }
    // Otherwise, it should be OK
    t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {HexLong} {
    /* A Hex long cannot be too big.  After removing
     * initial zeros, It can have 17 digits, the last of which is
     * the L or l
     */
    OrderingToken t = null;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-17; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){
            t = (new OrderingToken(OrderingToken.ERROR_LONG_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    }
    if (length - i > 17){
        t = (new OrderingToken(OrderingToken.ERROR_LONG_HEXIDECIMAL_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    }
    t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {ZeroFloat} {
    /* catch the case of a zero in parsing, so that we do not incorrectly
     * give an error that a number was rounded to zero
     */
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {ZeroDouble} {

    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {Float} {
    /* Sun s java has a few bugs here.  Their MAX_FLOAT and MIN_FLOAT do not
     * quite match the spec.  Its not far off, so we will deal.  If they fix
     * then we are fixed.  So all good.
     */
    Float f;
    OrderingToken t = null;
    try {
        f = Float.valueOf(yytext());
        if (f.isInfinite() || f.compareTo(new Float(0f)) == 0){
            t = (new OrderingToken(OrderingToken.ERROR_FLOAT_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        } else {
            t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    } catch (NumberFormatException e){
        t = (new OrderingToken(OrderingToken.ERROR_FLOAT_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    }
}
<YYINITIAL> {Double} {
    Double d;
    OrderingToken t = null;
    try {
        d = Double.valueOf(yytext());
        if (d.isInfinite() || d.compareTo(new Double(0d)) == 0){
            t = (new OrderingToken(OrderingToken.ERROR_DOUBLE_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        } else {
            t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
            t.setPrevToken(lastToken);
            lastToken = t;
            t.type = OrderingToken.TYPE_NUMBER;
            return (t);
        }
    } catch (NumberFormatException e){

        t = (new OrderingToken(OrderingToken.ERROR_DOUBLE_SIZE, yytext(), yyline, yychar, yychar + yytext().length()));
        t.setPrevToken(lastToken);
        lastToken = t;
        t.type = OrderingToken.TYPE_NUMBER;
        return (t);
    }
}


<YYINITIAL> {Character} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {String} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_STRING;
    lastToken = t;
    return (t);
}

<YYINITIAL> ({WhiteSpace}+) {
    OrderingToken t = (new OrderingToken(OrderingToken.WHITE_SPACE, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> {Comment} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {DocComment} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {TradComment} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR, yytext(), yyline, yychar, yychar + yytext().length()));
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
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_UNCLOSED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedUnclosedString} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_MALFORMED_UNCLOSED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedString} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_MALFORMED_STRING, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_STRING;
    lastToken = t;
    return (t);
}
<YYINITIAL> {UnclosedCharacter} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_UNCLOSED_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedUnclosedCharacter} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_MALFORMED_UNCLOSED_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {MalformedCharacter} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_MALFORMED_CHARACTER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    t.type = OrderingToken.TYPE_NUMBER;
    return (t);
}
<YYINITIAL> {ErrorFloat} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_FLOAT, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    t.type = OrderingToken.TYPE_NUMBER;
    lastToken = t;
    return (t);
}
<YYINITIAL> {ErrorIdentifier} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_IDENTIFIER, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> {OpenComment} {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_UNCLOSED_COMMENT, yytext(), yyline, yychar, yychar + yytext().length()));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}

<YYINITIAL> "(" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ")" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));

    return (t);
}

<YYINITIAL> "{" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "}" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));

    return (t);
}
<YYINITIAL> ";" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_SEPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "+" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "-" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "?" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ":" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "++" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "--" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "<<" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>>" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "+=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "-=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "*=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "/=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "&=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "|=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "^=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "%=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+2));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "<<=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+3));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> ">>>=" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+4));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "^" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "%" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "*" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "/" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}
<YYINITIAL> "&" {
    OrderingToken t = (new OrderingToken(OrderingToken.ERROR_INVALID_OPERATOR,yytext(),yyline,yychar,yychar+1));
    currentMdClass = mdClass;
    t.setPrevToken(lastToken);
    lastToken = t;
    return (t);
}





