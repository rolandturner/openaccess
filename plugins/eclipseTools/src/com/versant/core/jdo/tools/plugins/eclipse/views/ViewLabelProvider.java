
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
package com.versant.core.jdo.tools.plugins.eclipse.views;

import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdo.tools.plugins.eclipse.VOAToolsPlugin;
import com.versant.core.jdo.tools.workbench.model.MdClassOrInterface;
import com.versant.core.jdo.tools.workbench.model.MdField;
import com.versant.core.jdo.tools.workbench.model.MdPackage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.OverlayIcon;
import java.util.List;

public class ViewLabelProvider extends LabelProvider {

	public String getText(Object obj) {
		if(obj instanceof MdField) {
			return ((MdField)obj).getName();
		}else if(obj instanceof IProject) {
			return ((IProject)obj).getName();
		}else if(obj instanceof JdbcTable){
            return "KeyGen:"+obj;
        }else if(obj instanceof List){
            return "Inherited fields";
		}
		return obj.toString();
	}
	
	public Image getImage(Object obj) {
		boolean error = false;
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if(obj == null || obj instanceof IProject){
			imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT;
		}else if(obj instanceof MdPackage) {
			imageKey = ISharedImages.IMG_OBJ_FOLDER;
		}else if(obj instanceof MdClassOrInterface) {
            MdClassOrInterface f = (MdClassOrInterface)obj;
            return getImage(f.getTreeIcon(), f.hasErrors());
		}else if(obj instanceof JdbcTable){
            return getImage("Table16.gif", false);
		}else if(obj instanceof MdField) {
            MdField f = (MdField)obj;
            return getImage(f.getTreeIcon(), f.hasErrors());
        }else if(obj instanceof List) {
            imageKey = ISharedImages.IMG_OBJ_FOLDER;
		}
		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		return image;
	}

	private Image getImage(String name, boolean error) {
        Image image = VOAToolsPlugin.getDefault().getImage(name);
        if(error){
            Image errorImage = VOAToolsPlugin.getDefault().getImage("delete.gif");
			Rectangle bounds= image.getBounds();
        	OverlayIcon icon = new OverlayIcon(new ImageImageDescriptor(image), new ImageImageDescriptor(errorImage), new Point(bounds.width, bounds.height));
        	return icon.createImage();
        }
        return image; 
	}
}
