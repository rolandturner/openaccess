
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
public class VariableSorter extends SortableBase{
        private Vector variables;

        public VariableSorter(final Vector variables) {
            this.variables = variables;
        }


        public Vector getvariables() {
            return variables;
        }

        public void sort() {
            size = variables.size();
            super.sort();
        }

        public void setvariables(final Vector variables) {
            this.variables = variables;
        }

        protected int compare(final int a,final int b) {
            return ((VarNode)variables.get(a)).getIdentifier().compareTo(((VarNode)variables.get(b)).getIdentifier());
        }

        protected void swap(final int index1, final int index2) {
            Object a = variables.get(index1);
            Object b = variables.get(index2);

            variables.remove(index1);
            variables.insertElementAt(b,index1);

            variables.remove(index2);
            variables.insertElementAt(a,index2);
        }

        public VarNode search(String name){
            int low  = 0;
            int high = variables.size() - 1;
            int mid, cmp;
            VarNode node;
            while(low <= high){
                mid = (low + high) / 2;
                node = (VarNode)variables.get(mid);
                cmp = node.getIdentifier().compareTo(name);
                if( cmp < 0) low = mid + 1;
                else if(cmp > 0) high = mid - 1;
                else return node;
            }
            return null;
        }
    }
