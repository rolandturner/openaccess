
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
/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * DataStoreCache.java
 *
 */
 
package javax.jdo.datastore;

import java.util.Collection;


/** 
 * Many JDO implementations allow instances to be cached in a
 * second-level cache, and allow direct management of the cache by
 * knowledgeable applications. This interface standardizes this
 * behavior.
 * @since 2.0
 * @version 2.0
 */
public interface DataStoreCache {

    /** Evict the parameter instance from the second-level cache.
     * @param oid the object id of the instance to evict.
     * @since 2.0
     */
    void evict (Object oid);

    /** Evict the parameter instances from the second-level cache.
     * All instances in the PersistenceManager's cache are evicted 
     * from the second-level cache.
     * @since 2.0
     */
    void evictAll ();

    /** Evict the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     * @since 2.0
     */
    void evictAll (Object[] oids);

    /** Evict the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     * @since 2.0
     */
    void evictAll (Collection oids);

    /** Evict the parameter instances from the second-level cache.
     * @param pcClass the class of instances to evict
     * @param subclasses if true, evict instances of subclasses also
     * @since 2.0
     */
    void evictAll (Class pcClass, boolean subclasses);

    /** Pin the parameter instance in the second-level cache.
     * @param oid the object id of the instance to pin.
     * @since 2.0
     */
    void pin (Object oid);

    /** Pin the parameter instances in the second-level cache.
     * @param oids the object ids of the instances to pin.
     * @since 2.0
     */
    void pinAll (Collection oids);

    /** Pin the parameter instances in the second-level cache.
     * @param oids the object ids of the instances to pin.
     * @since 2.0
     */
    void pinAll (Object[] oids);

    /** Pin instances in the second-level cache.
     * @param pcClass the class of instances to pin
     * @param subclasses if true, pin instances of subclasses also
     * @since 2.0
     */
    void pinAll (Class pcClass, boolean subclasses);

    /** Unpin the parameter instance from the second-level cache.
     * @param oid the object id of the instance to unpin.
     * @since 2.0
     */
    void unpin(Object oid);

    /** Unpin the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     * @since 2.0
     */
    void unpinAll(Collection oids);

    /** Unpin the parameter instance from the second-level cache.
     * @param oids the object id of the instance to evict.
     * @since 2.0
     */
    void unpinAll(Object[] oids);

    /** Unpin instances from the second-level cache.
     * @param pcClass the class of instances to unpin
     * @param subclasses if true, unpin instances of subclasses also
     * @since 2.0
     */
    void unpinAll(Class pcClass, boolean subclasses);
    
    /** 
     * This class is an empty implementation of the DataStoreCache 
     * interface. It can be used by an implementation that does not
     * support a second-level cache.
     * @since 2.0
     */
    public class EmptyDataStoreCache implements DataStoreCache {
        
        public EmptyDataStoreCache() {
        }
        
        public void evict(Object oid) {
        }

        public void evictAll() {
        }

        public void evictAll(Object[] oids) {
        }

        public void evictAll(Collection oids) {
        }

        public void evictAll(Class pcClass, boolean subclasses) {
        }

        public void pin(Object oid) {
        }

        public void pinAll(Object[] oids) {
        }

        public void pinAll(Collection oids) {
        }

        public void pinAll(Class pcClass, boolean subclasses) {
        }

        public void unpin(Object oid) {
        }

        public void unpinAll(Object[] oids) {
        }

        public void unpinAll(Collection oids) {
        }

        public void unpinAll(Class pcClass, boolean subclasses) {
        }
    }
}
