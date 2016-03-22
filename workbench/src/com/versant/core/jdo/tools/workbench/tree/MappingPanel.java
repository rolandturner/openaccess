
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
package com.versant.core.jdo.tools.workbench.tree;

import com.versant.core.jdo.tools.workbench.forms.ClassPropertyForm;
import com.versant.core.jdo.tools.workbench.forms.FieldPropertyForm;
import com.versant.core.jdo.tools.workbench.forms.InterfacePropertyForm;
import com.versant.core.jdo.tools.workbench.forms.ProjectPropertyForm;
import com.versant.core.jdo.tools.workbench.forms.overview.WorkbenchOverviewForm;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.jdo.tools.workbench.WorkbenchPanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2005/02/09
 * Time: 04:29:50
 * To change this template use File | Settings | File Templates.
 */
public class MappingPanel extends JPanel implements MdProjectListener, MdProjectProvider, WorkbenchPanelHelper{

    private ClassPropertyForm classPropertyForm;
    private FieldPropertyForm fieldPropertyForm;
    private InterfacePropertyForm interfacePropertyForm = new InterfacePropertyForm();
    private ProjectPropertyForm projectPropertyForm;
    private WorkbenchOverviewForm overviewForm = new WorkbenchOverviewForm(
            "overview.html");
    private JPanel form = null;
    private MdProjectProvider projectProvider = MdProjectProviderManager.getDefaultProjectProvider();
    private WorkbenchPanelHelper workbenchPanelHelper;

    public MappingPanel(WorkbenchPanelHelper workbenchPanelHelper) throws Exception{
        super(new BorderLayout());
        this.workbenchPanelHelper = workbenchPanelHelper;
        interfacePropertyForm.setTitle(null);
        activateOverview();
    }

    private void activateForm(JPanel newForm) {
        if (form == newForm) return;
        removeAll();
        add(form = newForm, BorderLayout.CENTER);
    }

    public boolean activateOverview() {
        activateForm(overviewForm);
        return true;
    }

    public void valueChanged(Object o) {
        if(o instanceof MdPackage){
            o = ((MdPackage)o).getMdProject();
        }
        if(o instanceof MdProjectProvider){
            projectProvider = (MdProjectProvider) o;
        }
        try {
            JPanel newForm = overviewForm;
            MdClass mdClass = null;
            MdField mdField = null;
            if (o instanceof MdClass) {
                mdClass = (MdClass)o;
                if (classPropertyForm == null) {
                    classPropertyForm = new ClassPropertyForm();
                }
                newForm = classPropertyForm;
            } else if (o instanceof MdField) {
                mdField = (MdField)o;
                if (fieldPropertyForm == null) {
                    fieldPropertyForm = new FieldPropertyForm();
                    fieldPropertyForm.setTitle(null);
                }
                newForm = fieldPropertyForm;
            } else if (o instanceof MdInterface) {
                interfacePropertyForm.setMdInterface((MdInterface)o);
                newForm = interfacePropertyForm;
            }else if(o instanceof MdProject){
                MdProject project = (MdProject) o;
                if(projectPropertyForm == null || projectPropertyForm.getProject() != project){
                    projectPropertyForm = new ProjectPropertyForm(project);
                    projectPropertyForm.setTitle(null);
                }
                newForm = projectPropertyForm;
            }
            if (mdField != null) {
                try {
                    fieldPropertyForm.setMdField(mdField);
                } catch (Exception e1) {
                    //do Nothing
                }
            } else if (mdClass != null) {
                try {
                    classPropertyForm.setMdClass(mdClass);
                } catch (Exception e1) {
                    //do Nothing
                }
            }
            activateForm(newForm);
        } catch (Exception e1) {
            e1.printStackTrace(System.out);
            za.co.hemtech.gui.util.GuiUtils.dispatchException(this, e1);
        }finally{
            revalidate();
            repaint();
        }
    }


    public void updateUI() {
        super.updateUI();
        classPropertyForm = null;
        fieldPropertyForm = null;
    }

    /**
     * The project has changed.
     */
    public void projectChanged(MdProjectEvent ev) {
        switch (ev.getFlags()) {
            case MdProjectEvent.ID_ENGINE_STARTED:
            case MdProjectEvent.ID_ENGINE_STOPPED:
            case MdProjectEvent.ID_PARSED_DATABASE:
            case MdProjectEvent.ID_DIRTY_FLAG:
            case MdProjectEvent.ID_CLASSES_REMOVED:
//            case MdProjectEvent.ID_PARSED_META_DATA:
            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                return;
        }
        if(form instanceof MdProjectListener){
            ((MdProjectListener)form).projectChanged(ev);
        }
    }

    public MdProject getMdProject() {
        return projectProvider.getMdProject();
    }

    public Frame getMainFrame() {
        return workbenchPanelHelper.getMainFrame();
    }

    public boolean isReloadingClasses() {
        return workbenchPanelHelper.isReloadingClasses();
    }
}
