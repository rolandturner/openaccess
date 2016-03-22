
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
package com.versant.core.jdo.tools.plugins.eclipse.editor;

import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.metadata.ClassMetaData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class DDLEditor extends TextEditor {

	private String s = "Nothing to display";

	public DDLEditor() {
		super();
//		setSourceViewerConfiguration(new TextSourceViewerConfiguration());
		setDocumentProvider(new AbstractDocumentProvider() {
			protected IAnnotationModel createAnnotationModel(Object element)
					throws CoreException {
				return null;
            }

			protected IDocument createDocument(Object element)
					throws CoreException {
				return new Document(s);
			}

			protected void doSaveDocument(IProgressMonitor monitor,
					Object element, IDocument document, boolean overwrite)
					throws CoreException {
			}

			protected IRunnableContext getOperationRunner(
					IProgressMonitor monitor) {
				return new ProgressMonitorDialog(getSite().getShell());
			}
		});
	}

	public void dispose() {
		super.dispose();
	}

	protected void doSetInput(IEditorInput input) throws CoreException {
		try {
			final Object value = ((VOAMappingEditorInput) input).getObject();
			if (value instanceof MdProjectProvider) {
				MdProject mdProject = ((MdProjectProvider) value).getMdProject();
				JdbcStorageManagerFactory smf = mdProject.getJdbcStorageManagerFactory();
				ArrayList tables = new ArrayList();
				if (value instanceof MdClass) {
					JdbcTable table = ((MdClass) value).getTable();
					if (table != null) {
						tables.add(table);
					}
				} else if (value instanceof MdField) {
					JdbcTable table = ((MdField) value).getLinkTable();
					if (table != null) {
						tables.add(table);
					}
				} else if (value instanceof MdProject) {
					tables.addAll(smf.getJdbcMetaData().getTables());
				} else if (value instanceof JdbcTable) {
					tables.add(value);
				} else if (value instanceof MdPackage) {
					MdPackage mdPackage = (MdPackage) value;
					List classes = mdPackage.getClassList();
					HashSet packTables = new HashSet();
					for (Iterator it = classes.iterator(); it.hasNext();) {
						MdClass mdClass = (MdClass) it.next();
						ClassMetaData cmd = mdClass.getClassMetaData();
						if (cmd != null) {
							JdbcClass jdbcClass = (JdbcClass) cmd.storeClass;
							if (jdbcClass.doNotCreateTable) {
								continue;
							}
							jdbcClass.getTables(packTables);
						}
					}
					tables.addAll(packTables);
				}
				if (tables.size() == 0) {
					s = "Could not generate ddl.";
					return;
				}
				Collections.sort(tables);
				ByteArrayOutputStream out = new ByteArrayOutputStream(10240);
				PrintWriter pw = new PrintWriter(out, false);
				smf.getSqlDriver().generateDDL(tables, null, pw, true);
				pw.flush();
				out.flush();
				s = new String(out.toString());
				out.close();
			} else {
				s = "Project does not have VOA Nature.\nNothing to show.";
			}
		} catch (Exception x) {
			VOAToolsPlugin.log(x, "Error createing ddl.");
		} finally {
			super.doSetInput(input);
		}
	}
}
