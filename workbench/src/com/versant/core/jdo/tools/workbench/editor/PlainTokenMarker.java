
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

import javax.swing.text.Segment;
/**
 * @keep-all
 */
public class PlainTokenMarker extends TokenMarker {
  public PlainTokenMarker() { }

  public byte markTokensImpl(byte token, Segment line, int lineIndex) {
    addToken(line.count, Token.NULL);
    return Token.NULL;
  }
}

// End of PlainTokenMarker.java
