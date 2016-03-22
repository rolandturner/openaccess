
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
package com.versant.core.jdo.tools.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.Properties;

public class VOAConfigComposite extends Composite implements VersantStatics{

	private DialogPage dialogPage;
	private IProject project;
    private VOAConfigStruct pc;

    private Button addEnhanceBuilder;
    private Text textProjectFile;
    private Button buttonProjectFile; 
    private Button buttonCopyProjectFile;
    private Text textTokenFile;
    private Button buttonTokenFile;
    private Button buttonRelPaths;

    private boolean valid;
    private boolean intenal = true;

	public VOAConfigComposite(DialogPage dialogPage, IProject project, Composite parent) {
		super(parent, SWT.NONE);
		this.project = project;
		this.dialogPage = dialogPage;
		pc = new VOAConfigStruct(project);
		createContents(parent);
        resetFromStore();
    	ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				configChanged();
			}
		};
		SelectionListener sl = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configChanged();
			}
		};
	    addEnhanceBuilder.addSelectionListener(sl);
	    textProjectFile.addModifyListener(ml);
	    buttonCopyProjectFile.addSelectionListener(sl);
	    textTokenFile.addModifyListener(ml);
	    buttonRelPaths.addSelectionListener(sl);
	    intenal = false;
        configChanged();
	}

    private Control createContents(Composite composite) {
        GridData gridData = null;
    	GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        Composite pComposite = new Composite(composite, SWT.NONE);
        pComposite.setLayout(new GridLayout(3, false));

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        pComposite.setLayoutData(gridData);

        Label labelProjectFile = new Label(pComposite, SWT.LEFT);
        labelProjectFile.setText("Project file name");

        textProjectFile = new Text(pComposite, SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        textProjectFile.setLayoutData(gridData);

        buttonProjectFile = new Button(pComposite, SWT.PUSH);
        buttonProjectFile.setText("...");


        addEnhanceBuilder = new Button(composite, SWT.CHECK);
        addEnhanceBuilder.setText("Auto enhance");
        gridData = new GridData();
        gridData.horizontalSpan = 3;
        addEnhanceBuilder.setLayoutData(gridData);


        buttonProjectFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	if(handleBrowse(textProjectFile)){
            		addEnhanceBuilder.setSelection(true);
            	}
            }
        });

        buttonCopyProjectFile = new Button(composite, SWT.CHECK);
        buttonCopyProjectFile.setText("Copy project file to output dir");

        gridData = new GridData();
        gridData.horizontalSpan = 3;
        buttonCopyProjectFile.setLayoutData(gridData);

        Composite tComposite = new Composite(composite, SWT.NONE);
        tComposite.setLayout(new GridLayout(3, false));

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        tComposite.setLayoutData(gridData);

        Label labelTokenFile = new Label(tComposite, SWT.LEFT);
        labelTokenFile.setText("Ant filter token properties");

        textTokenFile = new Text(tComposite, SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        textTokenFile.setLayoutData(gridData);

        buttonTokenFile = new Button(tComposite, SWT.PUSH);
        buttonTokenFile.setText("...");
        buttonTokenFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	handleBrowse(textTokenFile);
            }
        });

        buttonRelPaths = new Button(composite, SWT.CHECK);
        buttonRelPaths.setText("Use relative paths");
        gridData = new GridData();
        gridData.horizontalSpan = 3;
        buttonRelPaths.setLayoutData(gridData);

        Composite emptyComposite = new Composite(composite, SWT.NONE);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 3;
        emptyComposite.setLayoutData(gridData);

        return composite;
    }

	private void configChanged() {
		if(intenal)return;
		boolean copyProjectFile = buttonCopyProjectFile.getSelection();
		String projectFilePath = textProjectFile.getText();
		boolean hasProject = projectFilePath != null && projectFilePath.length() > 0;
		boolean doAutoBuild = addEnhanceBuilder.getSelection() && projectFilePath != null && hasProject;
		pc.setDoAutoBuild(doAutoBuild);
		pc.setProjectFilePath(projectFilePath);
		pc.setCopyProjectFile(copyProjectFile);
		pc.setTokenFilePath(textTokenFile.getText());
		pc.setStorePathsRel(buttonRelPaths.getSelection());

		addEnhanceBuilder.setEnabled(hasProject);
		buttonCopyProjectFile.setEnabled(hasProject);
		textTokenFile.setEnabled(copyProjectFile && hasProject);
        buttonTokenFile.setEnabled(copyProjectFile && hasProject);
        validateProject();
	}

	private void validateProject() {
		String projectPath = pc.getProjectFilePath();
		if (projectPath == null || projectPath.length() == 0) {
			setMessage("Please select a VOA Project file.", IMessageProvider.ERROR);
			return;
		}
		File file = new File(projectPath);
		String cPath = projectPath;
		try {
			cPath = file.getCanonicalPath();
		} catch (IOException e1) {
			cPath = file.getAbsolutePath();
		}
		if(!file.getAbsolutePath().equals(projectPath)){
			file = new java.io.File(project.getLocation().toFile(), projectPath);
		}
		try {
			cPath = file.getCanonicalPath();
		} catch (IOException e1) {
			cPath = file.getAbsolutePath();
		}
		projectPath = cPath;
		pc.setProjectFilePath(projectPath);
		if (file != null && file.isDirectory()) {
			setMessage(projectPath+" is not a valid file.", IMessageProvider.ERROR);
			return;
		}
		if (file != null && file.exists() && !file.canWrite()) {
			setMessage("Project must be writable", IMessageProvider.ERROR);
			return;
		}
		boolean createFile = file == null || !file.exists();
		if (createFile) {
			File source = null;
			try {
				source = Utils.getSrcFile(project);
			} catch (JavaModelException e) {
				source = project.getLocation().toFile();
			}
			String jdoPathStr = source.getName()+"/"+project.getName()+".jdo";
			setMessage("Create Project file: "+file+"\nand .jdo file: "+jdoPathStr, IMessageProvider.WARNING);
		}else{
			setMessage("Project file found", IMessageProvider.INFORMATION);
			Properties props = new Properties();
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				props.load(in);
			} catch (Exception e) {
				e.printStackTrace();
				try{in.close();}catch(Exception x){}
			}
			if(!props.containsKey("javax.jdo.option.ConnectionURL")){
				setMessage("Project file("+file.getName()+") is not valid.\nIt does not contain the property:javax.jdo.option.ConnectionURL", IMessageProvider.ERROR);
				return;
			}
		}
	}

	private void setMessage(String string, int type) {
		valid = type != IMessageProvider.ERROR;
		dialogPage.setMessage(string, type);
	}

	public void resetFromStore() {
	    intenal = true;
        try {
            pc.load();
            addEnhanceBuilder.setSelection(pc.isDoAutoBuild());
            textProjectFile.setText(pc.getProjectFilePath());
            buttonCopyProjectFile.setSelection(pc.isCopyProjectFile());
            textTokenFile.setText(pc.getTokenFilePath());
            buttonRelPaths.setSelection(pc.isStorePathsRel());
        } catch (CoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
    	    intenal = false;
            configChanged();
        }
    }

    public void store() {
	    intenal = true;
        try {
	        if (!pc.isDirty()) return;
	        String projectPath = pc.getProjectFilePath();
			File file = new File(projectPath);
			String cPath = projectPath;
			try {
				cPath = file.getCanonicalPath();
			} catch (IOException e1) {
				cPath = file.getAbsolutePath();
			}
			if(!file.getAbsolutePath().equals(projectPath)){
				file = new java.io.File(project.getLocation().toFile(), projectPath);
			}
			try {
				cPath = file.getCanonicalPath();
			} catch (IOException e1) {
				cPath = file.getAbsolutePath();
			}
			projectPath = cPath;
			pc.setProjectFilePath(projectPath);
			// create a sample file
			if (file == null || !file.exists()) {
                String jdoFileName = project.getName()+".jdo";
                File jdoFile = new File(Utils.getSrcFile(project), jdoFileName);
                FileOutputStream out;
                if(!jdoFile.exists()){
                    out = new FileOutputStream(jdoFile);
                    out.write(getJDOContent());
                    out.flush();
                    out.close();
                }
                out = new FileOutputStream(file);
                out.write(getProjectContent(jdoFileName));
                out.flush();
                out.close();
			}
	    	pc.store();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }finally{
    	    intenal = false;
        }
    }
	
	private byte[] getProjectContent(String jdoFile) {
		String contents = "javax.jdo.PersistenceManagerFactoryClass=com.versant.core.jdo.BootstrapPMF\n"+
						  "javax.jdo.option.ConnectionURL=jdbc:hsqldb:mem:aname\n"+
						  "versant.metadata.0="+jdoFile;
		return contents.getBytes();
	}
	
	private byte[] getJDOContent() {
		String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
						  "<jdo>\n"+
						  "</jdo>";
		return contents.getBytes();
	}

    public boolean validate() {
        return valid;
    }

    public void setToDefaults() {
	    intenal = true;
    	try{
	        addEnhanceBuilder.setSelection(DEFAULT_AUTO_ENHANCE);
	        buttonRelPaths.setSelection(DEFAULT_RELATIVE_PATHS);
	        buttonCopyProjectFile.setSelection(DEFAULT_COPY_PROJECT_FILE);
	        textProjectFile.setText(DEFAULT_PROJECT_FILE);
    	}finally{
    	    intenal = false;
            configChanged();
    	}
    }

    private boolean handleBrowse(Text text) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        String val = project.getLocation().toFile().getPath();
        if (!val.endsWith(File.separator)) {
            val = val + File.separator;
        }
        dialog.setFilterExtensions(new String[] {"*.properties", "*.jdogenie", "*.*"});
        dialog.setFilterNames(new String[] {"Versant OpenAccess Project Files", "JDO Genie Project Files", "All Files (*.*)"});
        dialog.setFilterPath(val);
        String result = dialog.open();
        if (result != null) {
        	text.setText(result);
            return true;
        }
        return false;
	}
}
