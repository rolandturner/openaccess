
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
package com.versant.core.jdbc.metadata;

import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.JdbcTypeRegistry;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.metadata.MetaDataUtils;

import java.sql.Types;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This resolves the mapping for a field against mapping information from
 * the datastore and the SqlDriver.
 *
 * @see JdbcJavaTypeMapping
 * @see JdbcTypeMapping
 * @see #init
 * @see #resolveMapping
 */
public final class JdbcMappingResolver implements JdbcTypeRegistry {

    private String database;
    private HashMap javaTypeMappings;       // Class -> JdbcJavaTypeMapping
    private JdbcTypeMapping[] typeMappings; // indexed by JDBC type

    private static final int FIRST_TYPE = Types.BIT;
    
    private static final int LAST_TYPE = Types.REF;

    

    public JdbcMappingResolver() {
    }

    /**
     * Merge mappings from the sqlDriver and the datastore into a
     * combined array of type mappings. This is used to map JDBC
     * type codes from java.sql.Types to column properties.
     */
    public void init(SqlDriver sqlDriver, List dsTypeMappings,
            List dsJavaTypeMappings) {
        database = sqlDriver.getName();

        JdbcTypeMapping[] sqlDriverTypeMappings = sqlDriver.getTypeMappings();
        int n = sqlDriverTypeMappings.length;
        int q = dsTypeMappings.size();
        typeMappings = new JdbcTypeMapping[LAST_TYPE - FIRST_TYPE + 1];
        for (int i = 0; i < n; i++) {
            JdbcTypeMapping m = null;
            JdbcTypeMapping dm = sqlDriverTypeMappings[i];
            int jdbcType = dm.getJdbcType();
            for (int j = 0; j < q; j++) {
                JdbcTypeMapping dsm = (JdbcTypeMapping)dsTypeMappings.get(j);
                if (dsm.match(jdbcType, database)) {
                    dsm.copyFrom(dm);
                    m = dsm;
                    break;
                }
            }
            if (m == null) m = (JdbcTypeMapping)dm.clone();
            typeMappings[m.getJdbcType() - FIRST_TYPE] = m;
        }

        javaTypeMappings = sqlDriver.getJavaTypeMappings();
        n = dsJavaTypeMappings.size();
        for (int i = 0; i < n; i++) {
            JdbcJavaTypeMapping m = (JdbcJavaTypeMapping)dsJavaTypeMappings.get(i);
            String mdb = m.getDatabase();
            if (mdb != null && !mdb.equals(database)) continue;
            JdbcJavaTypeMapping sm = (JdbcJavaTypeMapping)javaTypeMappings.get(m.getJavaType());
            if (sm != null) m.copyFrom(sm);
            javaTypeMappings.put(m.getJavaType(), m);
        }
    }

    public String getDatabase() {
        return database;
    }

    public JdbcTypeMapping[] getTypeMappings() {
        return typeMappings;
    }

    public HashMap getJavaTypeMappings() {
        return javaTypeMappings;
    }

    /**
     * Resolve the mapping for javaType. The returned mapping will be complete
     * and can be used to create a JdbcColumn.
     */
    public JdbcJavaTypeMapping resolveMapping(Class javaType) {
        return resolveMapping(null, null, javaType);
    }

    /**
     * Resolve the mapping for fieldName using the fieldMapping provided
     * and our mapping tables. The returned mapping will be complete and
     * can be used to create a JdbcColumn.
     */
    public JdbcJavaTypeMapping resolveMapping(JdbcJavaTypeMapping fieldMapping,
            String fieldName, Class javaType) {
        JdbcJavaTypeMapping ans = fieldMapping;
        if (ans == null) ans = new JdbcJavaTypeMapping();
        JdbcJavaTypeMapping m = (JdbcJavaTypeMapping)javaTypeMappings.get(javaType);
        if (m != null) ans.copyFrom(m);
        JdbcTypeMapping tm = typeMappings[ans.getJdbcType() - FIRST_TYPE];
        if (tm == null) {
            
			throw BindingSupportImpl.getInstance().runtime("No JDBC type mapping found for: " +
                JdbcTypes.toString(ans.getJdbcType()) + " (" + javaType + " " +
                fieldName + ")");
	

        }
        ans.copyFrom(tm);
        return ans;
    }

    /**
     * If fm has a jdbcType set then use this type to fill in the other
     * fields of fm that do not have values. Otherwise do nothing. This
     * is used when the mapping is going to be used to modify a copy of
     * another column.
     */
    public void fillMappingForJdbcType(JdbcJavaTypeMapping fm) {
        if (fm.getJdbcType() == 0) return;
        JdbcTypeMapping tm = typeMappings[fm.getJdbcType() - FIRST_TYPE];
        if (tm == null) {
            throw BindingSupportImpl.getInstance().runtime("No JDBC type mapping found for: " +
                JdbcTypes.toString(fm.getJdbcType()));
        }
        fm.copyFrom(tm);
    }

    /**
     * Get the type mapping for a JDBC type code.
     */
    public JdbcTypeMapping getTypeMapping(int jdbcType) {
        return typeMappings[jdbcType - FIRST_TYPE];
    }

    /**
     * Get the converter factory used for the supplied JDBC type or null
     * if none.
     * @param jdbcType JDBC type code from java.sql.Types
     * @see Types
     */
    public JdbcConverterFactory getJdbcConverterFactory(int jdbcType) {
        JdbcTypes.toString(jdbcType);
        JdbcTypeMapping tm = getTypeMapping(jdbcType);
        if (tm == null) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                "No JDBC type mapping found for: " + JdbcTypes.toString(jdbcType));
        }
        JdbcConverterFactory ans = tm.getConverterFactory();
        if (ans == null) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                "No JdbcConverter found for JDBC type: " +
                JdbcTypes.toString(jdbcType));
        }
        return ans;
    }

    /**
     * Register any enabled store specific types with the mdutils so fields of
     * those types will be considered persistent.
     */
    public void registerStoreTypes(MetaDataUtils mdutils) {
        for (Iterator i = javaTypeMappings.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            Class type = (Class)e.getKey();
            JdbcJavaTypeMapping mapping = (JdbcJavaTypeMapping)e.getValue();
            if (mapping.getEnabled() != JdbcJavaTypeMapping.FALSE
                    && !mdutils.isPersistentType(type, 

												 Collections.EMPTY_MAP

 
						))
			{
                mdutils.registerStoreType(type);
            }
        }
    }

}
