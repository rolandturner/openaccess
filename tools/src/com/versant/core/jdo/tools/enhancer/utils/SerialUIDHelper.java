
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
package com.versant.core.jdo.tools.enhancer.utils;

import com.versant.lib.bcel.classfile.JavaClass;
import com.versant.lib.bcel.classfile.Method;
import com.versant.lib.bcel.classfile.Field;
import com.versant.lib.bcel.Repository;
import com.versant.lib.bcel.Constants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.DigestOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;



/**
 */
public class SerialUIDHelper {

	/**
     *  Find out if the class has a static class initializer <clinit>
     *
     */
    private static boolean hasStaticInitializer(JavaClass javaClass){
		Method[] methods = javaClass.getMethods();
		for(int i = 0; i < methods.length; i++) {
		    Method m = methods[i];
		    if (m.getName().equals("<clinit>")){//static constructor = <clinit>
				return true;
		    }
		}
		return false;
	}

	private static Comparator compareMethodBySig = new SerialUIDHelper.MethodSortSig();

	private static class MethodSortSig implements Comparator {
	    public int compare(Object a, Object b){
			Method methodA = (Method)a;
			Method methodB = (Method)b;
			String sigA = methodA.getSignature();
			String sigB = methodB.getSignature();
			return sigA.compareTo(sigB);
	    }
	}

    private static Comparator compareMethodByName = new SerialUIDHelper.MethodSortName();

	private static class MethodSortName implements Comparator {
	    public int compare(Object a, Object b){
			Method methodA = (Method)a;
			Method methodB = (Method)b;
			String sigA = methodA.getName();
			String sigB = methodB.getName();
			return sigA.compareTo(sigB);
	    }
	}

    private static Comparator compareFieldByName = new SerialUIDHelper.FieldSort();

	private static class FieldSort implements Comparator {
	    public int compare(Object a, Object b){
			Field fieldA = (Field)a;
			Field fieldB = (Field)b;
			return fieldA.getName().compareTo(fieldB.getName());
	    }
	}

	/*
     * Comparator object for Classes and Interfaces
     */
	private static Comparator compareStringByName = new SerialUIDHelper.CompareStringByName();

    private static class CompareStringByName implements Comparator {
		public int compare(Object o1, Object o2) {
			String c1 = (String)o1;
			String c2 = (String)o2;
			return c1.compareTo(c2);
		}
    }

	private static Set removePrivateConstructorsAndSort(JavaClass javaClass){
		TreeSet set = new TreeSet(compareMethodBySig);
		Method[] methods = javaClass.getMethods();
		for(int i = 0; i < methods.length; i++) {
		    com.versant.lib.bcel.classfile.Method m = methods[i];
		    if (m.getName().equals("<init>") && (!m.isPrivate())){
				set.add(m);
		    }
		}
		return set;
	}

	private static Set removePrivateAndConstructorsAndSort(JavaClass javaClass){
		TreeSet set = new TreeSet(compareMethodByName);
		Method[] methods = javaClass.getMethods();
		for(int i = 0; i < methods.length; i++) {
		    Method m = methods[i];
		    if (!m.getName().startsWith("<")){
                if (!m.isPrivate()){
                     set.add(m);
				}
		    }
		}
		return set;
	}


	/*
     * Compute a hash for the specified class.  Incrementally add
     * items to the hash accumulating in the digest stream.
     * Fold the hash into a long.  Use the SHA secure hash function.
     */
	public static long computeSerialVersionUID(JavaClass clazz) {
		ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);

		long h = 0;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			DigestOutputStream mdo = new DigestOutputStream(devnull, md);
			DataOutputStream data = new DataOutputStream(mdo);

			data.writeUTF(clazz.getClassName());
//            System.out.println("-> "+clazz.getClassName());


			int classaccess = clazz.getAccessFlags();
			classaccess &= (Constants.ACC_PUBLIC | Constants.ACC_FINAL |
				Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT);

			/* Workaround for javac bug that only set ABSTRACT for
			* interfaces if the interface had some methods.
			* The ABSTRACT bit reflects that the number of methods > 0.
			* This is required so correct hashes can be computed
			* for existing class files.
            * Previously this hack was present in the VM.
			*/
			Method[] method = clazz.getMethods();
			if ((classaccess & Constants.ACC_INTERFACE) != 0) {
				classaccess &= (~Constants.ACC_ABSTRACT);
				if (method.length > 0) {
					classaccess |= Constants.ACC_ABSTRACT;
				}
			}

			data.writeInt(classaccess);
//            System.out.println("-> "+classaccess);

			/*
			* Get the list of interfaces supported,
			* Accumulate their names in Lexical order
			* and add them to the hash
			*/

			String interfaces[] = clazz.getInterfaceNames();
			Arrays.sort(interfaces, compareStringByName);

			for (int i = 0; i < interfaces.length; i++) {
				data.writeUTF(interfaces[i]);
//                System.out.println("-> "+interfaces[i]);
			}


			/* Sort the field names to get a deterministic order */
			com.versant.lib.bcel.classfile.Field[] field = clazz.getFields();
			Arrays.sort(field, compareFieldByName);

			for (int i = 0; i < field.length; i++) {
				Field f = field[i];

				/* Include in the hash all fields except those that are
				* private transient and private static.
				*/
				int m = f.getAccessFlags();
				if ((f.isPrivate() && f.isStatic()) ||
					(f.isPrivate() && f.isTransient())){
				    continue;
				}

				data.writeUTF(f.getName());
//System.out.println("-> "+f.getName()+m+f.getSignature());
				data.writeInt(m);
				data.writeUTF(f.getSignature());

			}
//System.out.println("-------------------------------------------");
			if (hasStaticInitializer(clazz)) {
				data.writeUTF("<clinit>");
//System.out.println("-> <clinit>"+Constants.ACC_STATIC+"()V");
				data.writeInt(Constants.ACC_STATIC); // TBD: what modifiers does it have
				data.writeUTF("()V");

			}
//System.out.println("-------------------------------------------");
			/*
			* Get the list of constructors including name and signature
			* Sort lexically, add all except the private constructors
			* to the hash with their access flags
			*/

			Iterator nonPrivateConstructorsIter = removePrivateConstructorsAndSort(clazz).iterator();
			while (nonPrivateConstructorsIter.hasNext()){
			    Method m = (Method)nonPrivateConstructorsIter.next();
				String mname = "<init>";
				String desc = m.getSignature();
				desc = desc.replace('/', '.');
				data.writeUTF(mname);
//                System.out.println("-> "+mname+m.getAccessFlags()+desc);
				data.writeInt(m.getAccessFlags());
				data.writeUTF(desc);
			}
//System.out.println("-------------------------------------------");

			/* Include in the hash all methods except those that are
			* private transient and private static.
			*/

		    Iterator nonPrivateAndNoConstructorsIter = removePrivateAndConstructorsAndSort(clazz).iterator();
			while (nonPrivateAndNoConstructorsIter.hasNext()){
			    Method m = (Method)nonPrivateAndNoConstructorsIter.next();
				String mname = m.getName();
				String desc = m.getSignature();
				desc = desc.replace('/', '.');
				data.writeUTF(mname);
//                System.out.println("-> "+mname+m.getAccessFlags()+desc);
				data.writeInt(m.getAccessFlags());
				data.writeUTF(desc);
			}
//System.out.println("-------------------------------------------");
		    /* Compute the hash value for this class.
			* Use only the first 64 bits of the hash.
			*/
			data.flush();
			byte hasharray[] = md.digest();
			for (int i = 0; i < Math.min(8, hasharray.length); i++) {
				h += (long)(hasharray[i] & 255) << (i * 8);
			}
		} catch (IOException ignore) {
			/* can't happen, but be deterministic anyway. */
			h = -1;
		} catch (NoSuchAlgorithmException complain) {
			throw new SecurityException(complain.getMessage());
		}
		return h;
    }

}
