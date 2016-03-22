
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
package com.versant.core.jdo.query;

import com.versant.core.common.SortableBase;

import java.util.Vector;

/**
 */
public class ImportSorter extends SortableBase{
        private Vector imports;

        public ImportSorter(final Vector imports) {
            this.imports = imports;

        }

        public void sort() {
            size = imports.size();
            super.sort();
        }

        public Vector getImports() {
            return imports;
        }

        public void setImports(final Vector imports) {
            this.imports = imports;
        }

        protected int compare(final int a, final int b) {
            return ((ImportNode)imports.get(a)).name.compareTo(((ImportNode)imports.get(b)).name);
        }

        protected void swap(final int index1,final int index2) {
            Object a = imports.get(index1);
            Object b = imports.get(index2);

            imports.remove(index1);
            imports.insertElementAt(b,index1);

            imports.remove(index2);
            imports.insertElementAt(a,index2);
        }

        public ImportNode search(String name){
            int low  = 0;
            int high = imports.size() - 1;
            int mid, cmp;
            ImportNode node;
            while(low <= high){
                mid = (low + high) / 2;
                node = (ImportNode)imports.get(mid);
                cmp = node.name.compareTo(name);
                if( cmp < 0) low = mid + 1;
                else if(cmp > 0) high = mid - 1;
                else return node;
            }
            return null;
        }
    }



