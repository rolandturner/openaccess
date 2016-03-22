
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
package com.versant.core.jdbc;

import com.versant.core.common.GenericState;
import com.versant.core.common.GenericOID;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.conn.PooledPSWithParamLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import com.versant.core.common.BindingSupportImpl;

/**
 * Static JDBC utility functions. These are used to implement GenericOID
 * and GenericState in a way similar to the generated State and OID
 * implementations. They are also used elsewhere in JdbcDataStore.
 * @see GenericOID
 * @see GenericState
 */
public class JdbcUtils implements MDStatics {

    private JdbcUtils() { }

    
    
    /**
     * Set a parameter on a PreparedStatement. This will call setInt or
     * setShort etc depending on the type of value.
     */
    public static void set(PreparedStatement ps, int index, Object value,
            int javaTypeCode, int jdbcType) throws SQLException {
        if (value == null) {
            ps.setNull(index, jdbcType);
        } else {
            switch (javaTypeCode) {
                case INT:

				case INTW:
                    ps.setInt(index, ((Number)value).intValue());
                    break;
                case SHORT:
                
                case SHORTW:                
                    ps.setShort(index, ((Number)value).shortValue());
                    break;
                case STRING:
                    ps.setString(index, (String)value );
                    break;
                case BOOLEAN:
                
                case BOOLEANW:
                    ps.setBoolean(index, ((Boolean)value).booleanValue());
                    break;
                case BYTE:
                
                case BYTEW:
                    ps.setByte(index, ((Number)value).byteValue());
                    break;
                case BIGDECIMAL:
                    ps.setBigDecimal(index, (BigDecimal)value);
                    break;
                case BIGINTEGER:
                    ps.setBigDecimal(index, new BigDecimal((BigInteger)value));
                    break;                
                case DOUBLE:
                
				case DOUBLEW:                
                    ps.setDouble(index, ((Number)value).doubleValue());
                    break;                
                case FLOAT:
                
                case FLOATW:
                    ps.setFloat(index, ((Number)value).floatValue());
                    break;                
                case LONG:
                
                case LONGW:
                    ps.setLong(index, ((Number)value).longValue());
                    break;

                default:
                    ps.setObject(index, value, jdbcType);
            };
        }
    }

    /**
     * Set a parameter on a PreparedStatement. This will call setInt or
     * setShort etc depending on the type of value. This method is used
     * when the value is available as an int to avoid creating a wrapper
     * instance.
     */
    public static void set(PreparedStatement ps, int index, int value,
            int javaTypeCode, int jdbcType) throws SQLException {
        switch (javaTypeCode) {
            case INTW:
            case INT:
                ps.setInt(index, value);
                break;
            case SHORTW:
            case SHORT:
                ps.setShort(index, (short)value);
                break;
            case BYTEW:
            case BYTE:
                ps.setByte(index, (byte)value);
                break;

            default:
                throw BindingSupportImpl.getInstance().internal("set(...int...) called " +
                    "for javaTypeCode " + javaTypeCode);
        };
    }




    /**
     * Get a value from a ResultSet. This will call getInt or getShort etc.
     */
    public static Object get(ResultSet rs, int index, int javaTypeCode, int scale )
            throws SQLException {
        switch (javaTypeCode) {
            case INT:

            case INTW:
				return new Integer(rs.getInt(index));
            case SHORT:

            case SHORTW:
					return new Short(rs.getShort(index));
            case STRING:
                return rs.getString(index);
            case BOOLEAN:

            case BOOLEANW:
                return rs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE;
            case BYTE:

            case BYTEW:
                return new Byte(rs.getByte(index));
            case BIGDECIMAL:
                return rs.getBigDecimal(index);
            case BIGINTEGER:
                BigDecimal d = rs.getBigDecimal(index);
                if (d == null) return null;

                return d.unscaledValue();


            case DOUBLE:

            case DOUBLEW:
                return new Double(rs.getDouble(index));
            case FLOAT:

            case FLOATW:
				return new Float(rs.getFloat(index));
            case LONG:

            case LONGW:
                return new Long(rs.getLong(index));

            default:
                return rs.getObject(index);
        }
    }



    /**
     * Convert an ArrayList of values into an properly typed array (int[],
     * String[], byte[] etc.). Arrays of wrapper types (Integer et al) are
     * returned as arrays of primitives to save memory. This means that
     * there cannot be any nulls in values.
     */
    public static Object toArrayNoNulls(ArrayList values, int javaTypeCode)
            throws SQLException {
        int n = values.size();
        switch (javaTypeCode) {
            case INTW:
            case INT:

                int[] inta = new int[n];
                for (int i = inta.length - 1; i >= 0; i--) {
                    inta[i] = ((Integer)values.get(i)).intValue();
                }
                return inta;
            case SHORTW:
            case SHORT:

                short[] shorta = new short[n];
                for (int i = shorta.length - 1; i >= 0; i--) {
                    shorta[i] = ((Short)values.get(i)).shortValue();
                }
                return shorta;
            case STRING:
                String[] stringa = new String[n];
                values.toArray(stringa);
                return stringa;
            case BOOLEANW:
            case BOOLEAN:
                boolean[] booleana = new boolean[n];
                for (int i = booleana.length - 1; i >= 0; i--) {
                    booleana[i] = ((Boolean)values.get(i)).booleanValue();
                }
                return booleana;
            case BYTEW:
            case BYTE:
                byte[] bytea = new byte[n];
                for (int i = bytea.length - 1; i >= 0; i--) {
                    bytea[i] = ((Byte)values.get(i)).byteValue();
                }
                return bytea;
            case BIGDECIMAL:
                BigDecimal[] bigdecimala = new BigDecimal[n];
                values.toArray(bigdecimala);
                return bigdecimala;
            case BIGINTEGER:
                BigInteger[] bigintegera = new BigInteger[n];
                values.toArray(bigintegera);
                return bigintegera;
            case DOUBLEW:
            case DOUBLE:
                double[] doublea = new double[n];
                for (int i = doublea.length - 1; i >= 0; i--) {
                    doublea[i] = ((Double)values.get(i)).doubleValue();
                }
                return doublea;
            case FLOATW:
            case FLOAT:
                float[] floata = new float[n];
                for (int i = floata.length - 1; i >= 0; i--) {
                    floata[i] = ((Float)values.get(i)).floatValue();
                }
                return floata;
            case LONGW:
            case LONG:

                long[] longa = new long[n];
                for (int i = longa.length - 1; i >= 0; i--) {
                    longa[i] = ((Long)values.get(i)).longValue();
                }
                return longa;
            default:
                return values.toArray();
        }
    }

    /**
     * Convert x to a String by calling its toString method. If x is an
     * SQLException then any getNextExceptions() are recursively added to
     * the string preceded by linefeeds.
     */
    public static String toString(Throwable x) {
        if (x instanceof SQLException) {
            StringBuffer s = new StringBuffer();
            s.append(x);
            SQLException se = (SQLException)x;
            for (se = se.getNextException(); se != null; se = se.getNextException()) {
                s.append('\n');
                s.append(se);
                int n = s.length() - 1;
                if (s.charAt(n) == '\n') s.setLength(n);
            }
            return s.toString();
        } else {
            return x.toString();
        }
    }

    /**
     * Build a message with info about ps for exceptions and so on. If the
     * ps contains batches of parameters they are all added to the info
     * String.
     */
    public static String getPreparedStatementInfo(String sql,
            PreparedStatement ps) {
        StringBuffer s = new StringBuffer();
        s.append(sql);
        s.append('\n');
        if (ps instanceof PooledPSWithParamLogging) {
            s.append("Params: ");
            PooledPSWithParamLogging lps = (PooledPSWithParamLogging)ps;
            int bc = lps.getLastBatchCount();
            if (bc > 0 ) {
                for (int i = 0; i < bc; i++) {
                    s.append('\n');
                    s.append(lps.getLastExecParamsString(i));
                }
            } else {
                s.append(lps.getLastExecParamsString());
            }
        } else {
            s.append("(set event logging to all to see parameter values)");
        }
        return s.toString();
    }

    /**
     * Build a message with info about ps for exceptions and so on. Parameter
     * info is provided for the given batch entry if available.
     */
    public static String getPreparedStatementInfo(String sql,
            PreparedStatement ps, int batchEntry) {
        StringBuffer s = new StringBuffer();
        s.append(sql);
        s.append('\n');
        if (ps instanceof PooledPSWithParamLogging) {
            s.append("Params: ");
            PooledPSWithParamLogging lps = (PooledPSWithParamLogging)ps;
            s.append(lps.getLastExecParamsString(batchEntry));
        } else {
            s.append("(set event logging to all to see parameter data)");
        }
        return s.toString();
    }


}

