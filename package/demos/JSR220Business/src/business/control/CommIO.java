
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
package business.control;

import java.io.InputStreamReader;
import java.io.BufferedReader;

class CommIO{

	String getUserInput( String why ){
		System.out.println( why );
		return getUserInput( );
	}
	
	String getUserInput(){
		InputStreamReader  input = new InputStreamReader(System.in);
		BufferedReader stream = new BufferedReader( input );
		String answer = new String();
		try{
			answer = stream.readLine();
		}
		catch (java.io.IOException e){
		}
		return answer;
	}
	
	public static void suspend(String why ){
		System.out.println( why );
		suspend();
	}
	public static void suspend(){
		CommIO input = new CommIO();
		input.getUserInput();
	}

}
