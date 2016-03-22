
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
package com.versant.core.jdo.query.mem;


import com.versant.lib.bcel.Constants;
import com.versant.lib.bcel.classfile.JavaClass;
import com.versant.lib.bcel.generic.ClassGen;
import com.versant.lib.bcel.generic.InstructionFactory;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.jdo.*;
import com.versant.core.jdo.query.ParamNode;
import com.versant.core.jdo.query.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.versant.core.common.BindingSupportImpl;

/**
 * Compiler for in memory queries. This maintains a cache of precompiled
 * queries.
 */
public class MemQueryCompiler {

    private final ModelMetaData jmd;
    private final ClassLoader loader;
    private Map compiledQueryMap = new HashMap();
    private long clsCounter;
    private Method defineClass;

    public MemQueryCompiler(ModelMetaData jmd, ClassLoader loader) {
        this.jmd = jmd;
        this.loader = loader;

        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass",
                    new Class[]{String.class, byte[].class, Integer.TYPE,
                                Integer.TYPE});
        } catch (NoSuchMethodException e) {
            // not possible really
            throw BindingSupportImpl.getInstance().internal(e.toString(), e);
        }
        defineClass.setAccessible(true);

    }

    public synchronized BCodeQuery compile(QueryDetails queryParams, Object[] params) {
        BCodeQuery bCodeQuery = (BCodeQuery)compiledQueryMap.get(queryParams);
        if (bCodeQuery != null) return bCodeQuery;


        boolean toFilter = true;
        if (queryParams.getFilter() == null || queryParams.getFilter().equals(
                "true")) {
            toFilter = false;
        } else {
            toFilter = true;
        }

        ClassMetaData classMetaData = jmd.getClassMetaData(
                queryParams.getCandidateClass());
        String name = getQClsName();
        ClassGen classGen = new ClassGen(name, BCodeQuery.class.getName(),
                "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER,
                null);
        InstructionFactory factory = new InstructionFactory(classGen);
        classGen.addEmptyConstructor(Constants.ACC_PUBLIC);

        CompiledMemQuery compiledMemQuery = new CompiledMemQuery(jmd);
        try {
            compiledMemQuery.compile(queryParams);
        } catch (ParseException e) {
            throw BindingSupportImpl.getInstance().exception(e.getMessage());
        }

        if (toFilter) {
            ByteCodeQVisitor byteCodeQVisitor =
                    new ByteCodeQVisitor(classGen, factory, name,
                    classMetaData, compiledMemQuery);
            byteCodeQVisitor.setParamMap(
                    createParamMap(params, compiledMemQuery));
            compiledMemQuery.filter.visit(byteCodeQVisitor, null);
            byteCodeQVisitor.finish();
        }

        if (queryParams.getOrdering() != null) {
            ByteCodeQCompareVisitor byteCodeQCompareVisitor = new ByteCodeQCompareVisitor(
                    classGen, factory, name, classMetaData);
            for (int i = 0; i < compiledMemQuery.orders.length; i++) {
                compiledMemQuery.orders[i].visit(byteCodeQCompareVisitor, null);
            }
            byteCodeQCompareVisitor.finish();
        }

        JavaClass javaClass = classGen.getJavaClass();
        try {
            bCodeQuery = (BCodeQuery)defineClass(javaClass.getBytes(), null).newInstance();
            compiledQueryMap.put(queryParams, bCodeQuery);
        } catch (Exception e) {
            if (BindingSupportImpl.getInstance().isOwnException(e)) {
                throw (RuntimeException)e;
            }
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        }

        return bCodeQuery;
    }

    private Map createParamMap(Object[] params,
            CompiledMemQuery compiledMemQuery) {
        Map m = new HashMap();
        if (compiledMemQuery.params == null) {
            return m;
        }
        for (int i = 0; i < compiledMemQuery.params.length; i++) {
            ParamNode param = compiledMemQuery.params[i];
            m.put(param.getIdentifier(), params[i]);
        }
        return m;
    }

    private Class defineClass(byte[] bytecode, File dir) {
        try {
            Class cls = (Class)defineClass.invoke(loader, new Object[]{null,
                    bytecode, new Integer(0), new Integer(bytecode.length)});
            if (dir != null) {
                File f = new File(dir, cls.getName() + ".class");
                try {
                    FileOutputStream o = new FileOutputStream(f);
                    o.write(bytecode, 0, bytecode.length);
                    o.close();
                } catch (IOException x) {
                    throw BindingSupportImpl.getInstance().runtime(
                            "Error writing to " + f + ": " + x, x);
                }
            }
            return cls;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw BindingSupportImpl.getInstance().internal(t.toString(), t);
        } catch (Exception x) {
            throw BindingSupportImpl.getInstance().internal(x.toString(), x);
        }
    }

    private synchronized String getQClsName() {
        return "VOA_QUERY_" + clsCounter++;
    }

}
