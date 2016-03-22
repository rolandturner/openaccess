
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

import com.versant.core.metadata.*;
import com.versant.core.server.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.common.*;
import com.versant.core.storagemanager.RunningQuery;
import com.versant.core.storagemanager.ExecuteQueryReturn;
import com.versant.core.jdo.QueryDetails;

import com.versant.odbms.query.DatastoreQuery;

public final class VdsQueryResult
        implements RunningQuery, ExecuteQueryReturn {

    private long[] _loids;
    private OID[] _oids;
    private VdsCompiledQuery _cq;
    private int _pos;
    private int _cursorpos;
    private ClassMetaData _cmd;
    private VdsStorageManager sm;
    private DatastoreQuery _dq;
    private int _max = 0;
    private final LogEventStore pes;

    private boolean isCacheble;
    CachedQueryResult qRCache;

    public VdsQueryResult next;
    public VdsQueryResult prev;

    public VdsQueryResult(LogEventStore pes) {
        _loids = new long[0];
        this.pes = pes;
    }

    public VdsQueryResult(CompiledQuery cq, ClassMetaData cmd,
            VdsStorageManager sm, DatastoreQuery dq, boolean cachable) {
//      assert cq instanceof VersantCompiledQuery;
//      assert connectionholder != null;
//      assert dq != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(cq instanceof VdsCompiledQuery,
                    "cq is not a instanceof VersantCompiledQuery");
            Debug.assertInternal(dq != null,
                    "dq is null");
        }
      this._dq = dq;
      this._cq = (VdsCompiledQuery)cq;
      this._cmd = cmd;
      this.sm = sm;
      this.pes = sm.getPerfEventStore();
      _pos = 0;
      _cursorpos = 0;
      _max = 0; // todo sort this out: cq.getMaxRows();
        isCacheble = cachable;
    }

    public RunningQuery getRunningQuery() {
        return this;
    }

    public boolean next(int skip) {
      boolean hasNext = absolute(_pos + skip);
      _pos ++;
      return hasNext;
    }


    public boolean isRandomAccess() {
      return true;
    }


    public int getResultCount() {
      return _max;
    }


    public boolean absolute(int index) {
//        assert _loids != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(_loids != null,
                    "_loids is null");
        }

        _pos = index;
        _cursorpos = _pos;
        return (index < _max);
    }


    public OID getResultOID() {
//      assert _pos >= 0: "negatvie position " + _pos;
//      assert _pos < _oids.length;
        if (Debug.DEBUG) {
            Debug.assertInternal(_pos >= 0,
                    "negatvie position " + _pos);
            Debug.assertInternal(_pos < _oids.length,
                    "_pos < _oids.length");
        }
      if (_oids[_pos] == null)
        convertToOID(_loids.length);
      return _oids[_pos++];
    }

    public boolean addNextResult(int skipAmount, QueryResultContainer results,
            int fetchAmount) {
        if (skipAmount > 0 && !next(skipAmount)) {
            return false;
        }
      int max = _loids.length;
      if (_cq.getMaxRows() > 0)
        max = _cq.getMaxRows();
      else if (fetchAmount > 0) {
        if (_cursorpos + fetchAmount < max)
          max = _cursorpos + fetchAmount;
      }

        // fetch results using the correct fetch group
        StateFetchQueue q = new StateFetchQueue(_cmd.jmd, results.container,
                sm.con(), sm.getReadLock(), sm.getReadOption(), pes);
        FetchGroup fg = _cmd.fetchGroups[_cq.getFetchGroupIndex()];
        for (int i = _cursorpos; i < max; i++) {
            OID oid = _cmd.createOID(false);
            _oids[i] = oid;
            oid.setLongPrimaryKey(_loids[i]);
            q.add(oid, fg);
            results.addRow(oid);
        }
        q.fetch();

      _cursorpos = max;
      _pos = _cursorpos;
      return _loids.length > max;
//      if (_cq.getMaxRows() > 0)
//        return true;
//      if (_loids.length > max)
//        return false;
//      return true;
    }


    public void close() {
    }

    public CompiledQuery getCompiledQuery() {
      return _cq;
    }

    public void cancel() {
      throw unsupported();
    }

    public int getRelativeResultCount() {
      return 0;
    }

    public void resetRelativeResultCount() {
    }

    private void convertToOID(int fetchn) {
//      assert _loids != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(_loids != null,
                    "_loids is null");
        }
      int max = _loids.length;
      if (fetchn > 0) {
        if (_cursorpos + fetchn < max)
          max = _cursorpos + fetchn;
      }
      for (int i = _cursorpos; i < max; i++) {
        _oids[i] = _cmd.createOID(false);
        _oids[i].setLongPrimaryKey(_loids[i]);
      }
    }

    public void setLoids(long[] loids) {
      if (Debug.DEBUG) System.out.println ("vds returned " + loids.length + " objects");
      this._loids = loids;
      if (_max <= 0)
        _max = _loids.length;
      else
        _max = (_loids.length < _max) ? _loids.length : _max;
      _oids = new OID[_loids.length];
    }

    public DatastoreQuery getDatastoreQuery() {
      return _dq;
    }

    RuntimeException unsupported() {
      return BindingSupportImpl.getInstance().internal("unsupported");
    }

    /**
     * This is called to recheck if this results is still cacheble.
     */
    public void updateCacheble() {
        if (isCacheble) {
            if (sm.isActive() && !sm.isOptimistic() || sm.isFlushed()
                    || !sm.getCache().isQueryCacheEnabled()) {
                setNonCacheble();
                return;
            }
        }
    }

    public boolean isCacheble() {
        return isCacheble;
    }

    /**
     * If at any stage it is detected that the results may not be cached this
     * this method is called.
     */
    public void setNonCacheble() {
        isCacheble = false;
        qRCache = null;
    }

    public QueryDetails getQueryDetails() {
        return _cq.getQueryDetails();
    }

}


