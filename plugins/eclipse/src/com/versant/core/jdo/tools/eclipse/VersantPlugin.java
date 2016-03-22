
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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The plugin class.
 */
public class VersantPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static VersantPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public VersantPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("com.versant.core.jdo.tools.plugins.eclipse.EclipsePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = VersantPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static VersantPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
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
}
