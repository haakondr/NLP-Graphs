/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.item;

/**
 * An object that potentially has an associated part of speech.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public interface IHasPOS {

	/**
	 * Returns the associated part of speech for this object. If this object is
	 * not associated with any particular part of speech, this method may return
	 * <code>null</code>.
	 * 
	 * @return The associated part of speech, or <code>null</code> if none.
	 * @since JWI 2.0.0
	 */
	public POS getPOS();
}
