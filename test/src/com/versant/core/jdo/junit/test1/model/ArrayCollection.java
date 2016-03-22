
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
 * A tree.
 */
public class ArrayCollection {

	private Integer age;

	private Date birthday;

	private String name;

	// not embedded collections
	private int[][] array2DInt = new int[10][10];

	private int[] arrayInt = new int[10];

	private Integer[] arrayIntW = new Integer[10];

	private long[] arrayLong = new long[10];

	private Long[] arrayLongW = new Long[10];

	private Long[][] array2DLongW = new Long[10][10];

	private Node[]/*<Node>*/arrayNode = new Node[10];

	private Set/*<Node>*/set = new HashSet();

	private ArrayList/*<Node>*/arraylist = new ArrayList();

	private HashMap/*<Node>*/map = new HashMap();

	// embedded collections
	private int[][] emArray2DInt = new int[10][10];

	private int[] emArrayInt = new int[10];

	private Integer[] emArrayIntW = new Integer[10];

	private long[] emArrayLong = new long[10];

	private Long[] emArrayLongW = new Long[10];

	private Long[][] emArray2DLongW = new Long[10][10];

	private Node[]/*<Node>*/emArrayNode = new Node[10];

	private Set/*<Node>*/emSet = new HashSet();

	private ArrayList/*<Node>*/emArraylist = new ArrayList();

	private HashMap/*<Node>*/emMap = new HashMap();

	public ArrayCollection() {
	}

	public ArrayCollection(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the age.
	 */
	public Integer getAge() {
		return age;
	}

	/**
	 * @param age The age to set.
	 */
	public void setAge(Integer age) {
		this.age = age;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the birthday.
	 */
	public Date getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday The birthday to set.
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return Returns the emArray2DInt.
	 */
	public int[][] getArray2DInt(boolean embedded) {
		if (embedded)
			return emArray2DInt;
		return array2DInt;
	}

	/**
	 * @param array2DInt The array2DInt to set.
	 */
	public void setArray2DInt(int[][] array2DInt, boolean embedded) {
		if (embedded)
			this.emArray2DInt = array2DInt;
		else
			this.array2DInt = array2DInt;
	}

	/**
	 * @return Returns the emArray2DLongW.
	 */
	public Long[][] getArray2DLongW(boolean embedded) {
		if (embedded)
			return emArray2DLongW;
		return array2DLongW;
	}

	/**
	 * @param array2DLongW The array2DLongW to set.
	 */
	public void setArray2DLongW(Long[][] array2DLongW, boolean embedded) {
		if (embedded)
			this.emArray2DLongW = array2DLongW;
		else
			this.array2DLongW = array2DLongW;
	}

	/**
	 * @return Returns the emArrayInt.
	 */
	public int[] getArrayInt(boolean embedded) {
		if (embedded)
			return emArrayInt;
		return arrayInt;
	}

	/**
	 * @param arrayInt The arrayInt to set.
	 */
	public void setArrayInt(int[] arrayInt, boolean embedded) {
		if (embedded)
			this.emArrayInt = arrayInt;
		else
			this.arrayInt = arrayInt;
	}

	/**
	 * @return Returns the emArrayIntW.
	 */
	public Integer[] getArrayIntW(boolean embedded) {
		if (embedded)
			return emArrayIntW;
		return arrayIntW;
	}

	/**
	 * @param arrayIntW The arrayIntW to set.
	 */
	public void setArrayIntW(Integer[] arrayIntW, boolean embedded) {
		if (embedded)
			this.emArrayIntW = arrayIntW;
		else
			this.arrayIntW = arrayIntW;
	}

	/**
	 * @return Returns the emArrayLong.
	 */
	public long[] getArrayLong(boolean embedded) {
		if (embedded)
			return emArrayLong;
		return arrayLong;
	}

	/**
	 * @param arrayLong The arrayLong to set.
	 */
	public void setArrayLong(long[] arrayLong, boolean embedded) {
		if (embedded)
			this.emArrayLong = arrayLong;
		else
			this.arrayLong = arrayLong;
	}

	/**
	 * @return Returns the emArrayLongW.
	 */
	public Long[] getArrayLongW(boolean embedded) {
		if (embedded)
			return emArrayLongW;
		return arrayLongW;
	}

	/**
	 * @param arrayLongW The arrayLongW to set.
	 */
	public void setArrayLongW(Long[] arrayLongW, boolean embedded) {
		if (embedded)
			this.emArrayLongW = arrayLongW;
		else
			this.arrayLongW = arrayLongW;
	}

	/**
	 * @return Returns the arrayNode.
	 */
	public Node[] getArrayNode(boolean embedded) {
		if (embedded)
			return emArrayNode;
		return arrayNode;
	}

	/**
	 * @param arrayNode The arrayNode to set.
	 */
	public void setArrayNode(Node[] arrayNode, boolean embedded) {
		if (embedded)
			this.emArrayNode = arrayNode;
		else
			this.arrayNode = arrayNode;
	}

	/**
	 * @return Returns the emArraylist.
	 */
	public ArrayList getArraylist(boolean embedded) {
		if (embedded)
			return emArraylist;
		return arraylist;
	}

	/**
	 * @param arraylist The arraylist to set.
	 */
	public void setArraylist(ArrayList arraylist, boolean embedded) {
		if (embedded)
			this.emArraylist = arraylist;
		else
			this.arraylist = arraylist;
	}

	/**
	 * @return Returns the emMap.
	 */
	public HashMap getMap(boolean embbeded) {
		if (embbeded)
			return emMap;
		return map;
	}

	/**
	 * @param emMap The emMap to set.
	 */
	public void setMap(HashMap map, boolean embedded) {
		if (embedded)
			this.emMap = map;
		this.map = map;
	}

	/**
	 * @return Returns the emSet.
	 */
	public Set getSet(boolean embedded) {
		if (embedded)
			return emSet;
		return set;
	}

	/**
	 * @param emSet The emSet to set.
	 */
	public void setSet(Set aSet, boolean embbeded) {
		if (embbeded)
			this.emSet = aSet;
		this.set = aSet;
	}

	/**
	 * Get the names of all our children sorted alpha.
	 */
	public String getArrayListStr(boolean embedded) {
		ArrayList a = new ArrayList();
		for (Iterator i = getArraylist(embedded).iterator(); i.hasNext();) {
			Node n = (Node) i.next();
			if (n == null)
				a.add("<null>");
			else
				a.add(n.getName());
		}
		Collections.sort(a);
		return a.toString();
	}

	/**
	 * Get the names of all our children sorted alpha.
	 */
	public String getMapStr(boolean embedded) {
		ArrayList a = new ArrayList();
		for (Iterator i = getMap(embedded).values().iterator(); i.hasNext();) {
			Node n = (Node) i.next();
			a.add(n.getName());
		}
		Collections.sort(a);
		return a.toString();
	}

	/**
	 * Get the names of all our children sorted alpha.
	 */
	public String getSetStr(boolean embedded) {
		ArrayList a = new ArrayList();
		for (Iterator i = getSet(embedded).iterator(); i.hasNext();) {
			Node n = (Node) i.next();
			a.add(n.getName());
		}
		Collections.sort(a);
		return a.toString();
	}

	public String getArrayIntStr(boolean embedded) {
		ArrayList a = new ArrayList();
		int[] intarr = getArrayInt(embedded);
		for (int i = 0; i < intarr.length; i++) {
			a.add((new Integer(intarr[i])).toString());
		}
		Collections.sort(a);
		return a.toString();
	}

	public String getArrayIntWStr(boolean embedded) {
		ArrayList a = new ArrayList();
		Integer[] intarr = getArrayIntW(embedded);
		for (int i = 0; i < intarr.length; i++) {
			a.add(intarr[i].toString());
		}
		Collections.sort(a);
		return a.toString();
	}
}
