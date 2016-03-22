
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
package com.versant.core.jdo.tools.plugins.eclipse;

import com.versant.core.jdo.tools.workbench.*;
import com.versant.core.jdo.tools.workbench.jdoql.insight.FieldDisplay;
import com.versant.core.jdo.tools.workbench.jdoql.insight.FieldDisplayPainter;
import com.versant.core.jdo.tools.workbench.model.MdClassNameValue;
import com.versant.core.jdo.tools.workbench.model.MdValue;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import za.co.hemtech.gui.editor.EditorManager;
import za.co.hemtech.gui.painter.PainterManager;

import javax.swing.*;
import java.io.File;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class VOAToolsPlugin extends AbstractUIPlugin{
	//The shared instance.
	private static VOAToolsPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	public VOAToolsPlugin(){
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("Versant");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public VOAToolsPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
    }

    public void start(BundleContext context) throws Exception {
		try{
		super.start(context);
        PainterManager.getInstance().registerAlternatePainter(
                FieldDisplay.class,
                new FieldDisplayPainter());
        PainterManager.getInstance().registerPainter(Date.class,
                new DateTimeCellPainter());
        PainterManager.getInstance().registerPainter(MdValue.class,
                new MdValueCellPainter());
        EditorManager.getInstance().registerEditor(MdValue.class,
                new MdValueEditor());
        EditorManager.getInstance().registerEditor(MdClassNameValue.class,
                new MdClassNameValueEditor());
        EditorManager.getInstance().registerAlternateEditor(MdValue.class,
                new MdClassNameValueEditor());
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        WorkbenchSettings.getInstance().load();
		}catch(Exception x){
			x.printStackTrace();
			throw x;
		}
	}

	public static void log(Throwable e) {
		log(e, "VOA Plugin internal error");
	}

	public static void log(Throwable e, String extra) {
		log(e, extra, IStatus.ERROR);
	}

	public static void log(Throwable e, String extra, int severity) {
		log(new Status(severity, getPluginId(), Status.ERROR, extra, e));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static String getPluginId() {
		return "Versant";
	}

	public static VOAToolsPlugin getDefault() {
		return plugin;
	}

	public Image getImage(String name){
        Image image = getImageRegistry().get("icons/"+name);
        if(image == null){
            try{
	            image = new Image(Display.getCurrent(), getClass().getResourceAsStream("/com/versant/core/jdo/tools/workbench/images/"+name));
	            getImageRegistry().put("icons/"+name, image);
            }catch(Exception x){
            	x.printStackTrace();
            }
        }
        return image;
	}

	public ImageDescriptor getImageDescriptor(String name){
		ImageDescriptor imageDescr = getImageRegistry().getDescriptor("icons/"+name);
        if(imageDescr == null){
            try{
	            Image image = getImage(name);
	            if(image != null){
	            	imageDescr = new ImageImageDescriptor(image);
		            getImageRegistry().put("icons/"+name, imageDescr);
	            }
            }catch(Exception x){
            	log(null, "Image "+name+" not found", Status.WARNING);
            }
        }
        return imageDescr;
	}

	private class ImageImageDescriptor extends ImageDescriptor{
		private Image fImage;

		public ImageImageDescriptor(Image image) {
			super();
			fImage= image;
		}

		public ImageData getImageData() {
			return fImage.getImageData();
		}

		public boolean equals(Object obj) {
			return (obj != null) && getClass().equals(obj.getClass()) && fImage.equals(((ImageImageDescriptor)obj).fImage);
		}

		public int hashCode() {
			return fImage.hashCode();
		}
	}
}
