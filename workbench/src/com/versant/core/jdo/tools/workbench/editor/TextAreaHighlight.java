
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
package com.versant.core.jdo.tools.workbench.editor;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

/**
 * Highlight interface. Create implementations of this interface and
 * add them to the text area with <code>TextAreaPainter.addCustomHighlight()</code>
 * to paint custom highlights.
 * @keep-all
 * @author Slava Pestov
 */
public interface TextAreaHighlight {
	/**
	 * Called after the highlight painter has been added.
	 * @param textArea The text area
	 * @param next The painter this one should delegate to
	 */
	void init(JEditTextArea textArea, TextAreaHighlight next);

	/**
	 * This should paint the highlight and delgate to the
	 * next highlight painter.
	 * @param gfx The graphics context
	 * @param line The line number
	 * @param y The y co-ordinate of the line
	 */
	void paintHighlight(Graphics gfx, int line, int y);

	/**
	 * Returns the tool tip to display at the specified
	 * location. If this highlighter doesn't know what to
	 * display, it should delegate to the next highlight
	 * painter.
	 * @param evt The mouse event
	 */
	String getToolTipText(MouseEvent evt);
}
