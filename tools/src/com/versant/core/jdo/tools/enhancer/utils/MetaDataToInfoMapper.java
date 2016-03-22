
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

import com.versant.lib.bcel.classfile.ClassParser;
import com.versant.lib.bcel.classfile.Field;
import com.versant.lib.bcel.classfile.JavaClass;
import com.versant.lib.bcel.generic.Type;
import com.versant.core.jdo.tools.enhancer.info.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.parser.*;

import javax.jdo.InstanceCallbacks;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
public class MetaDataToInfoMapper {

    private HashMap classResourceMap;
    private ClassLoader callingClass;
	private Hashtable primitives;
	private HashMap classMap = new HashMap();


    public MetaDataToInfoMapper(HashMap classResourceMap, ClassLoader callingClass) {
        this.callingClass = callingClass;

        this.classResourceMap = classResourceMap;
	    primitives = new Hashtable();
		primitives.put("int","java.lang.Integer");
		primitives.put("byte","java.lang.Byte");
		primitives.put("char","java.lang.Character");
		primitives.put("short","java.lang.Short");
		primitives.put("float","java.lang.Float");
		primitives.put("double","java.lang.Double");
		primitives.put("long","java.lang.Long");
		primitives.put("boolean","java.lang.Boolean");

	}

    public List getClassInfoList(){
	    for (Iterator iterator = classMap.keySet().iterator();iterator.hasNext();) {
		    String name = (String) iterator.next();
		    ClassInfo clsInfo = (ClassInfo)classMap.get(name);
		    if (clsInfo.getTopName() != null ){
		        ClassInfo top = (ClassInfo)classMap.get(clsInfo.getTopName());
			    clsInfo.setTopPCSuperClass(top);
			    if (clsInfo.getIdentityType() == MDStatics.IDENTITY_TYPE_APPLICATION){
					clsInfo.setObjectidClass(top.getObjectidClass());

			    }
		    }
	    }

	    for (Iterator iterator = classMap.keySet().iterator();iterator.hasNext();) {
		    String name = (String) iterator.next();
		    ClassInfo clsInfo = (ClassInfo)classMap.get(name);
		    if (clsInfo.getTopName().equals(clsInfo.getClassName()) ){
		        clsInfo.setTopPCSuperClass(null);
		    }
	    }
	    return new ArrayList(classMap.values());
    }



    private JavaClass getJavaClass(String className)throws IOException{
        String classFileName = className.replace('.','/')+".class";
        InputStream inputStream = callingClass.getResourceAsStream(classFileName);
        if (inputStream == null){
	        inputStream = callingClass.getResourceAsStream("/"+classFileName);
	        if (inputStream == null){
		        throw new javax.jdo.JDOFatalUserException(
                    "Class not found: " + className +
                    " (" + classResourceMap.get(className) + ")");
	        }

        }
        ClassParser parser = new ClassParser(inputStream, classFileName);
        return parser.parse();
    }





	public void setClassInfo(JdoClass jdoClass,ClassMetaData metaData){
		ClassInfo infoClass = new ClassInfo();

		try{

			infoClass.setInstanceCallbacks(InstanceCallbacks.class.isAssignableFrom(metaData.cls));
			infoClass.setClassName(metaData.cls.getName());
			infoClass.setIdentityType(metaData.identityType);
			infoClass.setKeyGen(jdoClass.hasKeyGen());
			if (metaData.objectIdClass != null){
				infoClass.setObjectidClass(metaData.objectIdClass.getName());
			}

			if (metaData.pcSuperClass != null){
				infoClass.setPersistenceCapableSuperclass(metaData.pcSuperClass.getName());
			}

			if (metaData.top != null){
				infoClass.setTopName(metaData.top.cls.getName());
			}

			JavaClass clazz = getJavaClass(metaData.cls.getName());

			Field[] fields = clazz.getFields();
            for (int i = 0;i < fields.length; i++) {
                Field f = fields[i];
	            FieldMetaData fmd = metaData.getFieldMetaData(f.getName());
	            if (fmd == null){
		            continue;
	            }

                Type type = Type.getReturnType(f.getSignature());


	            FieldInfo infoField = new FieldInfo();
	            infoField.setJdoSetName("jdoSet"+f.getName());
	            infoField.setJdoGetName("jdoGet"+f.getName());
	            infoField.isPublic(f.isPublic());
	            infoField.isPrivate(f.isPrivate());
	            infoField.isProtected(f.isProtected());
	            infoField.setFieldName(f.getName());
	            infoField.setSignature(f.getSignature());
	            infoField.setType(Type.getReturnType(f.getSignature()));
		        infoField.primaryKey(fmd.primaryKey);
	            infoField.setPersistenceModifier(fmd.persistenceModifier);
	            infoField.setFieldNo(fmd.managedFieldNo);
	            infoField.defaultFetchGroup(fmd.isJDODefaultFetchGroup());
                int end1 = type.toString().indexOf("[");
                if (end1 != -1){
                    String returnType = type.toString().substring(0, end1); // return type , excluding Array
                    infoField.isArray(true);
                    infoField.setReturnType(returnType);
                }


                if(fmd.category == MDStatics.CATEGORY_ARRAY){                              // arrays
	                int end = type.toString().indexOf("[");
	                String returnType = type.toString().substring(0,end); // return type , excluding Array
	                infoField.isArray(true);
	                infoField.setReturnType(returnType);
                } else {
                    infoField.setReturnType(type.toString());
                    String stringType = type.toString();
                    if (primitives.containsKey(stringType)){                // primitive
	                    infoField.isPrimative(true);
                        infoField.setPrimativeTypeObject((String)primitives.get(type.toString()));
                    }
                }

                if (infoField.getPersistenceModifier() !=  MDStatics.PERSISTENCE_MODIFIER_NONE){
                    infoClass.getFieldList().add(infoField);
                }
            }
			classMap.put(infoClass.getClassName(),infoClass);
        }catch (Exception x){
            x.printStackTrace();
        }
	}




}
