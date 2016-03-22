
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
package com.versant.core.vds;

import com.versant.core.jdo.query.*;

import java.util.*;
import com.versant.core.metadata.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.odbms.net.Constants;
import com.versant.odbms.query.*;

/**
 * This is the common interface for a visitor.
 * Subclasses can apply different behavior for different nodes.
 */
public class Converter extends NodeVisitorAdapter {

    HashSet stringParams;
    HashSet stringFields;
    HashMap varNodeMap;
    ModelMetaData _jmd;
    ClassMetaData _cmd;
    ParamNode[] _params;
    HashSet extraClasses;
    private QueryParser qParser;
    private NamingPolicy _namingPolicy;
    static final String ELEMENTS_POSTFIX = ".elements";
    static final String KEYS_POSTFIX = ".keys";
    static final String VALUES_POSTFIX = ".values";
    public Converter(ClassMetaData cmd, QueryParser qparser, ParamNode[] params, NamingPolicy namingPolicy) {
        this._cmd = cmd;
        this._jmd = cmd.jmd;
        this._namingPolicy = namingPolicy;
        this.qParser = qparser;
        varNodeMap = new HashMap();
        stringParams = new HashSet();
        stringFields = new HashSet();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if ("String".equals(params[i].getType())) {
                    stringParams.add(params[i].getIdentifier());
                }
            }
        }
        this._params = params;
        extraClasses = new HashSet();
    }

    public Object visitAddNode(AddNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length >= 2,
                    "results.length is not  >= 2");
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
            Debug.assertInternal(results[1] instanceof SubExpression,
                    "results[1] is not instanceof SubExpression");
        }


        SubExpression se1 = (SubExpression)results[0];
        SubExpression se2 = null;

        SubExpression sexpr = null;
        for (int i = 0; i < results.length - 1; i++) {
            se2 = (SubExpression)results[i + 1];
            if (node.ops[i] == AddNode.OP_PLUS) {

                if (se1.getType() == SubExpression.LITERAL_TYPE && se1.getLiteralTypeCode() == Constants.QUERY_LITERAL_TYPE_STRING
                        || se2.getType() == SubExpression.LITERAL_TYPE && se2.getLiteralTypeCode() == Constants.QUERY_LITERAL_TYPE_STRING) {
                    sexpr = se1.concat(se2);
                } else if (se1.getType() == SubExpression.PARAMETER_TYPE && stringParams.contains(se1.toString())
                        || se2.getType() == SubExpression.PARAMETER_TYPE && stringParams.contains(se2.toString())) {
                    sexpr = se1.concat(se2);
                } else if (se1.getType() == SubExpression.FIELD_TYPE && stringFields.contains(se1.toString()) ||
                        se2.getType() == SubExpression.FIELD_TYPE && stringFields.contains(se2.toString())) {
                    sexpr = se1.concat(se2);
                } else {
                    sexpr = se1.add(se2);
                }
            } else {
                sexpr = se1.subtract(se2);
            }
            se1 = sexpr;
        }

        return sexpr;
    }

    public Object visitMultiplyNode(MultiplyNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length >= 2,
                    "results.length is not  >= 2");
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
        }

        SubExpression se1 = (SubExpression)results[0];
        SubExpression se2 = null;

        for (int i = 0; i < results.length - 1; i++) {
            if (Debug.DEBUG) {
                Debug.assertInternal(results[i + 1] instanceof SubExpression,
                        "results["+(i + 1)+"] is not instanceof SubExpression");

            }
            se2 = (SubExpression)results[i + 1];
            if (node.ops[i] == MultiplyNode.OP_TIMES)
                se1 = se1.multiply(se2);
            else
                se1 = se1.divide(se2);
        }
        return se1;
    }

    public Object visitLiteralNode(LiteralNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 0,
                    "results.length != 0");

        }

        SubExpression sexpr = null;

        switch (node.type) {
            case LiteralNode.TYPE_OTHER:
                if (Debug.DEBUG) {
                    Debug.assertInternal(false,
                            "LiteralNode is of TYPE_OTHER");

                }
                break;
            case LiteralNode.TYPE_STRING: {
            	String value;
            		
            	if (node.parent instanceof MethodNode &&
            			(((MethodNode)(node.parent)).getMethod() == MethodNode.STARTS_WITH || 
               			 ((MethodNode)(node.parent)).getMethod() == MethodNode.ENDS_WITH)) {
            		value = processPattern(node.value);
            	}
            	else {
            		value = node.value;
            	}
                sexpr = new SubExpression(value); break;
            }
            case LiteralNode.TYPE_NULL: sexpr = new SubExpression((Object)null); break;
            case LiteralNode.TYPE_BOOLEAN: {
                if (node.parent instanceof EqualNode || node.parent instanceof NotEqualNode)
                    sexpr = new SubExpression(new Boolean(node.value).booleanValue());
                else
                    return convertToBooleanTree(node);
                break;
            }
            case LiteralNode.TYPE_CHAR: sexpr = new SubExpression(node.value.charAt(0)); break;
            case LiteralNode.TYPE_DOUBLE: sexpr = new SubExpression(new Double(node.value).doubleValue()); break;
            case LiteralNode.TYPE_LONG:  {
                String actualString = node.value;
                if (actualString.endsWith("L") || actualString.endsWith("l"))
                    actualString = actualString.substring(0, actualString.length() - 1);
                sexpr = new SubExpression(new Long(actualString).longValue()); break;
            }
            default:
                if (Debug.DEBUG) {
                    Debug.assertInternal(false,
                            "Unknown type");

                }
        }

        return sexpr;
    }


    public Object visitNotEqualNode(NotEqualNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 2,
                    "results.length is not == 2");

        }
        if (! (results[0] instanceof SubExpression)
                || !(results[1] instanceof SubExpression)){
            return expandTree(results[0], results[1], false);
        }
        if (Debug.DEBUG) {
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
            Debug.assertInternal(results[1] instanceof SubExpression,
                    "results[1] is not instanceof SubExpression");
        }
        return new Predicate((SubExpression)results[0],
                Predicate.NOT_EQUALS,
                (SubExpression)results[1]);
    }

    public Object visitEqualNode(EqualNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 2,
                    "results.length is not == 2");

        }
        if (! (results[0] instanceof SubExpression) ||
                ! (results[1] instanceof SubExpression))
            return expandTree(results[0], results[1], true);
        if (Debug.DEBUG) {
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
            Debug.assertInternal(results[1] instanceof SubExpression,
                    "results[1] is not instanceof SubExpression");
        }

        return new Predicate((SubExpression)results[0],
                Predicate.EQUALS,
                (SubExpression)results[1]);
    }

    public Object visitCompareOpNode(CompareOpNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 2,
                    "results.length is not == 2");
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
            Debug.assertInternal(results[1] instanceof SubExpression,
                    "results[1] is not instanceof SubExpression");
        }

        SubExpression se1 = (SubExpression)results[0];
        SubExpression se2 = (SubExpression)results[1];

        if (node.op == CompareOpNode.GT) {
            return new Predicate(se1, Predicate.GREATER_THAN, se2);
        } else if (node.op == CompareOpNode.LT) {
            return new Predicate(se1, Predicate.LESS_THAN, se2);
        } else if (node.op == CompareOpNode.GE) {
            return new Predicate(se1, Predicate.GREATER_THAN_OR_EQUALS, se2);
        } else if (node.op == CompareOpNode.LE) {
            return new Predicate(se1, Predicate.LESS_THAN_EQUALS, se2);
        }

        if (Debug.DEBUG) {
            Debug.assertInternal(false,
                    "Illegal predicate ");
        }
        return null;
    }

    public Object visitUnaryNode(UnaryNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 1,
                    "results.length is not == 1");
        }
        if (node instanceof CastNode)
            return results[0];

        if (results[0] instanceof Predicate) {
            return new Expression((Predicate)results[0]);
        } else if (results[0] instanceof Expression) {
            return (Expression)results[0];
        } else if (node.childList instanceof MethodNode &&
                ((MethodNode)node.childList).getMethod() == MethodNode.CONTAINS) {
            throw BindingSupportImpl.getInstance().unsupportedOperation (
                    "unbound variable is not supported");
        } else if (isBooleanNode(node.childList)) {
            return new Expression(new Predicate(new SubExpression("true"),
                    Predicate.EQUALS,
                    new SubExpression("true")));
        }

        if (Debug.DEBUG) {
            Debug.assertInternal(false,
                    results[0].getClass().getName() + ";" +
                    node.childList.getClass().getName());
        }
        return null;
    }

    public Object visitUnaryOpNode(UnaryOpNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 1,
                    "results.length is not == 1");
        }

        if (node.op == UnaryOpNode.OP_MINUS) {
            if (Debug.DEBUG) {
                Debug.assertInternal(results[0] instanceof SubExpression,
                        "results[0] is not a instanceof SubExpression");
            }
            return ((SubExpression)results[0]).minus();
        } else if (node.op == UnaryOpNode.OP_PLUS) {
            if (Debug.DEBUG) {
                Debug.assertInternal(results[0] instanceof SubExpression,
                        "results[0] is not a instanceof SubExpression");
            }
            return ((SubExpression)results[0]).plus();
        } else if (node.op == UnaryOpNode.OP_TILDE) {
            if (Debug.DEBUG) {
                Debug.assertInternal(results[0] instanceof SubExpression,
                        "results[0] is not a instanceof SubExpression");
            }
            return ((SubExpression)results[0]).compliment();
        } else if (node.op == UnaryOpNode.OP_BANG) {
            if (Debug.DEBUG) {
                Debug.assertInternal(results[0] instanceof Predicate ||
                        results[0] instanceof Expression ||
                        node.childList instanceof LiteralNode ||
                        node.childList instanceof FieldNode,
                        "Wrong node");
            }
            if (results[0] instanceof Predicate)
                return new Expression((Predicate)results[0]).negate();
            if (results[0] instanceof Expression)
                return ((Expression)results[0]).negate();
            if (results[0] instanceof SubExpression && isBooleanNode(node.childList))
                return inverseBoolean(node.childList);
            if (results[0] instanceof SubExpression &&
                    ((SubExpression)results[0]).getType() == SubExpression.FIELD_TYPE) {
                return new Expression(new Predicate((SubExpression)results[0], Predicate.EQUALS, new SubExpression(new Boolean(false))));
            }
        }

        if (Debug.DEBUG) {
            Debug.assertInternal(false,
                    "Unknown node");
        }
        return null;
    }

    public Object visitParamNode(ParamNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 0,
                    "results.length is not == 0");
        }
        
        SubExpression s = new SubExpression(
        		new Parameter(node.getIdentifier()));
        return s;
    }

    public Object visitFieldNode(FieldNode node, Object[] results) {
        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 0,
                    "results.length is not == 0");
        }

        if (node.parent instanceof FieldNavNode) {
            return node.lexeme;
        }
        
        FieldMetaData field = _cmd.getFieldMetaData(node.lexeme);
        if (field == null) {
        	throw BindingSupportImpl.getInstance()
				.unsupportedOperation("Field not found: " + node.lexeme);
        }

        if (field.typeMetaData != null) {
            extraClasses.add(_cmd.getFieldMetaData(node.lexeme).type);
        }

        if (("java.lang.String").equals(field.type.getName()))
            stringFields.add(node.lexeme);

        if (isEmbeddedCollection(node.lexeme))
            return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + ELEMENTS_POSTFIX));

        if (isEmbeddedMap(node.lexeme)) {
        	if (node.parent instanceof MethodNode) {
        		if (((MethodNode)node.parent).getMethod() == MethodNode.CONTAINS) { 
        			return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + VALUES_POSTFIX));
        		} else if (((MethodNode)node.parent).getMethod() == MethodNode.CONTAINS_KEY) { 
        			return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + KEYS_POSTFIX));
        		}
        	}
        	throw BindingSupportImpl.getInstance().unsupportedOperation (
                "Query: " + node.parent + " on Map is not supported");
        }

        if (isCollection(node.lexeme))
        	return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + ELEMENTS_POSTFIX));
        if (isMap(node.lexeme)) {
        	if (node.parent instanceof MethodNode) {
        		if (((MethodNode)node.parent).getMethod() == MethodNode.CONTAINS) { 
        			return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + VALUES_POSTFIX));
        		} else if (((MethodNode)node.parent).getMethod() == MethodNode.CONTAINS_KEY) { 
        			return new SubExpression(new com.versant.odbms.query.Field(node.lexeme + KEYS_POSTFIX));
        		}
        	}
        	throw BindingSupportImpl.getInstance().unsupportedOperation (
                "Query: " + node.parent + " on Map is not supported");
        }

        return new SubExpression(new com.versant.odbms.query.Field(node.lexeme));
    }

    public Object visitFieldNavNode(FieldNavNode node, Object[] results) {

        if (node instanceof ReservedFieldNode) {
        	return visitReservedFieldNode((ReservedFieldNode)node, results);
        }

        if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 1,
                    "results.length is not == 1");
            Debug.assertInternal(results[0] instanceof String,
                    "results[0] is not a instanceof String");
        }

		String castTypeName = null;
		ClassMetaData castTypeCmd = null;

		String lexeme = node.lexeme;
		
		if (node.cast != null) {
			castTypeCmd = qParser.resolveCastType(node.cast)[0];
			castTypeName = _namingPolicy.mapClassName(castTypeCmd);
			lexeme = "(" + castTypeName + ")" + node.lexeme;
		}

		if (node.parent instanceof FieldNavNode) {
			return lexeme + "." + (String) results[0];
		}

		Variable var;
		String fname;

		if (node.var != null) {
			var = (Variable) varNodeMap.get(node.var.getIdentifier());
			fname = (String) results[0];
		} else {
			var = null;
			fname = lexeme + "." + (String) results[0];
		}

		ClassMetaData cmd;
		if (var != null) {
			Class varCls = qParser.resolveVarType(var.getType());
			cmd = _jmd.getClassMetaData(varCls);
		} else {
			cmd = _cmd;
		}

		FieldMetaData fmd = processField(cmd, node);

		if (node.parent instanceof MethodNode) {

			MethodNode methNode = (MethodNode) node.parent;

			if (fmd.category == MDStatics.CATEGORY_COLLECTION) {
				fname = fname + ELEMENTS_POSTFIX;
			} else if (fmd.category == MDStatics.CATEGORY_MAP) {
				if (methNode.getMethod() == MethodNode.CONTAINS_KEY) {
					fname = fname + KEYS_POSTFIX;
				} else {
					fname = fname + VALUES_POSTFIX;
				}
			}
		}

		return new SubExpression(new com.versant.odbms.query.Field(fname), var);
		
    }

    public Object visitVarNode(VarNode node, Object[] results) {

    	if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 0,
                    "results.length is not == 0");

        }
        Variable var = (Variable)varNodeMap.get(node.getIdentifier());
        if (var == null) {
        	String varType;
        	if (node.getCmd() != null) {
        		varType = _namingPolicy.mapClassName(node.getCmd());
        	}
        	else {
        		varType = node.getCls().getName(); 
        	}
        	var = new Variable(node.getIdentifier(), varType);
        	varNodeMap.put(node.getIdentifier(), var);
        }
        SubExpression s = new SubExpression(var);
        return s;
    	
    }

    public Object visitReservedFieldNode(ReservedFieldNode node, Object[] results) {

    	String varName;

    	if (node.lexeme.equals("this")) {
        	if (Debug.DEBUG) {
                Debug.assertInternal(results.length == 0 || results.length == 1,
                        "results.length is not == 0 and not == 1");
            }

        	if (results.length == 1) {
        		FieldMetaData fmd = processField(_cmd, (ReservedFieldNode )node.childList);
        		String fname = (String)results[0];
        		if (node.parent instanceof MethodNode) {

        			MethodNode methNode = (MethodNode)node.parent;
        			ClassMetaData cmd;

        			if (fmd.category == MDStatics.CATEGORY_COLLECTION) {
        				fname = fname + ELEMENTS_POSTFIX;
        			}
        			else if (fmd.category == MDStatics.CATEGORY_MAP) {
        				if (methNode.getMethod() == MethodNode.CONTAINS_KEY) {
        					fname = fname + KEYS_POSTFIX;
        				}
        				else {
        					fname = fname + VALUES_POSTFIX;
        				}
        			}
        		}
        		return new SubExpression(new com.versant.odbms.query.Field(fname), null);
        	}
    	}
    	
    	varName = node.lexeme;
    	Variable var = (Variable)varNodeMap.get(varName);
    	if (var == null) {
    		ClassMetaData cmd;
    		if (varName.equals("this")) {
    			cmd = _cmd;
    		}
    		else {
    			cmd = node.getTarget();
    		}
    		String VdsClassName = _namingPolicy.mapClassName(cmd);
    		var = new Variable(varName, VdsClassName);
    		varNodeMap.put(varName, var);
    	}
    	SubExpression s = new SubExpression(var);
    	return s;

    }

    public Object visitOrderNode(OrderNode node, Object[] results) {

    	if (Debug.DEBUG) {
            Debug.assertInternal(results.length == 1,
                    "results.length is not == 1");

            Debug.assertInternal(results[0] instanceof SubExpression,
            		"results[0] is not a SubExpression");
        }

    	OrderByExpression order = new OrderByExpression(
    			(SubExpression)results[0],
				(node.order == OrderNode.ORDER_DESCENDING));

    	return order;
    	
    }

    public Object visitMethodNode(MethodNode node, Object[] results) {

    	if (Debug.DEBUG) {
            Debug.assertInternal(results.length > 0,
                    "results.length is not > 0");
            Debug.assertInternal(results[0] instanceof SubExpression,
                    "results[0] is not instanceof SubExpression");
        }

    	int method = node.getMethod();
    	
    	switch (method) {
        case MethodNode.SQL:
        case MethodNode.CONTAINS_PARAM:

        	throw BindingSupportImpl.getInstance()
				.unsupportedOperation("Not supported method: "
							+ node.getName());

        case MethodNode.TO_LOWER_CASE:

    		if (Debug.DEBUG) {
                Debug.assertInternal(results.length == 1,
                		"results.length is not 1");

                Debug.assertInternal(results[0] instanceof SubExpression,
                        "results[0] is not instanceof SubExpression");
            }

        	return ((SubExpression)results[0]).toLowerCase();
    		
        case MethodNode.STARTS_WITH:
            if (Debug.DEBUG) {
                Debug.assertInternal(results[1] instanceof SubExpression,
                        "results[1] is not instanceof SubExpression");
            }
            return new Predicate((SubExpression)results[0],
            		Predicate.MATCHES,
					((SubExpression)results[1]).concat(new SubExpression("*")));

    	case MethodNode.ENDS_WITH:

    		if (Debug.DEBUG) {
                Debug.assertInternal(results[1] instanceof SubExpression,
                        "results[1] is not instanceof SubExpression");
            }
            return new Predicate((SubExpression)results[0],
            		Predicate.MATCHES, 
					new SubExpression("*").concat((SubExpression)results[1]));

    	case MethodNode.IS_EMPTY:
            if (node.childList instanceof ParamNode) {
            	SubExpression paramExpr = 
                    new SubExpression(
                        	new Parameter(((ParamNode)node.childList).getIdentifier()
                        			+ "_isempty"));

            	return new Predicate(paramExpr, Predicate.EQUALS,
            							new SubExpression(new Boolean(true)));
            	
            } else if (node.childList instanceof FieldNode) {
                String fname = ((FieldNode)node.childList).lexeme;
                return new Predicate(Predicate.IS_EMPTY,
                        new SubExpression(new com.versant.odbms.query.Field(fname + ELEMENTS_POSTFIX)));
            }
            return new Predicate(Predicate.IS_EMPTY, (SubExpression)results[0]);

    	case MethodNode.CONTAINS:
    	case MethodNode.CONTAINS_KEY:

    		return processContains(node, results);

    	default:

    		if (Debug.DEBUG) {
                Debug.assertInternal(false,
                        "Unsupported method");
            }

    		break;
        }

    	return null;

    }

    public Object visitAndNode(AndNode node, Object[] results) {
      if (Debug.DEBUG) {
          Debug.assertInternal(results.length >= 2,
                  "results.length is not  >= 2");
          Debug.assertInternal(results[0] instanceof Predicate ||
                  results[0] instanceof Expression ||
                  results[0] instanceof SubExpression, 
                  "results[0] is not a instanceof Predicate, Expression or SubExpression");
          Debug.assertInternal(results[1] instanceof Predicate ||
                results[1] instanceof Expression ||
                results[1] instanceof SubExpression, 
                "results[1] is not a instanceof Predicate, Expression or SubExpression");
      }
      Expression expr = processAND(results);
      return expr;
    }

    private Object processContains(MethodNode node, Object[] results) {

    	/*
    	 * General form is:
    	 * 		x.contains(y)
    	 * 		x.contains(y) && f(y)
    	 *	After parse the expression, node.childList correspond to "x" and
    	 *	node.childList.next corresponds to "y". 
    	 *
    	 *	"x" could be:
    	 *		- Field/NavField
    	 *		- Parameter
    	 *		- Literal
    	 *	"y" could be:
    	 *		- Variable
    	 *		- Parameter
    	 *		- Field/NavField
    	 *		- Literal
    	 *
    	 *	And we assume the following combinasions does not make much of sense
    	 *  in a query:
    	 *		1. x -> Literal
    	 *		2. x -> Paramater and y -> parameter
    	 */

    	/*
    	 * case: field.contains(variable):
    	 * 		example: field.contains(var) && f(var);
    	 * 		vds query: exists var in field : f(var); 
    	 */
    	if (((node.childList instanceof FieldNode) || 
    		 (node.childList instanceof FieldNavNode)) &&
    	    (node.childList.next instanceof VarNode)) {

            if (!((VarNode)node.childList.next).bound) {
            	throw BindingSupportImpl.getInstance()
					.unsupportedOperation(
						"Only the constrained variable is supported for contains "
							+ node.childList.next);
            }

            String fname = ((SubExpression)results[0]).toString();

            if (fname.endsWith(ELEMENTS_POSTFIX))
                fname = fname.substring(0, fname.length() - ELEMENTS_POSTFIX.length());

            Variable var = (Variable)varNodeMap.get(((VarNode)node.childList.next)
    				.getIdentifier());

        	if (Debug.DEBUG) {
                Debug.assertInternal(var != null, "VDS converter: variable not bound.");
            }

            SubExpression colExpr = (SubExpression)results[0];

        	SubExpression iterExpr = new SubExpression(var, colExpr);
        	SubExpression dummy = 
        		new SubExpression(new Expression(new Predicate(new SubExpression(new Boolean(true)),
        					Predicate.EQUALS, new SubExpression(new Boolean(true)))));

        	return new Predicate(iterExpr, Predicate.EXISTS, dummy);
        }
    	/*
    	 * case: field/fieldnav.contains(param):
    	 * 		example: field.contains(param)
    	 * 
    	 * vds query: exists param in field : param<the var> == param<the param>;
    	 * 
    	 * Note: param should not be collection type. 
    	 */
    	else if ((node.childList instanceof FieldNode || 
    			  node.childList instanceof FieldNavNode) &&
				 (node.childList.next instanceof ParamNode ||
				  node.childList.next instanceof ParamNodeProxy)) {

    		ParamNode paramNode;
			String varType; 

			if (node.childList.next instanceof ParamNode) {
				paramNode = (ParamNode)node.childList.next;
			}
			else {
				paramNode = (ParamNode)(((ParamNodeProxy)node.childList.next).getParamNode());
			}
    		String paramType = paramNode.getType();
    		Class paramCls = qParser.resolveParamType(paramType);
    		ClassMetaData cmd = _jmd.getClassMetaData(paramCls);
    		if (cmd != null) {
    			varType = _namingPolicy.mapClassName(cmd);
    		}
    		else {
    			// not a PC class: String etc. 
    			varType = paramCls.getName();
    		}
    		
    		String varName = paramNode.getIdentifier(); 
        	Variable var = new Variable(varName, varType);
        	varNodeMap.put(varName, var);

        	
        	SubExpression field = (SubExpression)results[0];
        	SubExpression iterExpr = new SubExpression(var, field);
            SubExpression param = (SubExpression)results[1];
        	SubExpression dummy = 
        		new SubExpression(new Expression(new Predicate(new SubExpression(var),
        					Predicate.EQUALS, param)));
        	return new Predicate(iterExpr, Predicate.EXISTS, dummy);
        }
    	/*
    	 * case: field/fieldnav.contains(literal):
    	 * 		example: field.contains("ss")
    	 * 
    	 * vds query: exists field<var> in field : field<var> == "ss";
    	 * 
    	 * Note: param should not be collection type. 
    	 */
    	else if ((node.childList instanceof FieldNode ||
    			  node.childList instanceof FieldNavNode) &&
                 (node.childList.next instanceof LiteralNode)) {

    		String varType = null;
    		
    		FieldNode fn = null;
    		ClassMetaData cmd = null;
    		
    		if (node.childList instanceof FieldNode) {
        		fn = (FieldNode)node.childList;
        		cmd = _cmd;
        	} 
        	else {
        		FieldNavNode fnn = (FieldNavNode)node.childList;
        		while (fnn.childList instanceof FieldNavNode) {
        			fnn = (FieldNavNode)fnn.childList;
        		}
        		fn = (FieldNode)fnn.childList;
        		cmd = (fnn.var == null) ? fnn.fmd.typeMetaData : fnn.var.getCmd();
        	}

    		String varName = fn.lexeme;
    		varType = cmd.getFieldMetaData(fn.lexeme).elementType.getName(); 
        	Variable var = new Variable(varName, varType);
        	varNodeMap.put(varName, var);

        	SubExpression field = (SubExpression)results[0];
        	SubExpression iterExpr = new SubExpression(var, field);
            SubExpression literal = (SubExpression)results[1];
        	SubExpression dummy = 
        		new SubExpression(new Expression(new Predicate(new SubExpression(var),
        					Predicate.EQUALS, literal)));
        	return new Predicate(iterExpr, Predicate.EXISTS, dummy);
        }
    	/*
    	 * case: param.contains(field/fieldnav):
    	 * example: Collection param = {"Steve" , "Tom"};
    	 * 			param.contains(field);
    	 * 
    	 * 		vds query: exists param in param : field == param;
    	 * 
    	 * Here, we use param'name as the variable name in vds query since they are
    	 * in different namespace in vds query. Normally, "param" is of collection type.
    	 * So we assume the element type in param is the same as "field" type.
    	 */
    	if (node.childList instanceof ParamNode &&
    		(node.childList.next instanceof FieldNode ||
    		 node.childList.next instanceof FieldNavNode)) {

    		String varType = null;
    		
    		FieldNode fn = null;
    		ClassMetaData cmd = null;
    		
    		if (node.childList.next instanceof FieldNode) {
        		fn = (FieldNode)node.childList.next;
        		cmd = _cmd;
        	} 
        	else {
        		FieldNavNode fnn = (FieldNavNode)node.childList.next;
        		while (fnn.childList instanceof FieldNavNode) {
        			fnn = (FieldNavNode)fnn.childList;
        		}
        		fn = (FieldNode)fnn.childList;
        		cmd = (fnn.var == null) ? fnn.fmd.typeMetaData : fnn.var.getCmd();
        	}

    		varType = cmd.getFieldMetaData(fn.lexeme).type.getName(); 
    		String varName = ((ParamNode)node.childList).getIdentifier(); 
        	Variable var = new Variable(varName, varType);
        	varNodeMap.put(varName, var);
            SubExpression param = (SubExpression)results[0];
        	SubExpression iterExpr = new SubExpression(var, param);
        	SubExpression field = (SubExpression)results[1];
        	SubExpression dummy = 
        		new SubExpression(new Expression(new Predicate(new SubExpression(var),
        					Predicate.EQUALS, field)));

        	return new Predicate(iterExpr, Predicate.EXISTS, dummy);

    	}
    	/*
    	 * case: param.contains(variable):
    	 * example: Collection param = {"Steve" , "Tom"};
    	 * 			param.contains(var);
    	 * 
    	 * 		vds query: exists param in param : var == param;
    	 * 
    	 * Here, we use param'name as the variable name in vds query since they are
    	 * in different namespace in vds query. Normally, "param" is of collection type.
    	 * So we assume the element type in param is the same as "variable" type.
    	 */
    	if (node.childList instanceof ParamNode &&
    		node.childList.next instanceof VarNode) {

    		String varType = ((VarNode)node.childList.next).getType(); 
    		String varName = ((ParamNode)node.childList.next).getIdentifier(); 
        	Variable var = new Variable(varName, varType);
        	varNodeMap.put(varName, var);
            SubExpression param = (SubExpression)results[0];
        	SubExpression iterExpr = new SubExpression(var, param);
        	SubExpression variable = (SubExpression)results[1];
        	SubExpression dummy = 
        		new SubExpression(new Expression(new Predicate(new SubExpression(var),
        					Predicate.EQUALS, variable)));

        	return new Predicate(iterExpr, Predicate.EXISTS, dummy);

    	}
    	else {

        	throw BindingSupportImpl.getInstance()
				.unsupportedOperation("Not supported contains method." + node.childList);

    	}

    }

    private Expression processAND(Object[] results) {

    	Expression expr = null;

    	if (results.length == 0) {
    		return null;
    	}

    	if (results[0] instanceof Predicate) {
        	expr = new Expression((Predicate)results[0]);
        } else if (results[0] instanceof Expression) {
        	expr = (Expression)results[0];
        } else { // SubExpression
    		expr = new Expression(new Predicate((SubExpression)results[0],
    								Predicate.EQUALS, new SubExpression(new Boolean(true))));
        }

    	Object[] expressions = new Object[results.length - 1];
    	for (int i = 0; i < results.length - 1; i++) {
    		expressions[i] = results[i + 1];
    	}

    	Expression expr2 = processAND(expressions);
    	if (expr2 == null)
    		return expr;

    	if (expr.getNode().operator() == Predicate.EXISTS) {
        	expr.quanlifierConstrains(expr2);
        }
        else {
    		expr = expr.and(expr2);
        }
        return expr;
    }

    public Object visitOrNode(OrNode node, Object[] results) {
    	
    	// TODO: needs to be revisited. Should be the same as visitAndNode
    	
    	if (Debug.DEBUG) {
            Debug.assertInternal(results.length >= 2,
                    "results.length is not  >= 2");
            Debug.assertInternal(results[0] instanceof Predicate ||
                    results[0] instanceof Expression,
                    "results[0] is not a instanceof Predicate or Expression");
            Debug.assertInternal(results[1] instanceof Predicate ||
                    results[1] instanceof Expression,
                    "results[1] is not a instanceof Predicate or Expression");
        }

        Expression expr = null;
        BitSet bits = new BitSet(2);
        bits.set(0, (results[0] instanceof Predicate));
        bits.set(1, (results[1] instanceof Predicate));
        if (bits.get(0) && bits.get(1)) {
            expr = new Expression((Predicate)results[0]).or((Predicate)results[1]);
        } else if (!bits.get(0) && bits.get(1)) {
            expr = ((Expression)results[0]).or((Predicate)results[1]);
        } else if (bits.get(0) && !(bits.get(1))) {
            expr = new Expression((Predicate)results[0]).or((Expression)results[1]);
        } else {
            expr = ((Expression)results[0]).or((Expression)results[1]);
        }

        if (results.length > 2) {
            for (int i = 2; i < results.length; i++) {
                if (results[i] instanceof Predicate)
                    expr = expr.or((Predicate)results[i]);
                else if (results[i] instanceof Expression)
                    expr = expr.or((Expression)results[i]);
                else {
                    if (Debug.DEBUG) {
                        Debug.assertInternal(false,
                                "Invalid node");
                    }
                }
            }
        }
        return expr;
    }

    public Object visitVarBindingNode(VarBindingNode node, Object[] results) {
    	throw BindingSupportImpl.getInstance()
			.unsupportedOperation("Unbound variable is not supported: " + node);
	}

    private boolean isBooleanNode(Node node) {
        return (node instanceof LiteralNode &&
                (("true").equalsIgnoreCase(((LiteralNode)node).value) ||
                ("false").equalsIgnoreCase(((LiteralNode)node).value)));
    }

    private SubExpression inverseBoolean(Node node) {
        if (Debug.DEBUG) {
            Debug.assertInternal(node instanceof LiteralNode,
                    "node is not a instanceof LiteralNode");
        }
        String value = ((LiteralNode)node).value;
        if ("true".equalsIgnoreCase(value)) {
            return new SubExpression(new Boolean("false"));
        }
        return new SubExpression(new Boolean("true"));
    }


    private Expression expandTree(Object r1, Object r2, boolean isEqual) {
        Expression lhs = getExpressionForm(r1);
        Expression lhsInvert = new Expression(lhs).negate();
        Expression rhs = getExpressionForm(r2);
        Expression rhsInvert = new Expression(rhs).negate();

        Expression expr1;
        Expression expr2;
        Expression expr;
        if (isEqual) {
            expr1 = new Expression(lhs.and(rhs));
            expr2 = lhsInvert.and(rhsInvert);
        } else {
            expr1 = new Expression(lhs.and(rhsInvert));
            expr2 = lhsInvert.and(rhs);
        }

        expr = expr1.or(expr2);

        return expr;
    }

    private Expression getExpressionForm(Object r) {
        Expression expr;
        if (r instanceof Expression) {
            expr = (Expression)r;
        } else if (r instanceof Predicate) {
            expr = new Expression((Predicate)r);
        } else {
            if (Debug.DEBUG) {
                Debug.assertInternal(r instanceof SubExpression,
                        "r is not a instanceof SubExpression");
            }
            // do we need to check it must be a boolean node?
            expr = new Expression(new Predicate(
                    (SubExpression)r, Predicate.EQUALS,
                    new SubExpression(new Boolean("true"))));
        }
        return expr;
    }

    private Expression convertToBooleanTree(Node child) {
        Expression expr = new Expression(new Predicate(new SubExpression(1),
                Predicate.EQUALS,
                new SubExpression(2)));
        if (("true").equalsIgnoreCase(((LiteralNode)child).value)) {
            expr = new Expression(new Predicate(new SubExpression(1),
                    Predicate.EQUALS,
                    new SubExpression(1)));
        }
        return expr;
    }

    int containsParam(String name) {
        if (_params == null)
            return -1;
        for (int i = 0; i < _params.length; i++) {
            if (_params[i].getIdentifier().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    boolean isCollection(String fname) {
        FieldMetaData fmd = _cmd.getFieldMetaData(fname);
        return (fmd.category == MDStatics.CATEGORY_COLLECTION);

    }

    boolean isEmbeddedCollection(String fname) {
        FieldMetaData fmd = _cmd.getFieldMetaData(fname);
        return (fmd.embedded) && (fmd.category == MDStatics.CATEGORY_COLLECTION);

    }

    boolean isMap(String fname) {
        FieldMetaData fmd = _cmd.getFieldMetaData(fname);
        return (fmd.category == MDStatics.CATEGORY_MAP);

    }

    boolean isEmbeddedMap(String fname) {
        FieldMetaData fmd = _cmd.getFieldMetaData(fname);
        return (fmd.embedded) && (fmd.category == MDStatics.CATEGORY_MAP);

    }

    FieldMetaData processField(ClassMetaData cmd, Node node) {

        FieldMetaData fmd = null;
    	ClassMetaData fcmd = cmd;
		Node next = null;

		if (node instanceof FieldNavNode && ((FieldNavNode)node).var != null)
			next = node.childList;
		else
			next = node;
		
    	while (next != null && (next instanceof FieldNavNode)) {

			FieldNavNode fnn = (FieldNavNode)next;

            if (fnn.cast != null) {
    			fcmd = qParser.resolveCastType(fnn.cast)[0];
    		}
			else {
				if (fcmd == null) {
		        	throw BindingSupportImpl.getInstance()
						.runtime("Cannot found field type for: " + fnn.lexeme);
				}
				
	            fmd = fcmd.getFieldMetaData(fnn.lexeme);
    	        if (Debug.DEBUG) {
        	        Debug.assertInternal(fmd != null,
            	            "Field " + fnn.lexeme +
                	        " can not be found in class " + fcmd);
	            }
	
    	        if (fmd.typeMetaData != null)
        	        extraClasses.add(fmd.type);
	
    	        fcmd = fmd.typeMetaData;
   	        }
			next = next.childList;
    	}

        if (Debug.DEBUG) {
			Debug.assertInternal(next instanceof FieldNode,
                       "Node " + next + " is not FieldNode ");
    	}

   		FieldNode fn = (FieldNode)next;

		fmd = fcmd.getFieldMetaData(fn.lexeme);
        if (Debug.DEBUG) {
			Debug.assertInternal(fmd != null,
                        "Field " + fn.lexeme +
                        " can not be found in class " + fcmd);
        }
		if (fmd == null) {
        	throw BindingSupportImpl.getInstance()
				.runtime("Can not found the type of field for: "
		    			 + fn.lexeme);
		}
		return fmd;		
    }

    String processPattern(String oldPattern) {

    	// TODO: a quick hack to replace SQL wild char '%' by VDS wild char '*'.
    	// to be revisited.
    	
    	String vdspattern = oldPattern.replace('%', '*');
    	return vdspattern;
    }
    
}
