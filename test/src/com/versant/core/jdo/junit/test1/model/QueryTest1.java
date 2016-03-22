
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
package com.versant.core.jdo.junit.test1.model;


import java.util.*;


/**
 *	<p>Used in testing; should be enhanced.</p>
 *
 */
public class QueryTest1
{
	private long 		num			= 0L;
	private String 		string 		= null;
	private String 		clobField	= null;
	private boolean		bool		= false;
	private float		decimal		= 0F;
	private char		character	= ' ';
	private Date		date		= null;
	private Collection	manyToMany	= null;


	public long getNum ()
	{
		return num;
	}


	public void setNum (long val)
	{
		num = val;
	}


	public String getString ()
	{
		return string;
	}


	public void setString (String val)
	{
		string = val;
	}


	public String getClob ()
	{
		return clobField;
	}


	public void setClob (String val)
	{
		clobField = val;
	}


	public boolean getBool ()
	{
		return bool;
	}


	public void setBool (boolean val)
	{
		bool = val;
	}


	public float  getDecimal ()
	{
		return decimal;
	}


	public void setDecimal (float val)
	{
		decimal = val;
	}


	public char getCharacter ()
	{
		return character;
	}


	public void setCharacter (char val)
	{
		character = val;
	}


	public void setDate (Date val)
	{
		date = val;
	}


	public Date getDate ()
	{
		return date;
	}


	public Collection getManyToMany ()
	{
		return manyToMany;
	}


	public void setManyToMany (Collection val)
	{
		manyToMany = val;
	}
}
