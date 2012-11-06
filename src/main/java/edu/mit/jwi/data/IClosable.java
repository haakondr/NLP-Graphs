/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data;

/**
 * An object that can be closed. What 'closing' means is implementation
 * specific.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public interface IClosable {

	/**
	 * This closes the object by disposing of data backing objects or
	 * connections. If the object is already closed, or in the process of
	 * closing, this method does nothing (although, if the object is in the
	 * process of closing, it may block until closing is complete).
	 * 
	 * @since JWI 2.2.0
	 */
	public void close();
	
}
